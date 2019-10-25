package com.andrew.coroutine

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import java.lang.Thread.sleep
import kotlin.random.Random
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

private val client = HttpClient()

@ExperimentalCoroutinesApi
fun main() {
    logger.info { "Starting" }
//    getValuesFromApiUsingChannels()
//    getValuesFromApiUsingChannelsBadly()
    getValuesFromApiUsingMultipleChannels()
//    manyCoroutinesPushToOneChannel()
    sleep(2_000)
}

@ExperimentalCoroutinesApi
// note we don't specify the dispatcher here. it's dispatched by the calling coroutine
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

@ExperimentalCoroutinesApi
// take values from one channel, modify them, and push them to a new channel
fun CoroutineScope.modifyValues(responses: ReceiveChannel<String>): ReceiveChannel<String> = produce {
    logger.info { "modifier channel started" }
    for (response in responses) {
        val value = "1 $response"
        logger.info { "modifying and sending" }
        send(value)
    }
    logger.info { "modifier closing" }
}

//region Channels
@ExperimentalCoroutinesApi
fun getValuesFromApiUsingChannels() = GlobalScope.launch(Dispatchers.IO) {
    val apiResponses = produceValuesFromApi()
    // pass one channel to another
    val modified = modifyValues(apiResponses)
    for (i in 1..3) {
        // receive only the first three values
        // .receive() will pull the value from .send()
        val modifiedResponses = modified.receive()
        logger.info { "apiResponses=$modifiedResponses" }
    }
    coroutineContext.cancelChildren() // cancel all children to let main finish
}
//endregion

//region Passing to channels improperly
@ExperimentalCoroutinesApi
fun getValuesFromApiUsingChannelsBadly() = GlobalScope.launch(Dispatchers.IO) {
    // this seems like a possible/valid refactoring from the function above.
    // but run the code and compare the results - they are not the same
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

@ExperimentalCoroutinesApi
fun getValuesFromApiUsingMultipleChannels() = GlobalScope.launch(Dispatchers.IO) {
    // get values from the same endpoint
    val apiResponses = produceValuesFromApi()
    // this time we are going to pass each value to a channel.
    // this is actually spinning up multiple channels to handle the results
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
    // multiple different coroutines can push data to one channel
    val channel = Channel<String>()
    // we are now  passing an explicit channel rather than using the implicit one in previous examples
    launch { produceValuesFromApiChannel(channel) }
    launch { putRandomValuesOnChannel(channel) }
    repeat(5) {
        // receive is going to take the first available value.
        // each channel can be pushing at different speeds
        // .receive() is going to push them out FIFO style
        val received = channel.receive()
        logger.info { "received $received" }
    }
    coroutineContext.cancelChildren()
}
//endregion
