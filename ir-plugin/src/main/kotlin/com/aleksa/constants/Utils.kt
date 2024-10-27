package com.aleksa.constants

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.symbols.IrReturnTargetSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.util.isLocal
import org.jetbrains.kotlin.ir.util.isObject
import org.jetbrains.kotlin.ir.util.isSetter
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.SpecialNames
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSupertypeOf

fun primitiveClassForName(name: String): KClass<*>? {
    return when(name) {
        "kotlin.Int" -> Int::class
        "kotlin.Byte" -> Byte::class
        "kotlin.Char" -> Char::class
        "kotlin.Long" -> Long::class
        "kotlin.Boolean" -> Boolean::class
        "kotlin.Double" -> Double::class
        "kotlin.Float" -> Float::class
        "kotlin.Short" -> Short::class
        else -> null
    }
}
fun assertPrimitiveClassForName(name: String): KClass<*> = primitiveClassForName(name) ?: throw Exception("Can't find primitive class for ${name}")

fun createIrConst(value: Any?, basedOn: IrExpression): IrExpression? {
    val s = basedOn.startOffset
    val e = basedOn.endOffset
    val type = basedOn.type
    return when (value) {
        is Int -> IrConstImpl(s, e, type, IrConstKind.Int, value)
        is Byte -> IrConstImpl(s, e, type, IrConstKind.Byte, value)
        is Char -> IrConstImpl(s, e, type, IrConstKind.Char, value)
        is Boolean -> IrConstImpl(s, e, type, IrConstKind.Boolean, value)
        is Double -> IrConstImpl(s, e, type, IrConstKind.Double, value)
        is Float -> IrConstImpl(s, e, type, IrConstKind.Float, value)
        is Long -> IrConstImpl(s, e, type, IrConstKind.Long, value)
        is Short -> IrConstImpl(s, e, type, IrConstKind.Short, value)
        is Unit -> IrGetObjectValueImpl(s, e, type, type.classOrFail)
        null -> IrConstImpl(s, e, type, IrConstKind.Null, null)
        else -> null
    }
}
class InvalidIrConstValue: Exception()
fun assertIrConst(value: Any?, basedOn: IrExpression) = createIrConst(value, basedOn) ?: throw InvalidIrConstValue()

val IrSymbol.name: String? get() = (owner as? IrDeclarationWithName)?.name?.asString()
val IrSymbol.scope: IrFunction? get() = (owner as? IrDeclaration)?.scope

/**
 * The declaration's surrounding method, or null for nonlocal declarations.
 */
val IrDeclaration.scope: IrFunction? get() {
    var decl = this.parent as? IrDeclaration
    while (decl != null)
        if (decl is IrFunction)
            return decl
        else
            decl = decl.parent as? IrDeclaration
    return null
}

val IrCall.allArguments: List<IrExpression> get() =
    listOfNotNull(dispatchReceiver, extensionReceiver) +
            valueArgumentExpressions

val IrCall.valueArgumentExpressions: List<IrExpression> get() =
    valueArguments.mapIndexed { index, it ->  if (it == null) this.symbol.owner.valueParameters.get(index).defaultValue!!.expression else it }

//TODO: Cache the results
/**
 * Find all local symbols assigned within this statement,
 * including the methods it invokes.
 */
val IrElement.assignedSymbols: Set<IrSymbol>  get() {
    val result = mutableSetOf<IrSymbol>()
    val visited = mutableListOf(this)
    val visitor = object: IrElementVisitorVoid {
        override fun visitElement(element: IrElement) {
            element.acceptChildrenVoid(this)
        }
        override fun visitDeclaration(declaration: IrDeclarationBase) {
            //Stop at class and function declarations
        }
        override fun visitVariable(declaration: IrVariable) {
            result.add(declaration.symbol)
            declaration.acceptChildrenVoid(this)
        }
        override fun visitSetValue(expression: IrSetValue) {
            result.add(expression.symbol)
            super.visitSetValue(expression)
        }
        override fun visitCall(expression: IrCall) {
            val func = expression.symbol.owner
            if (!visited.contains(func) && func.isLocal) {
                visited.add(func)
                func.acceptChildrenVoid(this)
            }
            super.visitCall(expression)
        }
    }
    acceptVoid(visitor)
    return result
}


//TODO: Cache the results
val IrDeclaration.isStack: Boolean get() {
    if (this !is IrVariable && this !is IrValueParameter)
        return false
    val parent = parent
    if (parent !is IrFunction)
        return false
    val visitor = object : IrElementVisitorVoid {
        var leaks = false
        override fun visitElement(element: IrElement) {
            if (!leaks)
                element.acceptChildrenVoid(this)
        }
        override fun visitFunction(declaration: IrFunction) {
            if (declaration.body?.assignedSymbols?.contains(symbol) != true)
                return super.visitFunction(declaration)
            //check if this capture can leak into nonlocal context by way of a lambda or object
            var func: IrDeclaration = declaration
            while(func != parent) {
                if (func is IrFunction && func.isLambda) {
                    leaks = true
                    break
                }
                func = func.parent as? IrDeclaration ?: break
            }
            super.visitFunction(declaration)
        }
    }
    parent.acceptChildrenVoid(visitor)
    return !visitor.leaks
}
val IrSymbol.isStack: Boolean get() = (owner as? IrDeclaration)?.isStack ?: false
val IrField.isStaticFinal: Boolean get() = isFinal && (isStatic || (parent as? IrClass)?.isObject == true)
val IrSymbol.isStaticFinal: Boolean get() = (owner as? IrField)?.isStaticFinal ?: false
val IrFunction.isLambda get() = name == SpecialNames.ANONYMOUS

fun Collection<KCallable<*>>.match(name: String, args: List<KClass<*>>): KCallable<*> {
    return single {
        it.name == name && it.parameters.zip(args).all { it.first.type.isSupertypeOf(it.second.createType()) }
    }
}