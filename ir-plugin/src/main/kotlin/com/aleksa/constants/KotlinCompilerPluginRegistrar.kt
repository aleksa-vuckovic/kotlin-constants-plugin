package com.aleksa.constants

import com.google.auto.service.AutoService
import com.aleksa.constants.KotlinIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class KotlinCompilerPluginRegistrar(
    private val defaultString: String,
    private val defaultFile: String,
    override val supportsK2: Boolean
) : CompilerPluginRegistrar() {

    @Suppress("unused") // Used by service loader
    constructor() : this(
        defaultString = "Hello, World!",
        defaultFile = "file.txt",
        supportsK2 = true
    )

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        val string = configuration.get(KotlinCommandLineProcessor.ARG_STRING, defaultString)
        val file = configuration.get(KotlinCommandLineProcessor.ARG_FILE, defaultFile)
        IrGenerationExtension.registerExtension(KotlinIrGenerationExtension(messageCollector, string, file))
    }
    /*
    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        val string = configuration.get(KotlinCommandLineProcessor.ARG_STRING, defaultString)
        val file = configuration.get(KotlinCommandLineProcessor.ARG_FILE, defaultFile)
        IrGenerationExtension.registerExtension(project, KotlinIrGenerationExtension(messageCollector, string, file))
    }*/
}