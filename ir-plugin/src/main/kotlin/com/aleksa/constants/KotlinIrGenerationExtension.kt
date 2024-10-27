package com.aleksa.constants

import org.jetbrains.kotlin.backend.common.extensions.*
import org.jetbrains.kotlin.backend.jvm.ir.isInlineFunctionCall
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.ir.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.*
import org.jetbrains.kotlin.name.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberExtensionProperties
import kotlin.reflect.full.memberProperties

class KotlinIrGenerationExtension(
    private val messageCollector: MessageCollector,
    private val visitors: List<IrElementVisitorVoid>? = null
) : IrGenerationExtension {
    private fun say(something: String) {
        messageCollector.report(CompilerMessageSeverity.WARNING, something)
    }
    private fun warn(something: String) {
        messageCollector.report(CompilerMessageSeverity.WARNING, something)
    }

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.files.forEach {
            /*it.accept(LogMethodVisitor(pluginContext) {
                "Starting method ${it.name}" to "Ending method ${it.name}"
            }, null)*/
            if (visitors != null) {
                visitors.forEach { visitor ->
                    val pluginContextProperty = visitor::class.memberProperties
                        .find { it.name == "pluginContext" && it is KMutableProperty<*> } as? KMutableProperty<*>
                    pluginContextProperty?.setter?.call(visitor, pluginContext)
                    it.acceptVoid(visitor)
                }
            }
            else {
                it.acceptVoid(DumpVisitor {
                    //it is IrFunction && it.name.asString() == "main"
                    it is IrFile
                })
                it.acceptVoid(TestVisitor())
            }

        }
    }

    /**
     * Visit static const value declarations, *try* to evaluate their initializers and replace
     * them with a constant. This includes top level declarations and declarations in nonlocal object expressions,
     * in which case initializer blocks are also taken into account
     */
    inner class StaticConstVisitor(
        private val pluginContext: IrPluginContext,
        private val shouldEvaluate: (IrFunction) -> Boolean = {true}
    ): IrElementVisitorVoid {
        val context = Context(null, null, mutable = true)

        /*override fun visitProperty(declaration: IrProperty) {
            val clazz = declaration.parent as? IrClass
            val field = declaration.backingField
            val initializer = field?.initializer
            if (!declaration.isLocal && declaration.isConst && (clazz?.isObject != false) && field != null && initializer != null)
                try {
                    say("Attempting to evaluate initializer for ${declaration.name}.")
                    val value = evaluate(initializer.expression, context.asImmutable())
                    context[declaration.symbol] = value
                    val result = createIrConst(value, initializer)
                    if (result == null)
                            warn("Could not evaluate ${declaration.name} - The return type is not primitive.")
                    else
                        initializer.expression = result
                } catch(ex: EvaluationException) {
                    warn("Could not evaluate ${declaration.name}. ${ex.message}")
                } catch(ex: Context.UnknownValueException) {
                    warn("Could not evaluate ${declaration.name}. The value of ${ex.symbol.name} is unknown at compile time.")
                } catch(ex: Context.ImmutabilityException) {
                    warn("Evaluation of ${declaration.name} failed because evaluated expressions must not have side effects. ${ex.message}")
                }
        }*/
    }

    inner class LogMethodVisitor(
        private val pluginContext: IrPluginContext,
        private val msgFactory: (IrFunction) -> Pair<String, String>?
    ): IrElementVisitorVoid {

        override fun visitElement(element: IrElement) {
            element.acceptChildrenVoid(this)
        }

        override fun visitFunction(declaration: IrFunction) {
            //visit children in case there are local nested functions, lambdas or class definitions
            super.visitFunction(declaration)
            //change the function body
            val body = declaration.body ?: return super.visitFunction(declaration)
            val msg = msgFactory(declaration) ?: return super.visitFunction(declaration)
            val printlnFunction = pluginContext.referenceFunctions(CallableId(FqName("kotlin.io"), Name.identifier("println"))).first {
                val params = it.owner.valueParameters
                params.size == 1 && params.first().type == pluginContext.irBuiltIns.anyType.makeNullable()
            }

            val createCall: (String) -> IrCallImpl = {
                IrCallImpl(
                    startOffset = body.startOffset,
                    endOffset = body.startOffset,
                    type = pluginContext.irBuiltIns.unitType,
                    symbol = printlnFunction,
                    typeArgumentsCount = 0,
                    valueArgumentsCount = 1
                ).apply {
                    putValueArgument(0, IrConstImpl.string(
                        startOffset = body.startOffset,
                        endOffset = body.startOffset,
                        type = pluginContext.irBuiltIns.stringType,
                        value = it
                    ))
                }
            }

            val statements = body.statements
            val newBody = IrBlockBodyImpl(
                startOffset = body.startOffset,
                endOffset = body.endOffset,
                statements = listOf(createCall(msg.first)) + statements + listOf(createCall(msg.second))
            )
            declaration.body = newBody
            return super.visitFunction(declaration)
        }
    }

    inner class DumpVisitor(
        private val predicate: (IrElement) -> Boolean
    ): IrElementVisitorVoid {
        override fun visitElement(element: IrElement) {
            element.acceptChildrenVoid(this)
            if (predicate(element)) {
                say("********************************************")
                say("********** DUMP: ${element} ****************")
                say(element.dump())
                say("--------------------------------------------")
            }
        }
    }

    inner class TestVisitor(

    ): IrElementVisitorVoid {
        override fun visitElement(element: IrElement) {
            element.acceptChildrenVoid(this)
        }

        override fun visitFunction(declaration: IrFunction) {
            if (declaration.name.asString() != "hehe")
                return
            say("---------------")
            for (param in declaration.allParameters) {
                say("Param ${param.name} index ${param.index} symbol ${param.symbol} ${param.origin}")
            }
            say("---------------")
        }
        override fun visitCall(expression: IrCall) {
            val func = expression.symbol.owner
            say("Visit call of ${func.name} (${func.kotlinFqName}) with body ${func.body} receiver ${func.dispatchReceiverParameter} and is operator ${func.isOperator} and is infix ${func.isInfix}")
            super.visitCall(expression)
        }
    }
}