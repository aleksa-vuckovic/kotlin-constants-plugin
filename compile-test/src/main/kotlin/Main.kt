package org.example

import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.javaType
import java.time.*

class A {
    fun List<String>.hehe(count: Int) {}
    fun f() {}
}

@OptIn(ExperimentalStdlibApi::class)
fun main(x: Int) {
    //testLog()
    val a = A()
    (if (x > 3) a else return).f()
}
/*
fun dumpThis(): Int {
    val x = 1 + 3
    val y = 10
    return x + y
}

fun testLog() {
    println("In testLog")
    class A {
        fun f(value: String) {
            println("In A::f with ${value}")
        }
    }
    fun fstatic() {
        println("In f static")
    }
    val flambda = {println("In flambda")}
    val a = A()

    a.f("someValue")
    fstatic()
    flambda()
}

*/