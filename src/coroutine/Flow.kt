package com.andrew.coroutine

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import java.lang.Thread.sleep

private val logger = KotlinLogging.logger {}

private val client = HttpClient()

fun main() {
    responseCollector()
//    takeOnlyAFewElementsFromAFlow()
//    flowBuilder()
//    complexFlowBuilder()
//    combiningFlows()
//    exceptionFlows()
    sleep(5_000)
}

fun getFromUrl(): Flow<String> = flow {
    logger.info { "Flow started" }
    for (i in 1..3) {
        logger.info { "i=$i" }
        val response = client.get<String>("https://postman-echo.com/get?a=$i")
        logger.info { "response=$response" }
        emit(response)
    }
}

//region Collector
fun responseCollector() = CoroutineScope(Dispatchers.IO).launch {
    val flow = getFromUrl()
    val strings = ArrayList<String>()
    logger.info { "about to collect flow" }
    flow.collect { value ->
        logger.info { "adding value=$value" }
        strings.add(value)
    }
    logger.info { "count=${strings.size}" }
}
//endregion

//region Take
fun takeOnlyAFewElementsFromAFlow() = CoroutineScope(Dispatchers.IO).launch {
    val flow = getFromUrl()
    flow.take(2).collect { value -> logger.info { "taking element $value" } }
}
//endregion

//region Builder
val sum: (Int, Int) -> Int = { x: Int, y: Int -> x + y }

fun flowBuilder() = runBlocking {
    (1..100).asFlow().collect { value -> logger.info { sum(value, value) } }
}
//endregion

//region Chaining
fun complexFlowBuilder() = CoroutineScope(Dispatchers.IO).launch {
    val flow = getFromUrl()
    val valuesSquared = flow
        .map {
            logger.info { "completed with value $it" }
            it.get(14).toString().toInt()
        }
        .map { it * it }
        .toList()

    logger.info { "valuesSquared=$valuesSquared" }
}
//endregion

//region Combining Flows
fun combiningFlows() = CoroutineScope(Dispatchers.IO).launch {
    val flow = getFromUrl()
    val numbers = emitNumbers()

    numbers.combine(flow) { a, b ->
        "$a -> $b"
    }.collect { logger.info { it } }
}

fun emitNumbers(): Flow<Int> = flow {
    (1..3).forEach {
        logger.info { "sleeping and then emitting $it" }
        delay(300)
        logger.info { "emitting $it" }
        emit(it)
    }
}
//endregion

//region Exception Handling
fun emitPeriodicExceptions(): Flow<Int> = flow {
    (1..10).forEach {
        if (it % 3 == 0) throw ArithmeticException()
        emit(it)
    }
}

fun exceptionFlows() = CoroutineScope(Dispatchers.IO).launch {
    emitPeriodicExceptions()
        .catch { e -> logger.info { "caught exception $e" } }
        .collect { logger.info { "emitted value=$it" } }
}
//endregion
