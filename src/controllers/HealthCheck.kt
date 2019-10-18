package com.andrew.controllers

import io.ktor.locations.Location

@Location("/health")
class HealthCheck {
    fun healthCheck() = "OK"
}
