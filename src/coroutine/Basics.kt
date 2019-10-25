package com.andrew.coroutine

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import java.lang.Thread.sleep
import kotlinx.coroutines.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "starting up!" }
    globalScope()
//    blockingCoroutine()
//    coroutineWithScopeDefined()
//    ioCoroutine()
//    launchSuspend()
//    launchNonSuspend()
//    launchALotLotLotOfRoutines()
//    exceptionHandlingCoroutine()
//    asyncHandlingCoroutine()
//    coroutineHandlingException()
    sleep(3_000)
}

//region Global Scope
fun globalScope() {
    GlobalScope.launch {
        logger.info { "Coroutine launched" }
        delay(1_000L) // non-blocking delay for 1 second
        logger.info { "slept for 1 second" }
    }
    logger.info { "Launched!" } // main thread continues while coroutine is delayed
    sleep(2_000L)
}
//endregion

//region Blocking Scope
fun blockingCoroutine() = runBlocking {
    logger.info { "sleeping for 1 second" }
    delay(1_000)
    logger.info { "done sleeping" }
}
//endregion

//region Coroutine Scope at Function Declaration
fun coroutineWithScopeDefined() = CoroutineScope(Dispatchers.Default).launch {
    logger.info { "sleeping for 1 second" }
    delay(1_000)
    logger.info { "done sleeping" }
}

// IO dispatcher is intended for blocking calls
fun ioCoroutine() = CoroutineScope(Dispatchers.IO).launch {
    val client = HttpClient()
    val response = client.get<String>("https://postman-echo.com/get?a=1")
    logger.info { response }
}
//endregion

//region Suspending functions
suspend fun waitAndReturn(): Int {
    logger.info { "starting" }
    delay(1_000L)
    val ten = 10
    logger.info { "returning $ten" }
    return ten
}

fun launchSuspend() = CoroutineScope(Dispatchers.Default).launch {
    // launch the suspend function synchronously in the context of this coroutine scope
    waitAndReturn()
    // launch the suspend function within a new coroutine
    launch { waitAndReturn() }
    // launch the suspend function within an async builder and await the results
    logger.info { "post launch. starting Async" }
    val asyncValue = async { waitAndReturn() }
    asyncValue.await()
    logger.info { "post async" }
}

fun nonSuspend(): Int {
    val ten = 10
    logger.info { "returning $ten" }
    return ten
}

fun launchNonSuspend() = CoroutineScope(Dispatchers.Default).launch {
    // even non suspend functions can be launched within coroutine contexts
    launch {
        val x = nonSuspend()
        logger.info { "received $x" }
    }
    logger.info { "post launch" }
}
//endregion

//region Lots of Coroutines
// this will NOT kill your machine. these are very lightweight and will all be able to process together
fun launchALotLotLotOfRoutines() = runBlocking {
    repeat(100_000) {
        // launch a lot of coroutines
        launch {
            delay(1_000L)
            print("Hi!")
        }
    }
    delay(1_000L)
}
//endregion

//region Launched Exceptions
fun exceptionHandlingCoroutine() {
    logger.info { "main thread starting" }
    coroutineThrowsException()
    sleep(1_000)
}

fun coroutineThrowsException() = CoroutineScope(Dispatchers.Default).launch {
    // pay attention to the logs of where the exception is surfaced
    logger.info { "throwing exception in .5 seconds" }
    delay(500)
    throw ArithmeticException()
}
//endregion

//region Async Exceptions
fun asyncHandlingCoroutine(): Nothing = runBlocking {
    logger.info { "async thread starting" }
    val deferred = asyncThrowsException()
    delay(1_000)
    deferred.await()
}

fun asyncThrowsException() = CoroutineScope(Dispatchers.Default).async {
    // pay attention to the logs of where the exception is surfaced
    logger.info { "throwing exception in .5 seconds" }
    delay(500)
    throw ArithmeticException()
}
//endregion

//region Caught Exceptions
fun coroutineHandlingException() {
    throwCaughtException()
    sleep(1_000)
}

// note the new exceptionHandler being passed to the launch function
// it will capture any exception that isnt explicitly caught
fun throwCaughtException() = CoroutineScope(Dispatchers.Default).launch(exceptionHandler) {
    logger.info { "throwing exception in .5 seconds" }
    delay(500)
    throw ArithmeticException()
}

val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
    logger.info { "context=$context" }
    logger.info { "throwable.message=${throwable.message}" }
}
//endregion
