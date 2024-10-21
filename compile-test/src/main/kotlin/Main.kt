package org.example

fun main() {
    testLog()
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

