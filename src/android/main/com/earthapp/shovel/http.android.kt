package com.earthapp.shovel

import android.net.Uri
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import kotlinx.coroutines.Dispatchers
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

internal actual val engine: HttpClientEngine = OkHttp.create {
    pipelining = true
    dispatcher = Dispatchers.IO.limitedParallelism(PARALLEL_COUNT)

    config {
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
        writeTimeout(30, TimeUnit.SECONDS)
    }
}

private fun org.jsoup.nodes.Element.convert(): Element {
    return Element(
        tagName = tagName(),
        innerHTML = html(),
        textContent = text(),
        ownTextContent = ownText(),
        attributes = attributes().asList().associate { it.key to it.value },
        children = children().map { it.convert() }
    )
}

actual fun Document.querySelectorAll(selector: String): List<Element> {
    val doc = Jsoup.parse(html)
    return doc.select(selector).map { it.convert() }
}