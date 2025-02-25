package com.example.myapplication

import org.junit.Test

class DelegateExampleUnitTest {

    @Test
    fun testFunction() {
        val base = BaseImpl();
        val derived = Derived(base);
        derived.printMessage()
    }

    interface Base {
        fun printMessage()
    }

    class BaseImpl : Base {
        override fun printMessage() {
            println("BaseImpl: printMessage")
        }
    }

    class Derived(b : Base) : Base by b

}