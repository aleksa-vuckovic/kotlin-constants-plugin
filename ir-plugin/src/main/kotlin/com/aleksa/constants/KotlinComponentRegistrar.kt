package org.example.com.aleksa.constants

import com.aleksa.constants.KotlinCommandLineProcessor
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class KotlinComponentRegistrar(
    private val defaultString: String,
    private val defaultFile: String
) : ComponentRegistrar {

    @Suppress("unused") // Used by service loader
    constructor() : this(
        defaultString = "Hello, World!",
        defaultFile = "file.txt"
    )

    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        val string = configuration.get(KotlinCommandLineProcessor.ARG_STRING, defaultString)
        val file = configuration.get(KotlinCommandLineProcessor.ARG_FILE, defaultFile)
        IrGenerationExtension.registerExtension(project, KotlinIrGenerationExtension(messageCollector, string, file))
    }
}