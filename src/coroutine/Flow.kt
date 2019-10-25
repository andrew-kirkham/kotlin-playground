package com.andrew.coroutine

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import java.lang.Thread.sleep
import kotlin.random.Random
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

private val client = HttpClient()

@ExperimentalCoroutinesApi
fun main() {
//    responseCollector()
    takeOnlyAFewElementsFromAFlow()
//    flowBuilder()
//    complexFlowBuilder()
//    combiningFlows()
//    exceptionFlows()
    sleep(5_000)
}

fun getFromUrl(url: String = "https://postman-echo.com/get?a="): Flow<String> = flow {
    // this is very similar style to a channel
    logger.info { "Flow started" }
    for (i in 1..3) {
        logger.info { "i=$i" }
        val response = client.get<String>(url.plus("$i"))
        logger.info { "response=$response" }
        emit(response)
    }
}

//region Collector
fun responseCollector() = CoroutineScope(Dispatchers.IO).launch {
    val flow = getFromUrl()
    val strings = ArrayList<String>()
    logger.info { "about to collect flow" }
    // interacting with flows is much different, as they are effectively collections
    flow.collect { value ->
        logger.info { "adding value=$value" }
        strings.add(value)
    }
    logger.info { "count=${strings.size}" }
}
//endregion

//region Take
@ExperimentalCoroutinesApi
fun takeOnlyAFewElementsFromAFlow() = CoroutineScope(Dispatchers.IO).launch {
    val flow = getFromUrl()
    // we don't have to take the entire collection, and we can specify exactly how many we want
    // because flows are cold, this helps limit total execution
    // even though our flow is 3 elements, we only execute 2x as you can see in the logs
    flow.take(2)
        .collect { value -> logger.info { "taking element $value" } }
}
//endregion

//region Builder
val sum: (Int, Int) -> Int = { x: Int, y: Int -> x + y }

// we can construct a flow from an existing collection
fun flowBuilder() = runBlocking {
    (1..100).asFlow()
        .collect { value -> logger.info { sum(value, value) } }
}
//endregion

//region Chaining
fun complexFlowBuilder() = CoroutineScope(Dispatchers.IO).launch {
    val flow = getFromUrl()
    // we can chain a lot of operations on a flow just like a collection
    val valuesSquared = flow
        .map {
            logger.info { "completed with value $it" }
            it.get(14).toString().toInt() // hacky lazy way of parsing out an int
        }
        .map { it * it }
        .toList()

    logger.info { "valuesSquared=$valuesSquared" }
}
//endregion

//region Combining Flows
@ExperimentalCoroutinesApi
fun combiningFlows() = CoroutineScope(Dispatchers.IO).launch {
    val flow = getFromUrl()
    val numbers = emitNumbers()

    // we can combine multiple flows together into a single flow
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

fun emitNumbersForever(): Flow<Int> = flow {
    while (true) {
        delay(500)
        emit(Random.nextInt())
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

@ExperimentalCoroutinesApi
fun exceptionFlows() = CoroutineScope(Dispatchers.IO).launch {
    // exception handling is different compared to channels
    // how many times do you think this flow will execute?
    emitPeriodicExceptions()
        .catch { e -> logger.info { "caught exception $e" } }
        .collect { logger.info { "emitted value=$it" } }
}
//endregion
