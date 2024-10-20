package com.aleksa.constants

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.DumpIrTreeOptions
import org.jetbrains.kotlin.ir.util.dump

class KotlinIrGenerationExtension(
    private val messageCollector: MessageCollector,
    private val string: String,
    private val file: String
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        messageCollector.report(CompilerMessageSeverity.WARNING, "Argument 'string' = $string")
        messageCollector.report(CompilerMessageSeverity.INFO, "Argument 'file' = $file")
        messageCollector.report(CompilerMessageSeverity.WARNING, "***********************************************")
        messageCollector.report(CompilerMessageSeverity.WARNING, moduleFragment.dump(DumpIrTreeOptions()))
        messageCollector.report(CompilerMessageSeverity.WARNING, "******************************************************")


    }
}