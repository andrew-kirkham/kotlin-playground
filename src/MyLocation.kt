package com.andrew

import io.ktor.locations.Location


@Location("/location/{name}")
class MyLocation(val name: String, val arg1: Int = 42, val arg2: String = "default")
