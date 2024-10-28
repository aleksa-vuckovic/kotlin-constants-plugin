package com.aleksa.constants

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrBreakImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrContinueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.util.isTopLevel
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer

/**
 * Simplify the given element by replacing parts that can be calculated
 * at compile time with an IrConst node and return the modified IrExpression.
 * The context passed as argument should be appropriate for the actual context of the statement,
 * and it should contain variable values that are always equal to the give values when the statement is executed,
 * otherwise the replacement can result in nonequivalent code.
 * The context is not changed, and will be copied if necessary.
 */
class Replacer(
    private val evaluator: Evaluator = Evaluator()
): IrElementTransformer<Context> {
    var pluginContext: IrPluginContext? = null
    private val strictEvaluator = Evaluator(throwOnUnevaluatedMethod = true, shouldEvaluate = evaluator.shouldEvaluate)
    private val root = Context()

    override fun visitElement(element: IrElement, data: Context): IrElement {
        return super.visitElement(element, data)
    }

    override fun visitBlockBody(body: IrBlockBody, data: Context): IrBody {
        val statements = body.statements
        val context = data.copy()
        var i = 0
        while(i < statements.size) {
            statements[i] = statements[i].transform(this, context) as IrStatement
            val result = statements[i].accept(evaluator, context)
            if (statements[i] is IrConst<*>) {
                //remove statement
                statements.removeAt(i)
            }
            else if (result is Evaluator.FlowChange) {
                //we can remove the remaining statements
                for (j in i+1..<statements.size) statements.removeLast()
                return body
            }
            else i++
        }
        return body
    }

    override fun visitBlock(expression: IrBlock, data: Context): IrExpression {
        val statements = expression.statements
        val context = data.copy()
        var i = 0
        while(i < statements.size) {
            statements[i] = statements[i].transform(this, context) as IrStatement
            val result = statements[i].accept(evaluator, context)
            if (statements[i] is IrConst<*> && i != statements.size - 1)
                statements.removeAt(i)
            else if (result is Evaluator.FlowChange) {
                //we can remove the remaining statements
                for (j in i+1..<statements.size) statements.removeLast()
                return expression
            }
            else i++
        }
        return expression
    }

    override fun visitExpressionBody(body: IrExpressionBody, data: Context): IrBody {
        body.expression = body.expression.transform(this, data)
        return body
    }

    override fun visitCall(expression: IrCall, data: Context): IrElement {
        expression.transformChildren(this, data) //replace parameter expressions
        val context = data.copy()
        try {
            val result = expression.accept(strictEvaluator, context.asImmutable())
            if (result is Evaluator.Unknown) return expression
            if (result is Evaluator.FlowChange)
                return createFlowChangeExpression(result, expression) ?: expression
            return assertIrConst(result, expression)
        } //Only replace call if it has 0 side effects
        catch(_: Context.ImmutabilityException) {}
        catch(_: Evaluator.UnevaluatedMethodException) {}
        catch(_: Evaluator.EvaluationException) {}
        catch(_: InvalidIrConstValue) {}
        return expression
    }

    override fun visitReturn(expression: IrReturn, data: Context): IrExpression {
        expression.transformChildren(this, data)
        return expression
    }

    override fun visitWhen(expression: IrWhen, data: Context): IrExpression {
        val context = data.copy()
        val branches = expression.branches
        var hasUnknownConditions = false
        var i = 0
        while(i < branches.size)  {
            branches[i].condition = branches[i].condition.transform(this, context)
            val result = branches[i].condition.accept(evaluator, context)
            if (result is Evaluator.FlowChange) {
                for (j in i+1..<branches.size) branches.removeLast()
                branches[i].result = branches[i].result.transform(this, context)
                return expression
                //???
            }
            else if (result is Evaluator.Unknown) {
                branches[i].result = branches[i].result.transform(this, context)
                context.removeAll(branches[i].assignedSymbols)
                i++
                hasUnknownConditions = true
                //We don't know if this will execute, so delete all symbols assigned here
            }
            else if (result == false) {
                if (branches[i].condition is IrConst<*>)
                    //The branch is always false and the condition has no side effects, so it can be safely removed
                    branches.removeAt(i)
                else {
                    //The branch is always false, but condition has side effects so replace the result with ?what?
                    branches[i].result = branches[i].result.transform(this, context)
                    i++
                }
            }
            else if (result == true) {
                //The branch is always true, so cut off the remaining ones
                for (j in i+1..<branches.size) branches.removeLast()
                branches[i].result = branches[i].result.transform(this, context)
                //If there was an unknown condition before, return the entire conditional expression
                //otherwise we can safely replace this with just the result of this branch
                if (hasUnknownConditions) return expression
                else return branches[i].result
            }
            else throw Exception("Unexpected result in visitWhen ${result}")
        }
        return expression
    }

    override fun visitWhileLoop(loop: IrWhileLoop, data: Context): IrExpression {
        val context = data.copy()
        val body = loop.body
        context.removeAll(loop.condition.assignedSymbols)
        if (body != null) context.removeAll(body.assignedSymbols)
        loop.condition = loop.condition.transform(this, context)
        if (body != null) loop.body = body.transform(this, context)
        // TODO: Maybe check if the loop can be removed altogether
        return loop
    }

    override fun visitGetValue(expression: IrGetValue, data: Context): IrExpression {
        return createIrConst(data[expression.symbol], expression) ?: expression
    }

    override fun visitSetValue(expression: IrSetValue, data: Context): IrExpression {
        val context = data.copy()
        expression.value = expression.value.transform(this, context)
        try {
            val result = expression.value.accept(strictEvaluator, context)
            if (result is Evaluator.FlowChange)
                return createFlowChangeExpression(result, expression) ?: expression
        } catch(_: Exception) {}
        return expression
    }

    override fun visitGetField(expression: IrGetField, data: Context): IrExpression {
        return createIrConst(data[expression.symbol], expression) ?: expression
    }

    override fun visitSetField(expression: IrSetField, data: Context): IrExpression {
        val context = data.copy()
        if (expression.receiver != null) {
            expression.receiver = expression.receiver!!.transform(this, context)
            try {
                val result = expression.receiver!!.accept(strictEvaluator, context.asImmutable())
                if (result is Evaluator.FlowChange)
                    return createFlowChangeExpression(result, expression) ?: expression
            } catch(_: Exception) {}
            expression.receiver!!.accept(evaluator, context)
        }
        expression.value = expression.value.transform(this, context)
        try {
            val result = expression.value.accept(strictEvaluator, context.asImmutable())
            if (result is Evaluator.FlowChange)
                return createFlowChangeExpression(result, expression) ?: expression
        } catch (_: Exception) {}
        return expression
    }

    override fun visitTypeOperator(expression: IrTypeOperatorCall, data: Context): IrExpression {
        val context = data.copy()
        expression.argument = expression.argument.transform(this, context)
        try {
            val value = expression.accept(strictEvaluator, context.asImmutable())
            if (value is Evaluator.FlowChange)
                return createFlowChangeExpression(value, expression) ?: expression
            else return createIrConst(value, expression) ?: expression
        }
        catch (_: Exception) { }
        return expression
    }

    override fun visitFunction(declaration: IrFunction, data: Context): IrStatement {
        val context = Context(declaration, data.copy().apply { removeVars() })
        declaration.transformChildren(this, context)
        return declaration
    }

    override fun visitValueParameter(declaration: IrValueParameter, data: Context): IrStatement {
        declaration.transformChildren(this, data)
        return declaration
    }
    override fun visitProperty(declaration: IrProperty, data: Context): IrStatement {
        declaration.transformChildren(this, data)
        val field = declaration.backingField ?: return declaration
        if (field.isStaticFinal && field.initializer != null)
            root[field.symbol] = field.initializer!!.accept(evaluator, root)
        return declaration
    }
    override fun visitField(declaration: IrField, data: Context): IrStatement {
        declaration.transformChildren(this, data)
        return declaration
    }

    override fun visitClass(declaration: IrClass, data: Context): IrStatement {
        declaration.transformChildren(this, data)
        return declaration
    }

    override fun visitFile(declaration: IrFile, data: Context): IrFile {
        //ignoring data because we want to use the same root for all files?
        declaration.transformChildren(this, root)
        return declaration
    }

    fun createFlowChangeExpression(change: Evaluator.FlowChange, basedOn: IrExpression): IrExpression? {
        val s = basedOn.startOffset
        val e = basedOn.endOffset
        val n = pluginContext!!.irBuiltIns.nothingType
        if (change is Evaluator.Return) {
            val ret = createIrConst(change.value, basedOn) ?: return null
            return IrReturnImpl(s, e, n, change.target, ret)
        }
        if (change is Evaluator.Continue)
            return IrContinueImpl(s, e, n, change.target)
        if (change is Evaluator.Break)
            return IrBreakImpl(s, e, n, change.target)
        return null
    }

    private fun say(something: String) {
        evaluator.messageCollector?.report(CompilerMessageSeverity.INFO, something)
    }
}