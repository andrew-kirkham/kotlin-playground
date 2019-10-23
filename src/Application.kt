package com.andrew

import com.andrew.controllers.ArrowTest
import com.andrew.controllers.HealthCheck
import com.andrew.controllers.MyLocation
import com.andrew.controllers.Type
import com.fasterxml.jackson.databind.*
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.getOrFail
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(Locations) {
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(CallLogging) {
        level = Level.INFO
    }

    val client = HttpClient(CIO) {
    }

    routing {
        trace { application.log.trace(it.buildText()) }

        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get<MyLocation> {
            call.respondText("Location: name=${it.name}, arg1=${it.arg1}, arg2=${it.arg2}")
        }
        // Register nested routes
        get<Type.Edit> {
            call.respondText("Inside $it")
        }
        get<Type.List> {
            call.respondText("Inside $it")
        }

        get<HealthCheck> {
            call.respond(it.healthCheck())
        }

        get<ArrowTest.Parse> {
            call.respond(it.tryParse()!!)
        }

        install(StatusPages) {
            exception<AuthenticationException> { cause ->
                call.respond(HttpStatusCode.Unauthorized)
            }
            exception<AuthorizationException> { cause ->
                call.respond(HttpStatusCode.Forbidden)
            }
        }

        get("/json/jackson") {
            call.respond(mapOf("hello" to "world"))
        }

        intercept(ApplicationCallPipeline.Call) {
            if (call.request.uri.startsWith("/location")) {
                call.request.queryParameters.getOrFail<Int>("arg1")
                call.request.queryParameters.getOrFail<String>("arg2")
            }
            proceed()
        }

        intercept(ApplicationCallPipeline.Fallback) {
            call.application.environment.log.error("uncaught exception thrown")
            proceed()
        }
    }
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()
