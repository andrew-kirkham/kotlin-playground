package com.andrew.coroutine

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import mu.KotlinLogging
import java.lang.Thread.sleep
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

private val client = HttpClient()

fun main() {
    logger.info { "Starting" }
    getValuesFromApiUsingChannels()
//    getValuesFromApiUsingChannelsBadly()
//    getValuesFromApiUsingMultipleChannels()
//    manyCoroutinesPushToOneChannel()
    sleep(2_000)
}

fun CoroutineScope.produceValuesFromApi() = produce<String> {
    logger.info { "Producer started" }
    var i = 0
    while (true) {
        logger.info { "i=$i" }
        val response = client.get<String>("https://postman-echo.com/get?a=$i")
        logger.info { "response=$response" }
        send(response)
        i++
    }
    logger.info { "Producer closing" }
}

fun CoroutineScope.modifyValues(responses: ReceiveChannel<String>): ReceiveChannel<String> = produce<String> {
    logger.info { "modifier channel started" }
    for (response in responses) {
        val value = "1 $response"
        logger.info { "modifying and sending" }
        send(value)
    }
    logger.info { "modifier closing" }
}

//region Multiple Channels
fun getValuesFromApiUsingChannels() = GlobalScope.launch(Dispatchers.IO) {
    val apiResponses = produceValuesFromApi()
    val modified = modifyValues(apiResponses)
    for (i in 1..3) {
        val modifiedResponses = modified.receive()
        logger.info { "apiResponses=$modifiedResponses" }
    }
    coroutineContext.cancelChildren() // cancel all children to let main finish
}
//endregion

//region Passing to channels improperly
fun getValuesFromApiUsingChannelsBadly() = GlobalScope.launch(Dispatchers.IO) {
    val apiResponses = produceValuesFromApi()
    for (i in 1..3) {
        val modifiedResponses = modifyValues(apiResponses).receive()
        logger.info { "apiResponses=$modifiedResponses" }
    }
    coroutineContext.cancelChildren() // cancel all children to let main finish
}
//endregion

//region Multiple Channel Processors
fun CoroutineScope.printvalues(id: Int, responses: ReceiveChannel<String>) = launch {
    logger.info { "printer channel $id started" }
    for (response in responses) {
        logger.info { "$id received $response" }
    }
    logger.info { "printer $id closing" }
}

fun getValuesFromApiUsingMultipleChannels() = GlobalScope.launch(Dispatchers.IO) {
    val apiResponses = produceValuesFromApi()
    for (i in 1..3) {
        printvalues(i, apiResponses)
    }
    delay(2_000)
    apiResponses.cancel()
}
//endregion

//region Many Coroutines One Channel
suspend fun produceValuesFromApiChannel(channel: Channel<String>) {
    logger.info { "Producer started" }
    var i = 0
    while (true) {
        logger.info { "i=$i" }
        val response = client.get<String>("https://postman-echo.com/get?a=$i")
        logger.info { "response=$response" }
        channel.send(response)
        i++
    }
    logger.info { "Producer closing" }
}

suspend fun putRandomValuesOnChannel(channel: Channel<String>) {
    logger.info { "putting random stuff in the channel" }
    while (true) {
        val random = Random.nextInt()
        logger.info { "random=$random" }
        channel.send(random.toString())
        delay(300)
    }
}

fun manyCoroutinesPushToOneChannel() = CoroutineScope(Dispatchers.IO).launch {
    val channel = Channel<String>()
    launch { produceValuesFromApiChannel(channel) }
    launch { putRandomValuesOnChannel(channel) }
    repeat(5) {
        val received = channel.receive()
        logger.info { "received $received" }
    }
    coroutineContext.cancelChildren()
}
//endregion
