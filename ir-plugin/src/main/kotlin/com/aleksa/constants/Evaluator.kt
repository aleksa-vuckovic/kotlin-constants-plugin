package com.aleksa.constants

import org.intellij.lang.annotations.Flow
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.IrReturnTargetSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

/**
 * Evaluate an entire expression, including any side effects (assignments),
 * and return the value without altering the original expression tree.
 * May return Context.Unknown.
 *
 * Only supports:
 *  - Primitive type operations.
 *  - Lambda creation and execution.
 *  - Assigning and reading static properties (and by static I mean top level and singleton objects).
 *  - Assigning and reading local variables.
 *  - Invoking static methods (no dispatch receiver or the receiver is a singleton object).
 *
 * It does not support:
 *  - Creating objects by invoking constructors.
 *  - Virtual method invocation (and, by extension, field access).
 *  - A bunch of other stuff.
 */
class Evaluator(
    val messageCollector: MessageCollector? = null,
    val shouldEvaluate: (IrFunction) -> Boolean = {it.body != null && it !is IrConstructor},
): IrElementVisitor<Any?, Context> {

    interface FlowChange //A flow change (possible or definite)
    interface Unknown { //Unknown result of evaluation. Might be a flow change, might be a value.
        operator fun plus(change: FlowChange): Unknown = plus(setOf(change))
        operator fun plus(other: Unknown): Unknown = if (other is PossibleFlowChange) plus(other.changes) else this
        operator fun plus(changes: Set<FlowChange>): Unknown
        operator fun minus(loop: IrLoop): Unknown = minus(setOf(loop))
        operator fun minus(target: IrReturnTargetSymbol): Unknown = minus(setOf(target))
        operator fun minus(loopsOrTargets: Set<*>): Unknown = UnknownValue
    }
    object UnknownValue: Unknown {
        override operator fun plus(changes: Set<FlowChange>) = PossibleFlowChange(mutableSetOf<FlowChange>().apply { addAll(changes) })
    }
    data class Return(val value: Any?, val target: IrReturnTargetSymbol, val type: IrType): FlowChange
    data class Continue(val target: IrLoop): FlowChange
    data class Break(val target: IrLoop): FlowChange
    class PossibleFlowChange(val changes: MutableSet<FlowChange> = mutableSetOf()): Unknown {
        override operator fun plus(moreChanges: Set<FlowChange>) = PossibleFlowChange(
            changes = mutableSetOf<FlowChange>().apply {
                addAll(changes)
                addAll(moreChanges)
            }
        )
        override operator fun minus(loopsOrTargets: Set<*>): Unknown {
            var rem = changes
                .filterNot { it is Return && loopsOrTargets.contains(it.target) }
                .filterNot { it is Break && loopsOrTargets.contains(it.target) }
                .filterNot { it is Continue && loopsOrTargets.contains(it.target) }
                .toMutableSet()
            return if (rem.isEmpty()) UnknownValue
            else PossibleFlowChange(rem)
        }
    }

    class EvaluationException(msg: String?) : Exception(msg)

    override fun visitElement(element: IrElement, data: Context): Any? {
        throw EvaluationException("Element type ${element} not supported.")
    }

    override fun visitDeclaration(declaration: IrDeclarationBase, data: Context): Any? {
        return Unit
    }

    override fun visitVariable(declaration: IrVariable, data: Context): Any? {
        val expr = declaration.initializer
        if (expr != null) {
            val value = expr.accept(this, data)
            if (value is Unknown)
                data.remove(declaration.symbol)
            else
                data[declaration.symbol] = value
            return value
        }
        return Unit
    }

    override fun visitCall(expression: IrCall, data: Context): Any? {
        val call = expression
        val func = call.symbol.owner
        //Evaluate args and check if any control flow change is encountered
        val args = mutableListOf<Any?>()
        val allArguments = call.allArguments
        var i = 0
        while (i < allArguments.size) {
            var arg = allArguments[i].accept(this, data)
            if (arg is PossibleFlowChange) {
                for (j in i+1..<allArguments.size) {
                    data.removeAll(allArguments[j].assignedSymbols)
                    arg += allArguments[j].flowChanges
                }
                if (func.isLocal && func.body != null)
                    data.removeAll(func.body!!.assignedSymbols)
               arg += func.body.flowChanges - func.symbol
                return arg
            }
            else if (arg is FlowChange)
                return arg
            args.add(arg)
            i++
        }
        fun invocationContext(parent: Context): Context {
            val context = Context(func, parent)
            func.allParameters.map { it.symbol }.zip(args).forEach {
                context[it.first] = it.second
            }
            return context
        }
        //Primitive operator or conversion?
        if (call.dispatchReceiver != null && primitiveClassForName(call.dispatchReceiver?.type?.classFqName?.asString() ?: "") != null)
            return processPrimitive(func.name.asString(), args)
        //Comparison?
        if (func.kotlinFqName.asString().startsWith("kotlin.internal.ir"))
            return processComparison(func.name.asString(), args)
        //Lambda?
        if (func.kotlinFqName.asString().matches("""^kotlin.Function\d+.invoke$""".toRegex())) {
            //lambda invocation
            val actualFunc = call.dispatchReceiver!!.accept(this, data) as? Pair<*, *> ?: return UnknownValue //TODO: There should be a way to make this an exception when using with Replacer
            val lambda = actualFunc.first as? IrFunction ?: return UnknownValue
            val lambdaContext = actualFunc.second as? Context ?: return UnknownValue
            val context = Context(lambda, lambdaContext)
            lambda.allParameters.map { it.symbol }.zip(args.drop(1)).forEach {
                context[it.first] = it.second
            }
            //interpret the lambda and return the result
            return processCall(lambda, context)
        }

        if (!shouldEvaluate(func)) {
            if (func.body != null && func.isLocal)
                data.removeAll(func.body?.assignedSymbols ?: setOf())
            return func.body.flowChanges - func.symbol //Might be an inlined lambda with a return targeting outer method
        }
        if (func.dispatchReceiverParameter != null) {
            //This is only allowed when the dispatch receiver is a singleton object
            val clazz = func.dispatchReceiverParameter!!.type.getClass()
            if (clazz == null || clazz.isLocal || !clazz.isObject)
                return UnknownValue //TODO: There should be a way to make this an exception when using with Replacer
            return processCall(func, invocationContext(data.root))
        } else {
            //To facilitate local function definitions, find the appropriate parent context
            var parent = data
            while (parent.scope != func.parent && parent.parent != null)
                parent = parent.parent!! //Not expecting an exception here
            //interpret the invocation and return the result
            return processCall(func, invocationContext(parent))
        }
    }
    private fun processCall(function: IrFunction, context: Context): Any? {
        val body = function.body
        val retSymbol = when(body) {
            is IrBlockBody -> function.symbol
            is IrExpressionBody -> (body.expression as? IrReturnableBlock)?.symbol
            else -> throw EvaluationException("Unexpected body type ${body}.")
        }
        val result = body.accept(this, context)
        if (result is Return && result.target == retSymbol)
            return result.value
        if (result is Unknown)
            return if (retSymbol != null) result - retSymbol else result
        return result
    }

    override fun visitExpressionBody(body: IrExpressionBody, data: Context): Any? {
        return body.expression.accept(this, data)
    }

    override fun visitBlockBody(body: IrBlockBody, data: Context): Any? {
        var i = 0
        val statements = body.statements
        while (i < statements.size) {
            var result = statements[i].accept(this, data)
            if (result is PossibleFlowChange) {
                for (j in i+1..<statements.size) {
                    data.removeAll(statements[j].assignedSymbols)
                    result += statements[j].flowChanges
                }
                return result
            }
            if (result is FlowChange)
                return result
            i++
        }
        return Unit
    }

    override fun visitReturn(expression: IrReturn, data: Context): Any? {
        val value = expression.value.accept(this, data)
        if (value is PossibleFlowChange)
            return value + Return(UnknownValue, expression.returnTargetSymbol, expression.value.type)
        return Return(value, expression.returnTargetSymbol, expression.value.type)
    }

    override fun visitBreak(jump: IrBreak, data: Context): Any? {
        return Break(jump.loop)
    }

    override fun visitContinue(jump: IrContinue, data: Context): Any? {
        return Continue(jump.loop)
    }

    override fun visitBlock(expression: IrBlock, data: Context): Any? {
        var result: Any? = Unit
        var i = 0
        val statements = expression.statements
        while (i < statements.size) {
            result = statements[i].accept(this, data)
            if (result is PossibleFlowChange) {
                for (j in i+1..<statements.size) {
                    data.removeAll(statements[j].assignedSymbols)
                    result += statements[j].flowChanges
                }
                return result
            }
            if (result is FlowChange)
                return result
            i++
        }
        return result
    }

    override fun visitFunctionExpression(expression: IrFunctionExpression, data: Context): Any? {
        return expression.function to data
    }

    override fun visitConst(expression: IrConst<*>, data: Context): Any? {
        return expression.value
    }

    override fun visitWhen(expression: IrWhen, data: Context): Any? {
        val branches = expression.branches
        for (i in 0..<branches.size) {
            val branch = branches[i]
            var result: Any? = null
            result = branch.condition.accept(this, data)
            if (result is Unknown) {
                data.removeAll(branches[i].result.assignedSymbols)
                result += branches[i].result.flowChanges
                for (j in i+1..<branches.size) {
                    data.removeAll(branches[i].condition.assignedSymbols)
                    result += branches[i].condition.flowChanges
                    data.removeAll(branches[i].result.assignedSymbols)
                    result += branches[i].result.flowChanges
                }
                return result
            }
            if (result is FlowChange) {
                return result
            }
            if (result == true) {
                return branch.result.accept(this, data)
            }
        }
        return Unit
    }

    override fun visitWhileLoop(loop: IrWhileLoop, data: Context): Any? {
        val body = loop.body
        while(true) {
            var condition = loop.condition.accept(this, data)
            if (condition is Unknown) {
                //We don't know if the condition or body will be executed again
                data.removeAll(loop.condition.assignedSymbols)
                condition += loop.condition.flowChanges
                if (body != null) {
                    data.removeAll(body.assignedSymbols)
                    condition += body.flowChanges
                }
                return condition
            }
            if (condition is FlowChange) return condition
            if (condition == false) break


            if (body != null) {
                var result = body.accept(this, data)
                if (result is PossibleFlowChange) {
                    result -= loop
                    data.removeAll(loop.condition.assignedSymbols)
                    result += loop.condition.flowChanges
                    data.removeAll(body.assignedSymbols)
                    result += body.flowChanges
                    return result
                }
                if (result is Continue && result.target == loop) continue
                if (result is Break && result.target == loop) break
                if (result is FlowChange) return result
            }

        }
        return Unit
    }

    override fun visitGetValue(expression: IrGetValue, data: Context): Any? {
        val name = expression.symbol.owner.name.asString()
        return data[expression.symbol]
    }

    override fun visitSetValue(expression: IrSetValue, data: Context): Any? {
        val value = expression.value.accept(this, data)
        if (value is Unknown) {
            data.remove(expression.symbol)
            return value
        }
        if (value is FlowChange)
            return value
        data[expression.symbol] = value
        return value
    }

    override fun visitGetField(expression: IrGetField, data: Context): Any? {
        //This will only be executed for singleton objects and top level properties
        return data[expression.symbol]
    }

    override fun visitSetField(expression: IrSetField, data: Context): Any? {
        //This will only be executed for singleton objects and top level properties
        val value = expression.value.accept(this, data)
        if (value is Unknown) {
            data.remove(expression.symbol)
            return value
        }
        if (value is FlowChange)
            return value
        data[expression.symbol] = value
        return value
    }

    override fun visitTypeOperator(expression: IrTypeOperatorCall, data: Context): Any? {
        val value = expression.argument.accept(this, data)
        if (value is FlowChange || value is Unknown) return value
        return when (expression.operator) {
            IrTypeOperator.IMPLICIT_COERCION_TO_UNIT -> Unit
            else -> UnknownValue
        }
    }

    /**
     * Returns the unknown result containing possible flow changes for this code segment.
     */
    // TODO: Cache the results
    val IrElement?.flowChanges get(): Unknown {
        if (this == null) return UnknownValue
        val innerReturnTargets = mutableSetOf<IrReturnTargetSymbol>()
        val innerLoops = mutableSetOf<IrLoop>()
        var result: Unknown = UnknownValue
        val visitor = object: IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
            override fun visitReturn(expression: IrReturn) {
                result += Return(UnknownValue, expression.returnTargetSymbol, expression.value.type)
                super.visitReturn(expression)
            }
            override fun visitContinue(jump: IrContinue) {
                result += Continue(jump.loop)
            }
            override fun visitBreak(jump: IrBreak) {
                result += Break(jump.loop)
            }
            override fun visitClass(declaration: IrClass) { }
            override fun visitFunction(declaration: IrFunction) {
                if (declaration.isLambda) {
                    super.visitFunction(declaration)
                }
            }
            override fun visitBlock(expression: IrBlock) {
                if (expression is IrReturnableBlock) innerReturnTargets.add(expression.symbol)
                super.visitBlock(expression)
            }
            override fun visitLoop(loop: IrLoop) {
                innerLoops.add(loop)
                super.visitLoop(loop)
            }
        }
        acceptVoid(visitor)
        return result - innerReturnTargets - innerLoops
    }

    private fun say(something: String) {
        messageCollector?.report(CompilerMessageSeverity.INFO, something)
    }
}