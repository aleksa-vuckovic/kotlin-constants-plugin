package org.example
import kotlin.reflect.*

fun func(value: Int, op: Int.() -> Unit) {
    value.op()
}
class A<T> {}
fun main() {
    val f = {it: Int -> println(it)}
    var g: Int.() -> Unit = {println(this)}
    f(1)
    g(2)
    3.g()
    g = f
    4.g()
    val l = A<List<*>>()
}