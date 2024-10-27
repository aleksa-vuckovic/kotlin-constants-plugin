package com.aleksa.constants

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

//@Ignore
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

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Simple method invocation`() {
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
            fun calc(x: Int, y: Int): Int {
                return x + y * 2
            }
            fun main() {
                val x = calc(1,2) + calc(2,3)*4 //5 + 32 = 37
            }
        """.trimIndent()
        val result = compile(
            sourceFile = SourceFile.kotlin(name = "main.kt", program),
            plugin = KotlinCompilerPluginRegistrar(visitors = listOf(visitor))
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals("""
            VAR name:x type:kotlin.Int [val]
                CONST Int type=kotlin.Int value=37
        """.trimm(), visitor.result.trimm())
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Lambda and side effects`() {
        val visitor = object: IrElementVisitorVoid {
            lateinit var value1: IrVariable
            lateinit var value2: IrVariable
            lateinit var ret: IrReturn
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
            override fun visitFunction(declaration: IrFunction) {
                if (declaration.name.asString() == "main") {
                    declaration.body!!.transform(Replacer(), Context.forMethod(declaration))
                    val statements = (declaration.body as IrBlockBody).statements
                    value1 = statements[1] as IrVariable
                    value2 = statements[2] as IrVariable
                    ret = statements[3] as IrReturn
                }
            }
        }
        val program = """
            fun main(): Int {
                val lambda = { x: Int, y: Int, log: Boolean -> 
                    if (log)
                        println("Invoked lambda")
                    x + y
                }
                val value1 = lambda(1,2,false)
                val value2 = lambda(3,4,true)
                return value1+value2
            }
        """.trimIndent()
        val result = compile(
            sourceFile = SourceFile.kotlin(name = "main.kt", program),
            plugin = KotlinCompilerPluginRegistrar(visitors = listOf(visitor))
        )
        /*
        * Expect the initializer of value1 to be replaced with constant 3.
        * Expect the initializer of value2 to not be replaced with a constant because the invocation has side effects.
        * Expect the return statement value to be replaced with 10.
        */
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        val value1 = visitor.value1.initializer!!
        assertIs<IrConst<*>>(value1)
        assertEquals(3, value1.value)
        val value2 = visitor.value2.initializer!!
        assertIs<IrCall>(value2)
        val ret = visitor.ret.value
        assertIs<IrConst<*>>(ret)
        assertEquals(10, ret.value)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `When and while`() {
        val visitor = object: IrElementVisitorVoid {
            lateinit var evalFElse: IrExpression
            lateinit var mainReturn: IrExpression
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
            override fun visitFile(declaration: IrFile) {
                declaration.transform(Replacer(), Context())
                super.visitFile(declaration)
            }
            override fun visitFunction(declaration: IrFunction) {
                if (declaration.name.asString() == "evalF") {
                    val ret = declaration.body!!.statements.last() as IrReturn
                    evalFElse =  ((ret.value as IrBlock).statements.last() as IrWhen).branches.last().result
                }
                else if (declaration.name.asString() == "main") {
                    mainReturn = (declaration.body!!.statements.last() as IrReturn).value
                }
            }
        }
        val program = """
            fun evalAbs(x: Double): Double {
                if (x < 0)
                    return -x
                return x
            }
            fun evalSqrt(x: Double): Double {
                var result = x/2
                while(true) {
                    val newresult = (result + x/result)/2
                    val err = evalAbs(newresult - result)
                    if (err < 1e-10) return newresult
                    result = newresult
                }
                return result
            }
            fun evalF(value: Double, type: Int): Double {
                var i = 0
                return when(type) {
                    0 -> value
                    1 -> value / 2
                    2 -> value * value
                    3 -> evalSqrt(value)
                    else -> evalSqrt(3600.0)
                }
            }
            fun main(): Double {
                var i = 0
                var result = 0.0
                while (true) {
                    result = evalF(10.0, i++)
                    if (result > 50.0 && result < 70.0)
                        break
                }
                return result
            }
        """.trimIndent()
        val result = compile(
            sourceFile = SourceFile.kotlin(name = "main.kt", program),
            plugin = KotlinCompilerPluginRegistrar(visitors = listOf(visitor))
        )
        /*
        * Expect the else branch in evalSqrt to be replaced with 60.
        * Expect the main return value to be replaced with 60.
        */
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        val evalFElse = visitor.evalFElse
        assertIs<IrConst<*>>(evalFElse)
        assertEquals(60.0, evalFElse.value)
        val mainReturn = visitor.mainReturn
        assertIs<IrConst<*>>(mainReturn)
        assertEquals(60.0, mainReturn.value)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Break`() {
        val visitor = object: IrElementVisitorVoid {
            lateinit var result: String
            val collector = StringCollector()
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
            override fun visitFile(declaration: IrFile) {
                declaration.transform(Replacer(evaluator = Evaluator(messageCollector = collector)), Context())
                super.visitFile(declaration)
            }
            override fun visitFunction(declaration: IrFunction) {
                if (declaration.name.asString() == "main")
                    super.visitFunction(declaration)
            }

            override fun visitWhen(expression: IrWhen) {
                result = expression.dump()
            }
        }
        val program = """
            fun evalAbs(x: Double): Double {
                if (x < 0)
                    return -x
                return x
            }
            fun evalSqrt(x: Double): Double {
                var result = x/2
                while(true) {
                    val newresult = (result + x/result)/2
                    val err = evalAbs(newresult - result)
                    if (err < 1e-10) return newresult
                    result = newresult
                }
                return result
            }
            val LIMIT = 1000.0
            fun main() {
                var i = 0
                while (true) {
                    if (i > 5 && evalSqrt(1600.0) < LIMIT)
                        break
                    println("...")
                    i++
                }
            }
        """.trimIndent()
        val result = compile(
            sourceFile = SourceFile.kotlin(name = "main.kt", program),
            plugin = KotlinCompilerPluginRegistrar(visitors = listOf(visitor))
        )
        /*
        * Expect the break condition to be simplified to
        *   if (i > 5) break
        */
        println(visitor.collector.output)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals("""
            WHEN type=kotlin.Unit origin=IF
              BRANCH
                if: WHEN type=kotlin.Boolean origin=ANDAND
                  BRANCH
                    if: CALL 'public final fun greater (arg0: kotlin.Int, arg1: kotlin.Int): kotlin.Boolean declared in kotlin.internal.ir' type=kotlin.Boolean origin=GT
                      arg0: GET_VAR 'var i: kotlin.Int [var] declared in <root>.main' type=kotlin.Int origin=null
                      arg1: CONST Int type=kotlin.Int value=5
                    then: CONST Boolean type=kotlin.Boolean value=true
                  BRANCH
                    if: CONST Boolean type=kotlin.Boolean value=true
                    then: CONST Boolean type=kotlin.Boolean value=false
                then: BREAK label=null loop.label=null
        """.trimm(), visitor.result.trimm())
    }

}