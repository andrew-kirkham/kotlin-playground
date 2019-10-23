package com.andrew.coroutine

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.lang.Thread.sleep

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "starting up!" }
//    mostBasic()
//    blockingCoroutine()
//    coroutineWithScopeDefined()
//    ioCoroutine()
//    launchSuspend()
//    launchNonSuspend()
//    launchALotLotLotOfRoutines()
//    exceptionHandlingCoroutine()
//    asyncHandlingCoroutine()
//    coroutineHandlingException()
//    sleep(3_000)
}

//region Global Scope
fun mostBasic() {
    GlobalScope.launch {
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
    waitAndReturn()
    launch { waitAndReturn() }
    logger.info { "post launch" }
}

fun nonSuspend(): Int {
    val ten = 10
    logger.info { "returning $ten" }
    return ten
}

fun launchNonSuspend() = CoroutineScope(Dispatchers.Default).launch {
    launch {
        val x = nonSuspend()
        logger.info { "received $x" }
    }
    logger.info { "post launch" }
}
//endregion

//region Lots of Coroutines
fun launchALotLotLotOfRoutines() = runBlocking {
    repeat(100_000) {
        // launch a lot of coroutines
        launch {
            delay(1_000L)
            print(".")
        }
    }
    delay(1_000L)
}
//endregion

//region Launched Exceptions
fun exceptionHandlingCoroutine() {
    logger.info { "main thread starting" }
    coroutineThrowsException()
    sleep(1000)
}

fun coroutineThrowsException() = CoroutineScope(Dispatchers.Default).launch {
    logger.info { "throwing exception in .5 seconds" }
    delay(500)
    throw ArithmeticException()
}
//endregion

//region Async Exceptions
fun asyncHandlingCoroutine(): Nothing = runBlocking {
    logger.info { "async thread starting" }
    val deferred = asyncThrowsException()
    delay(1000)
    deferred.await()
}

fun asyncThrowsException() = CoroutineScope(Dispatchers.Default).async {
    logger.info { "throwing exception in .5 seconds" }
    delay(500)
    throw ArithmeticException()
}
//endregion

//region Caught Exceptions
fun coroutineHandlingException() {
    throwCaughtException()
    sleep(1000)
}

fun throwCaughtException() = CoroutineScope(Dispatchers.Default).launch(exceptionHandler) {
    logger.info { "throwing exception in .5 seconds" }
    delay(500)
    throw ArithmeticException()
}

val exceptionHandler = CoroutineExceptionHandler{ context, throwable ->
    logger.info { "context=$context" }
    logger.info { "throwable.message=${throwable.message}" }
}
//endregion