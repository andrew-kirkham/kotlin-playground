package com.andrew

import io.ktor.locations.Location

@Location("/health")
class HealthCheck {
    fun healthCheck() = "OK"
}
