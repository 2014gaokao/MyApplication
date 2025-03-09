package com.example.myapplication

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.random.Random

class FlowExampleUnitTest {

    @Test
    fun coldFlow() {
        runBlocking {
            val flow = flow {
                repeat(10) {
                    emit(it)
                }
            }
            flow.transform { emit(it * 2) }.onEach { println(it) }.collect()
            flow.map { it * it }
                .filter { it % 2 == 0 }
                .take(2)
                .onEach { println(it) }
                .collect()
        }
    }

    @Test
    fun shareFlow() {
        runBlocking {
            val sharedFlow = MutableSharedFlow<Int>(
                replay = 3,
                extraBufferCapacity = 1,
                onBufferOverflow = BufferOverflow.DROP_OLDEST
            )
            sharedFlow.onEach {
                println("接受数据${it}")
                delay(1000)
            }.launchIn(this)
            delay(1000)
            repeat(10) {
                println("发送次数${it + 1}")
                sharedFlow.emit(it + 1)
            }
        }
    }

    @Test
    fun stateFlow() {
        runBlocking {
            val viewModel = ViewModel()
            launch {
                viewModel.age.collect {
                    println(it)
                }
            }
            launch {
                while (isActive) {
                    viewModel.updateAge()
                }
            }
        }
    }

}

class ViewModel {
    private val _age = MutableStateFlow(0)
    val age = _age.asStateFlow()
    suspend fun updateAge() {
        _age.emit(request())
    }
}

suspend fun request() : Int {
    delay(Random.nextLong(1000))
    return Random.nextInt(20, 60)
}