package com.aleksa.constants

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.util.dump

@OptIn(ExperimentalCompilerApi::class)
fun compile(
    sourceFiles: List<SourceFile>,
    plugin: CompilerPluginRegistrar = KotlinCompilerPluginRegistrar(),
): KotlinCompilation.Result {
    return KotlinCompilation().apply {
        sources = sourceFiles
        compilerPluginRegistrars = listOf(plugin)
        inheritClassPath = true
    }.compile()
}

@OptIn(ExperimentalCompilerApi::class)
fun compile(
    sourceFile: SourceFile,
    plugin: CompilerPluginRegistrar = KotlinCompilerPluginRegistrar(),
): KotlinCompilation.Result {
    return compile(listOf(sourceFile), plugin)
}

class StringCollector: MessageCollector {
    private val text = StringBuilder()
    val output: String get() = text.toString()
    override fun clear() {
        text.clear()
    }
    override fun hasErrors(): Boolean {
        return false
    }
    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageSourceLocation?) {
        text.append("\n" + message)
    }
}

fun String.trimm(): String {
    return lineSequence()
        .map { it.trimStart() }
        .filter { it.isNotBlank() }
        .joinToString("\n")
}

fun List<IrStatement>.dump(vararg indices: Any): String {
    val result = StringBuilder()
    for (index in indices) {
        if (index is Int)
            result.append(this[index].dump()).append("\n")
        if (index is IntRange) {
            for (i in index)
                result.append(this[i].dump()).append("\n")
        }
    }
    return result.toString()
}