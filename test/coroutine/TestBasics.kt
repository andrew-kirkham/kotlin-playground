package com.andrew.coroutine

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlinx.coroutines.async

class TestBasics : StringSpec({
    "it should return a value of 10 with async/await" {
        val ret = async { waitAndReturn() }
        ret.await() shouldBe 10
    }
    "it should return from a function that executes"
})
