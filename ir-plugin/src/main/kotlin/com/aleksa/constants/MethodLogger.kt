package com.aleksa.constants;

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockBodyImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class MethodLogger(
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