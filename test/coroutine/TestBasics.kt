package com.andrew.coroutine

import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.specs.DescribeSpec
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest

class TestBasics : AnnotationSpec() {

    @Test
    fun testACoroutineWithDelay() {
        val expected = 10
        val scope = TestCoroutineScope()
        scope.runBlockingTest {
            val asyncActual = async { waitAndReturn() }
            val actual = asyncActual.await()
            expected shouldBe actual
        }
    }

    @Test
    fun testACoroutineWithDelayBlockingScope() = runBlockingTest {
        val expected = 10
        val actual = waitAndReturn()
        expected shouldBe actual
    }
}

class TestWithDescribeSpec : DescribeSpec({
    describe("coroutines") {
        it("can return a value from a suspend function") {
            val scope = TestCoroutineScope()
            scope.runBlockingTest {
                val expected = 10
                val actual = waitAndReturn()
                expected shouldBe actual
            }
        }
    }
})
