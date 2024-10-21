package com.aleksa.constants

import org.jetbrains.kotlin.backend.common.extensions.*
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.ir.*
import org.jetbrains.kotlin.ir.backend.js.lower.boxParameter
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFunctionImpl
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.*
import org.jetbrains.kotlin.name.*

class KotlinIrGenerationExtension(
    private val messageCollector: MessageCollector,
    private val string: String,
    private val file: String
) : IrGenerationExtension {
    private fun say(something: String) {
        messageCollector.report(CompilerMessageSeverity.WARNING, something)
    }
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.files.forEach {
            it.accept(LogMethodVisitor(pluginContext) {
                "Starting method ${it.name}" to "Ending method ${it.name}"
            }, null)
        }
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
}

