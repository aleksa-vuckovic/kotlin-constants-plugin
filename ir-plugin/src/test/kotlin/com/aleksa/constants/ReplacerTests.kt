package com.aleksa.constants

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.junit.Test
import kotlin.test.assertEquals

class ReplacerTests {

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Simple constant arithmetic`() {
        val visitor = object: IrElementVisitorVoid {
            var result: String = ""
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
            override fun visitFunction(declaration: IrFunction) {
                if (declaration.name.asString() == "main") {
                    declaration.body!!.transform(Replacer(), Context.forMethod(declaration))
                }
                super.visitFunction(declaration)
            }
            override fun visitVariable(declaration: IrVariable) {
                if (declaration.name.asString() == "x") result = declaration.dump()
            }
        }
        val program = """
            fun main() {
                val x = (10f + 3) / 2
            }
        """.trimIndent()
        val result = compile(
            sourceFile = SourceFile.kotlin(name = "main.kt", program),
            plugin = KotlinCompilerPluginRegistrar(visitors = listOf(visitor))
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals("""
VAR name:x type:kotlin.Float [val]
 CONST Float type=kotlin.Float value=6.5
""".trimm(), visitor.result.trimm())
    }
}