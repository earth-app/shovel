package com.earthapp.shovel

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.java.Java
import kotlinx.coroutines.Dispatchers
import org.jsoup.Jsoup

actual val engine: HttpClientEngine = Java.create {
    pipelining = true
    dispatcher = Dispatchers.IO.limitedParallelism(PARALLEL_COUNT)
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