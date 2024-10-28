package com.aleksa.constants

import com.google.auto.service.AutoService
import com.aleksa.constants.KotlinIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class KotlinCompilerPluginRegistrar(
    val visitors: List<IrElementVisitorVoid>? = null,
    override val supportsK2: Boolean = true
) : CompilerPluginRegistrar() {

    @Suppress("unused") // Used by service loader
    constructor() : this(null, true)

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        IrGenerationExtension.registerExtension(KotlinIrGenerationExtension(messageCollector, visitors))
    }
}