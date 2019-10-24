package com.andrew.coroutine

import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.assertNotSame

class TestFlow : AnnotationSpec() {

    /*
    fun emitNumbers(): Flow<Int> = flow {
        (1..3).forEach {
            logger.info { "sleeping and then emitting $it" }
            delay(300)
            logger.info { "emitting $it" }
            emit(it)
        }
    }

    fun emitNumbersForever(): Flow<Int> = flow {
        while(true) {
            delay(500)
            emit(Random.nextInt())
        }
    }
     */

    @Test
    fun testFlow() = runBlockingTest {
        val flow = emitNumbers()
        val expected = listOf(1, 2, 3)
        val actual = flow.toList()
        expected shouldBe actual
    }

    @Test
    fun testFromNeverEndingFlow() = runBlockingTest {
        val flow = emitNumbersForever()
        flow.take(1).collect { value ->
            assertNotSame(1, value)
        }
    }
}
