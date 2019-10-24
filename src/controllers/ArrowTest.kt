package com.andrew.controllers

import arrow.core.Either
import io.ktor.locations.Location

fun parse(s: String): Either<NumberFormatException, Int> =
    if (s.matches(Regex("-?[0-9]+"))) Either.Right(s.toInt())
    else Either.Left(NumberFormatException("$s is not a valid integer."))

@Location("/arrow")
class ArrowTest {

    @Location("/{name}")
    class Parse(val name: String) {
        fun tryParse(): String? {
            return when (val result = parse(name)) {
                is Either.Left -> result.a.message
                is Either.Right -> result.b.toString()
            }
        }
    }
}
