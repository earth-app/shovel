package com.earthapp.shovel

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.winhttp.WinHttp
import io.ktor.http.HttpProtocolVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

actual val engine: HttpClientEngine = WinHttp.create {
    pipelining = true
    protocolVersion = HttpProtocolVersion.HTTP_2_0
    dispatcher = Dispatchers.IO.limitedParallelism(PARALLEL_COUNT)
}