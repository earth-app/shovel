package com.earthapp.shovel

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

actual val engine: HttpClientEngine = CIO.create {
    pipelining = true
    dispatcher = Dispatchers.IO.limitedParallelism(PARALLEL_COUNT)
}