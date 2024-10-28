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
                it.transform(Replacer(), Context())
                //say(it.dump())
            }

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
}