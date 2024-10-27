package com.aleksa.constants

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.assertEquals
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.SpecialNames
import org.junit.Ignore
import org.junit.Test

//@Ignore
class BasicTest {
    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Basic compilation`() {
        val program = """
fun main() {
  println(debug())
}

fun debug() = "Hello, World!"
        """
        val result = compile(
            sourceFile = SourceFile.kotlin("main.kt", program)
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}

//@Ignore
@OptIn(ExperimentalCompilerApi::class)
class UtilsTest {

    @Test
    fun `isLambda test`() {
        val visitor = object: IrElementVisitorVoid {
            val lambdas = mutableListOf<String>()
            val nonlambdas = mutableListOf<String>()
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
            override fun visitFunction(declaration: IrFunction) {
                if (declaration.isLambda)
                    lambdas.add(declaration.name.asString())
                else
                    nonlambdas.add(declaration.name.asString())
                super.visitFunction(declaration)
            }
        }
        val program = """
            fun main() {
                val lambda = {println("hello")}
                fun innerF() {
                    val innerLambda = {
                        println("inner")
                        fun innerInnerF() {
                        
                        }
                    }
                }
            }
        """.trimIndent()
        val result = compile(
            sourceFile = SourceFile.kotlin("main.kt", program),
            plugin = KotlinCompilerPluginRegistrar(visitors = listOf(visitor))
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals(visitor.lambdas.size, 2)
        assertEquals(visitor.nonlambdas, listOf("main", "innerF", "innerInnerF"))
    }

    @Test
    fun `assignedSymbols test`() {
        val visitor = object: IrElementVisitorVoid {
            val assigned = mutableMapOf<String, Set<String>>()
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
            override fun visitFunction(declaration: IrFunction) {
                assigned[declaration.name.asString()] = declaration.body!!.assignedSymbols.map { it.name!! }.toSet()
                super.visitFunction(declaration)
            }
        }
        val program = """
            fun main() {
                var a = 10
                var b = 2
                var c = 1
                fun innerF() {
                    a = 4
                    println(c)
                    val lambda = {
                        b = 2
                        println(c)
                    }
                    fun innerInnerF() {
                        innerF()
                    }
                }
            }
        """.trimIndent()
        val result = compile(
            sourceFile = SourceFile.kotlin(name = "main.kt", program),
            plugin = KotlinCompilerPluginRegistrar(visitors = listOf(visitor))
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals(setOf("a", "b", "c"), visitor.assigned["main"])
        assertEquals(setOf("a", "lambda"), visitor.assigned["innerF"])
        assertEquals(setOf("a", "lambda"), visitor.assigned["innerInnerF"]) //hm, this is actually semi incorrect, for 'lambda' is actually assigned within a new stack context
        assertEquals(setOf("b"), visitor.assigned[SpecialNames.ANONYMOUS_STRING])
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `isStack and isGlobalStatic test`() {
        val visitor = object: IrElementVisitorVoid {
            val stackVars = mutableListOf<String>()
            val staticVars = mutableListOf<String>()
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
            override fun visitVariable(declaration: IrVariable) {
                if (declaration.isStack)
                    stackVars.add(declaration.name.asString())
                super.visitVariable(declaration)
            }

            override fun visitValueParameter(declaration: IrValueParameter) {
                if (declaration.name.asString() == "param" && declaration.isStack)
                    stackVars.add(declaration.name.asString())
                super.visitValueParameter(declaration)
            }
            override fun visitField(declaration: IrField) {
                if (declaration.isStaticFinal)
                    staticVars.add(declaration.name.asString())
                super.visitField(declaration)
            }
        }
        val program = """
            val a: Int = 1
                get() = field + 1
            var b: Int = 2
            object A {
                val c: Int = 3
                var d: Int = 4
            }
            fun main(param: String) {
                val e = 5
                var f = 6
                val lambda = {
                    println(e)
                    println(f)
                }
                var g = 7
                var h = 8
                var i = 9
                fun localF() {
                    g = g + 1
                    val lambda2 = {
                        h = h + 1
                        fun localG() {
                            i = i + 1
                        }
                    }
                }
            }
        """.trimIndent()
        val result = compile(
            sourceFile = SourceFile.kotlin("main.kt", program),
            plugin = KotlinCompilerPluginRegistrar(visitors = listOf(visitor))
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals(listOf("param", "e", "f", "lambda", "g", "lambda2"), visitor.stackVars)
        assertEquals(listOf("a", "c"), visitor.staticVars)
    }

    @Test
    fun `Method matching test`() {
        val doublePlusInt = processPrimitive("plus", listOf(1.4, 2))
        assertEquals(3.4, doublePlusInt)
        val doubleToInt = processPrimitive("toInt", listOf(1.7))
        assertEquals(1, doubleToInt)
    }
}

