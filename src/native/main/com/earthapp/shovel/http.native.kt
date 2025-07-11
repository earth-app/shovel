package com.earthapp.shovel

import com.fleeksoft.ksoup.Ksoup

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

actual fun Document.querySelectorAll(selector: String): List<Element> {
    val doc = Ksoup.parse(html)
    return doc.select(selector).map { it.convert() }
}

actual fun Element.querySelectorAll(selector: String): List<Element> {
    val element = Ksoup.parse(outerHTML)
    return element.select(selector).map { it.convert() }
}