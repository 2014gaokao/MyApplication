package com.example.myapplication

import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class DelegateExample {

    fun testFunction() {
        val base = BaseImpl();
        val derived = Derived(base);
        derived.printMessage()

        A().apply {
            aTest = "123"
            println(aTest)
        }

        val test by lazy {
            println("初始化")
            true //这他妈的是返回值
        }
        println(test)
        println(test)

        var aTest by Delegates.observable("hello") { _, oldValue, newValue ->
            println("${oldValue} ${newValue}")
        }
        aTest = "test"
        println(aTest)

        var mytest by MyTest("test") {oldValue, newValue ->
            println("${oldValue} ${newValue}")
        }
        mytest = "hello"
        println(mytest)
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

    class A {
        var aTest: String by MyTestClass()
    }

    class MyTestClass : ReadWriteProperty<A, String> {
        private var myVar = ""

        override fun getValue(thisRef: A, property: KProperty<*>): String = myVar

        override fun setValue(thisRef: A, property: KProperty<*>, value: String) {
            myVar = value
        }
    }

    class MyTest(private var defaultValue : String = "", val onChange : (String, String) -> Unit) :
        ReadWriteProperty<Nothing?, String> {
        override fun getValue(thisRef: Nothing?, property: KProperty<*>) = defaultValue

        override fun setValue(thisRef: Nothing?, property: KProperty<*>, value: String) {
            if (defaultValue == value) return
            onChange(defaultValue, value)
            defaultValue = value
        }

    }

}