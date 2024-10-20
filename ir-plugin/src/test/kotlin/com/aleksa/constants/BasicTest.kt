package com.aleksa.constants

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import kotlin.test.assertEquals
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test

class BasicTest {
    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `IR plugin success`() {
        val result = compile(
            sourceFile = SourceFile.kotlin(
                "main.kt", """
fun main() {
  println(debug())
}

fun debug() = "Hello, World!"
"""
            )
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}

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