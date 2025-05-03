package com.earthapp.shovel

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.Dispatchers

actual val engine: HttpClientEngine = Darwin.create {
    pipelining = true
    dispatcher = Dispatchers.Default

    configureRequest {
        setAllowsCellularAccess(true)
    }

    configureSession {
        setAllowsCellularAccess(true)
    }
}