package org.example

import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.javaType
import java.time.*

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

fun test1(): Double {
    var i = 0
    var result = 0.0
    while (true) {
        result = evalF(10.0, i++)
        if (result > 50.0 && result < 70.0)
            break
    }
    return result
}

fun test2(): Int {
    val lambda = { x: Int, y: Int, log: Boolean ->
        if (log)
            println("Invoked lambda")
        x + y
    }
    val value1 = lambda(1,2,false)
    val value2 = lambda(3,4,true)
    return value1+value2
}

fun main() {
    //testLog()
}

/*
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
}*/