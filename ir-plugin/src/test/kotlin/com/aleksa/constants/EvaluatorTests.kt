package com.aleksa.constants

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.kapt3.util.MessageCollectorBackedWriter
import org.jetbrains.kotlin.name.SpecialNames
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals


@Ignore
class EvaluatorTests {

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Simple constant expression test`() {
        val visitor = object: IrElementVisitorVoid {
            var result: Any? = null
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
            override fun visitVariable(declaration: IrVariable) {
                if (declaration.name.asString() == "a") {
                    result = declaration.initializer!!.accept(Evaluator(), Context.forMethod(declaration.parent as IrFunction))
                }
            }
        }
        val program = """
            fun main() {
                val a = ((10 + 3) / 5 * 10.6).toInt()
            }
        """.trimIndent()
        val result = compile(
            sourceFile = SourceFile.kotlin(name = "main.kt", program),
            plugin = KotlinCompilerPluginRegistrar(visitors = listOf(visitor))
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals(21, visitor.result)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Expression in method with constant local variables`() {
        val visitor = object: IrElementVisitorVoid {
            var result: Any? = null
            var method: IrFunction? = null
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
            override fun visitFunction(declaration: IrFunction) {
                if (declaration.name.asString() == "main") method = declaration
                super.visitFunction(declaration)
            }

            override fun visitCall(expression: IrCall) {
                if (expression.symbol.owner.name.asString() == "f")
                    result = expression.accept(Evaluator(), Context.forMethod(method!!))
            }
        }
        val program = """
            fun f(): Double {
                val a = 10.2
                return a*2
            }
            fun main() {
                val x = f()
            }
        """.trimIndent()
        val result = compile(
            sourceFile = SourceFile.kotlin(name = "main.kt", program),
            plugin = KotlinCompilerPluginRegistrar(visitors = listOf(visitor))
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals(20.4, visitor.result)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Comparison test`() {
        val visitor = object: IrElementVisitorVoid {
            var result: Any? = null
            var method: IrFunction? = null
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
            override fun visitFunction(declaration: IrFunction) {
                if (declaration.name.asString() == "main") method = declaration
                super.visitFunction(declaration)
            }
            override fun visitVariable(declaration: IrVariable) {
                if (declaration.name.asString() == "y")
                    result = declaration.initializer!!.accept(Evaluator(), Context.forMethod(method!!))
                super.visitVariable(declaration)
            }
        }
        val program = """
            fun f(): Boolean {
                val x = 2
                return x == 2
            }
            fun main() {
                val y = f()
            }
        """.trimIndent()
        val result = compile(
            sourceFile = SourceFile.kotlin(name = "main.kt", program),
            plugin = KotlinCompilerPluginRegistrar(visitors = listOf(visitor))
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals(true, visitor.result)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Invocation with parameters, recursion and if`() {
        val visitor = object: IrElementVisitorVoid {
            var result: Any? = null
            var method: IrFunction? = null
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
            override fun visitFunction(declaration: IrFunction) {
                if (declaration.name.asString() == "main") method = declaration
                super.visitFunction(declaration)
            }
            override fun visitCall(expression: IrCall) {
                if (method != null && expression.symbol.owner.name.asString() == "fib")
                    result = expression.accept(Evaluator(), Context.forMethod(method!!))
            }
        }
        val program = """
            fun fib(n: Int): Int {
                if (n == 0 || n == 1)
                    return 1
                else
                    return fib(n-1) + fib(n-2)
            }
            fun main() {
                val x = fib(10)
            }
        """.trimIndent()
        val result = compile(
            sourceFile = SourceFile.kotlin(name = "main.kt", program),
            plugin = KotlinCompilerPluginRegistrar(visitors = listOf(visitor))
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals(89, visitor.result)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Simple loop`() {
        val visitor = object: IrElementVisitorVoid {
            var result: Any? = null
            var method: IrFunction? = null
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
            override fun visitFunction(declaration: IrFunction) {
                if (declaration.name.asString() == "main") {
                    method = declaration
                    super.visitFunction(declaration)
                }
            }
            override fun visitCall(expression: IrCall) {
                if (expression.symbol.owner.name.asString() == "fac")
                    result = expression.accept(Evaluator(), Context.forMethod(method!!))
            }
        }
        val program = """
            fun fac(n: Int): Int {
                var i = 1
                var result = 1
                while(i < n) result *= i++
                return result
            }
            fun main() {
                val x = fac(5)
            }
        """.trimIndent()
        val result = compile(
            sourceFile = SourceFile.kotlin(name = "main.kt", program),
            plugin = KotlinCompilerPluginRegistrar(visitors = listOf(visitor))
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals(24, visitor.result)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Multiple local variables and unknown variables`() {
        val visitor = object: IrElementVisitorVoid {
            var result: Any? = null
            var method: IrFunction? = null
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
            override fun visitFunction(declaration: IrFunction) {
                if (declaration.name.asString() == "main") {
                    method = declaration
                    super.visitFunction(declaration)
                }
            }
            override fun visitCall(expression: IrCall) {
                if (expression.symbol.owner.name.asString() == "f")
                    result = expression.accept(Evaluator(), Context.forMethod(method!!))
            }
        }
        val program = """
            var state = 1
            fun f(x: Int, y: Float): Float {
                var result: Float
                when(x) {
                    1 -> result = 100f
                    2 -> result = 200f
                    else -> result = 300f
                }
                result = result * y + 1
                var msg: String = ""
                when(state) {
                    0 -> msg = "Ok"
                    else -> msg = "Warning"
                }
                println("Message " + msg)
                return result
            }
            fun main() {
                val x = f(2, 1.5f)
            }
        """.trimIndent()
        val result = compile(
            sourceFile = SourceFile.kotlin(name = "main.kt", program),
            plugin = KotlinCompilerPluginRegistrar(visitors = listOf(visitor))
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals(301f, visitor.result)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Lambdas and local methods`() {
        val visitor = object: IrElementVisitorVoid {
            var result: Any? = null
            var method: IrFunction? = null
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
            override fun visitFunction(declaration: IrFunction) {
                if (declaration.name.asString() == "main") {
                    method = declaration
                    super.visitFunction(declaration)
                }
            }
            override fun visitCall(expression: IrCall) {
                if (expression.symbol.owner.name.asString() == "pow")
                    result = expression.accept(Evaluator(), Context.forMethod(method!!))
            }
        }
        val program = """
            var state = 1
            fun pow(base: Float, power: Int): Float {
                var x = 1f
                var i = 0
                fun incI() {
                    i++
                }
                val lamb = {
                    x * base
                }
                while(i < power) {
                    x = lamb()
                    incI()
                }
                return x
            }
            fun main() {
                val x = pow(2.5f, 3)
            }
        """.trimIndent()
        val result = compile(
            sourceFile = SourceFile.kotlin(name = "main.kt", program),
            plugin = KotlinCompilerPluginRegistrar(visitors = listOf(visitor))
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals(15.625f, visitor.result)
    }


    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Nonlocal return and break`() {
        val visitor = object: IrElementVisitorVoid {
            var result1: Any? = null
            var result2: Any? = null
            var method: IrFunction? = null
            val collector = StringCollector()
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
            override fun visitFunction(declaration: IrFunction) {
                if (declaration.name.asString() == "main") {
                    method = declaration
                    super.visitFunction(declaration)
                }
            }
            override fun visitVariable(declaration: IrVariable) {
                val result = declaration.initializer!!.accept(Evaluator(messageCollector = collector), Context.forMethod(method!!))
                if (declaration.name.asString() == "result1") result1 = result
                else result2 = result
            }
        }
        val program = """
            inline fun combine(a: Int, b: Int, c: Int, how: (Int, Int) -> Int): Int {
                var result = how(a,b)
                result = how(result, c)
                return result
            }
            fun f(start: Int, count: Int): Int {
                var i = 0
                var a = 0
                var b = 0
                var c = start
                while(i < count) {
                    val tmp = combine(a,b,c) { x,y ->
                        if (x == 10)
                            return 2000
                        x + y
                    }
                    a = b
                    b = c
                    c = tmp
                    if (c == 13)
                        break
                }
                return c
            }
            fun main() {
                val result1 = f(5, 100)
                val result2 = f(1, 100)
            }
        """.trimIndent()
        val result = compile(
            sourceFile = SourceFile.kotlin(name = "main.kt", program),
            plugin = KotlinCompilerPluginRegistrar(visitors = listOf(visitor))
        )
        println(visitor.collector.output)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals(2000, visitor.result1)
        assertEquals(13, visitor.result2)
    }
}