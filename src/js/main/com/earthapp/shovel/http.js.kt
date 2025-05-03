package com.earthapp.shovel

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import kotlinx.coroutines.Dispatchers

actual val engine: HttpClientEngine = Js.create {
    pipelining = true
    dispatcher = Dispatchers.Default
}

private fun com.fleeksoft.ksoup.nodes.Element.convert(): Element {
    return Element(
        tagName(),
        html(),
        text(),
        attributes().associate { it.key to it.value },
        children().map { it.convert() }
    )
}

actual fun Document.querySelectorAll(selector: String): List<Element> {
    val doc = Ksoup.parse(html)
    return doc.select(selector).map { it.convert() }
}