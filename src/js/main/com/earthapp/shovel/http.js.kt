@file:OptIn(ExperimentalJsExport::class)

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
        outerHtml(),
        text(),
        ownText(),
        attributes().associate { it.key to it.value },
        children().map { it.convert() }
    )
}

@JsExport
@JsName("documentQuerySelectorAll")
actual fun Document.querySelectorAll(selector: String): List<Element> {
    val doc = Ksoup.parse(html)
    return doc.select(selector).map { it.convert() }
}

@JsExport
@JsName("elementQuerySelectorAll")
actual fun Element.querySelectorAll(selector: String): List<Element> {
    val element = Ksoup.parse(outerHTML)
    return element.select(selector).map { it.convert() }
}