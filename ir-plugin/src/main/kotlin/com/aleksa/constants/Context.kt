package com.aleksa.constants

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.symbols.IrSymbol

/**
 * This is a map of known variables when interpreting a method or code fragment during compile time.
 * Basically a stack frame, except for the root context which contains global static constants.
 * Lambdas keep a reference to the stack context they were created in.
 * Each local context is associated with the corresponding IrFunction declaration (scope).
 * Only assignments to immutable variables or variables modified by a single thread are allowed.
 *  This includes variables allocated on the stack, static immutable variables,
 *  variables that are captured in closures, but are never assigned to within them.
 *  It could also include immutable fields, however object constructors and virtual method invocations
 *  are not interpreted at all here.
 */
class Context(
    val scope: IrFunction? = null,
    val parent: Context? = null,
    val mutable: Boolean = true,
    private val values: MutableMap<IrSymbol, Any?> = mutableMapOf()
) {
    class ImmutabilityException(msg: String?): Exception(msg)

    val root: Context
        get() {
            var cur: Context = this
            while (cur.parent != null)
                cur = cur.parent!!
            return cur
        }

    operator fun get(symbol: IrSymbol): Any? {
        if (symbol.scope == scope)
            return getValue(symbol)
        return if (parent != null) parent.getValue(symbol) else Evaluator.UnknownValue
    }

    operator fun set(symbol: IrSymbol, value: Any?) {
        if (symbol.scope == scope) {
            if (!mutable)
                throw ImmutabilityException("Can't write ${symbol.name} because the context is immutable.")
            if (symbol.isStack || symbol.isStaticFinal)
                values[symbol] = value
        }
        else if (parent != null)
            parent[symbol] = value
    }

    fun removeAll(symbols: Set<IrSymbol>) {
        for (symbol in symbols) this[symbol] = Evaluator.UnknownValue
    }
    fun remove(symbol: IrSymbol) {
        this[symbol] = Evaluator.UnknownValue
    }
    fun removeVars() {
        values.entries.removeIf f@{
            val owner = it.key.owner as? IrVariable ?: return@f false
            !owner.isConst
        }
    }


    fun copy(): Context {
        val context = Context(scope, parent?.copy(), mutable)
        context.values.putAll(values)
        return context
    }

    fun asImmutable(): Context
            = Context(scope, parent?.asImmutable(), mutable = false, values = values)

    fun dump(): String {
        var result: String = ""
        if (parent != null) result += parent.dump()
        else result += "--------------------------------\n----------CONTEXT DUMP------------\n"
        result += "Scope of ${scope?.name}\n"
        for (entry in values) {
            if (entry.value !is Evaluator.Unknown)
                result += "${entry.key.name}\t = ${entry.value}\n"
        }
        return result + "--------------------------------\n"
    }

    private fun getValue(symbol: IrSymbol): Any? {
        if (!values.contains(symbol))
            return Evaluator.UnknownValue
        return values[symbol]
    }

    companion object {
        fun forMethod(method: IrFunction, root: Context? = null): Context {
            val outerMethods = mutableListOf<IrFunction>()
            var cur: IrElement = method
            while (cur is IrDeclaration) {
                if (cur is IrFunction)
                    outerMethods.add(cur)
                cur = cur.parent
            }
            var result: Context = root ?: Context()
            for (scope in outerMethods.reversed())
                result = Context(scope, result)
            return result
        }
    }
}
