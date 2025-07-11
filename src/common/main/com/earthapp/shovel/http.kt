@file:OptIn(ExperimentalJsExport::class)

package com.earthapp.shovel

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.readRemaining
import kotlinx.io.IOException
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.jvm.JvmOverloads

internal const val PARALLEL_COUNT = 32
internal expect val engine: HttpClientEngine

internal val client
    get() = HttpClient(engine) {
        expectSuccess = false
        followRedirects = false
    }

internal const val USER_AGENT = "Ktor HTTP Client, @earth-app/shovel"

internal val cache = mutableMapOf<String, Document>()

private suspend fun String.performRequest(request: HttpRequestBuilder.() -> Unit): HttpResponse {
    val res = try {
        client.get(this) {
            headers {
                append("User-Agent", USER_AGENT)
                append("Host", substringAfter("://").substringBefore('/'))
                append("Accept-Language", "en-US,en;q=0.9")
                append("Connection", "keep-alive")
                append("Upgrade-Insecure-Requests", "1")

                request()
            }
        }
    } catch (e: Throwable) {
        throw IOException("Error happened when trying to fetch '$this': ${e.message}", e)
    }

    if (!res.status.isSuccess()) {
        throw IOException("Failed to fetch '$this': ${res.status}\n${res.bodyAsText(Charsets.UTF_8)}")
    }

    return res
}

/**
 * Fetches a document from the given URL.
 *
 * This function will cache the document for future use. You can clear the
 * cache by calling [clearCache].
 *
 * @param request Additional builder methods for the GET request.
 * @return The document from the page.
 * @see [fetchDocument]
 */
@JvmOverloads
suspend fun fetch(url: String, request: HttpRequestBuilder.() -> Unit = {}): Document = url.fetchDocument(request)

/**
 * Fetches a document from the given URL.
 *
 * This function will cache the document for future use. You can clear the
 * cache by calling [clearCache].
 *
 * @param request Additional builder methods for the GET request.
 * @return The document from the page.
 */
@JvmOverloads
suspend fun String.fetchDocument(request: HttpRequestBuilder.() -> Unit = {}): Document {
    if (this in cache) return cache[this]!!

    val res = performRequest(request)
    val text = res.bodyAsText(Charsets.UTF_8)
    cache[this] = Document(this, text)

    return Document(this, text)
}

/**
 * Fetches a document from the given URL, returning null if an error occurs.
 *
 * This function will cache the document for future use. You can clear the
 * cache by calling [clearCache].
 *
 * @param request Additional builder methods for the GET request.
 * @return The document from the page, or null if an error occurred.
 */
@JvmOverloads
suspend fun String.fetchDocumentOrNull(request: HttpRequestBuilder.() -> Unit = {}): Document? {
    return try {
        fetchDocument(request)
    } catch (e: IOException) {
        null
    }
}

/**
 * Fetches the text content from the given URL.
 * @param request Additional builder methods for the GET request.
 * @return The text content of the page.
 */
@JvmOverloads
suspend fun String.fetchText(request: HttpRequestBuilder.() -> Unit = {}): String {
    val res = performRequest(request)
    return res.bodyAsText(Charsets.UTF_8)
}

/**
 * Fetches the text content from the given URL, returning null if an error occurs.
 * @param request Additional builder methods for the GET request.
 * @return The text content of the page, or null if an error occurred.
 */
@JvmOverloads
suspend fun String.fetchTextOrNull(request: HttpRequestBuilder.() -> Unit = {}): String? {
    return try {
        fetchText(request)
    } catch (e: IOException) {
        null
    }
}

/**
 * Fetches the bytes from the given URL.
 * @param request Additional builder methods for the GET request.
 * @return The bytes of the page.
 */
@JvmOverloads
suspend fun String.fetchBytes(request: HttpRequestBuilder.() -> Unit = {}): ByteArray {
    val res = performRequest(request)
    return res.bodyAsBytes()
}

/**
 * Fetches the bytes from the given URL, returning null if an error occurs.
 * @param request Additional builder methods for the GET request.
 * @return The bytes of the page, or null if an error occurred.
 */
@JvmOverloads
suspend fun String.fetchBytesOrNull(request: HttpRequestBuilder.() -> Unit = {}): ByteArray? {
    return try {
        fetchBytes(request)
    } catch (e: IOException) {
        null
    }
}

/**
 * Closes the client. This should be called when the API is no longer needed.
 */
@JsExport
fun closeClient() = client.close()

/**
 * Clears the document cache.
 */
@JsExport
fun clearCache() = cache.clear()

/**
 * Represents an HTML element.
 */
@JsExport
class Element internal constructor(
    /**
     * The name of the tag (e.g., "div", "span").
     */
    val tagName: String,
    /**
     * The inner HTML of the element.
     */
    val innerHTML: String,
    /**
     * The outer HTML of the element, including the tag itself.
     */
    val outerHTML: String,
    /**
     * The text content of the element and its children, excluding HTML tags.
     */
    val textContent: String,
    /**
     * The text content of the element itself, excluding its children.
     */
    val ownTextContent: String = textContent,
    /**
     * A map of attributes of the element.
     */
    val attributes: Map<String, String>,
    /**
     * A list of child elements.
     */
    val children: List<Element>
) {
    /**
     * Gets the value of an attribute by its name.
     * @param attribute The name of the attribute.
     * @return The value of the attribute, or null if it doesn't exist.
     */
    operator fun get(attribute: String): String? = attributes[attribute]

    /**
     * Gets the ID of the element.
     * @return The ID of the element, or null if it doesn't exist.
     */
    val id: String?
        get() = attributes["id"]

    /**
     * Gets the class names of the element.
     * @return A list of class names, or an empty list if the element has no classes.
     */
    val classes: List<String>
        get() = attributes["class"]?.split("\\s+".toRegex()) ?: emptyList()

    /**
     * Gets the inline CSS style of the element.
     * @return The inline style as a string, or null if it doesn't exist.
     */
    val style: String?
        get() = attributes["style"]

}

/**
 * Represents an HTML document.
 */
@JsExport
class Document internal constructor(
    /**
     * The URL of the document.
     */
    val url: String,
    /**
     * The HTML content of the document.
     */
    val html: String
) {

    /**
     * Gets the body element of the document.
     * @return The body element.
     * @throws IllegalStateException if the document does not have a body element.
     */
    val body: Element
        get() = querySelector("body") ?: error("Document does not have a body element")

    /**
     * Gets the inner HTML of the body element, excluding script and style tags.
     * @return The inner HTML of the body element.
     * @throws IllegalStateException if the document does not have a body element.
     */
    val bodyElements: String
        get() = body.innerHTML
            .replace(Regex("<script\\b[^>]*>([\\s\\S]*?)</script>"), "")
            .replace(Regex("<style\\b[^>]*>([\\s\\S]*?)</style>"), "")

    /**
     * Gets the head element of the document.
     * @return The head element.
     * @throws IllegalStateException if the document does not have a head element.
     */
    val head: Element
        get() = querySelector("head") ?: error("Document does not have a head element")

    /**
     * Gets the title of the document.
     * @return The title of the document.
     * @throws IllegalStateException if the document does not have a title element.
     */
    val title: String
        get() = querySelector("title")?.textContent ?: error("Document does not have a title element")

    /**
     * Gets a map of all the meta tags in the document to their values.
     * The keys are the names or properties of the meta tags, and the values are their content.
     * If a meta tag is repeated, its content will be stored in a list, otherwise the list will contain a single value.
     * @return A map of meta tag names to their content.
     */
    val metadata: Map<String, List<String>>
        get() = querySelectorAll("meta").groupBy(
            keySelector = { it["name"] ?: it["property"] ?: "" },
            valueTransform = { it["content"] ?: "" }
        ).filterKeys { it.isNotEmpty() }.mapValues { it.value.filter { content -> content.isNotEmpty() } }

    /**
     * Gets a map of all the link tags in the document to their href values.
     * The keys are the rel attributes of the link tags, and the values are their href attributes.
     * If a link tag is repeated, its href will be stored in a list, otherwise the list will contain a single value.
     * @return A map of link rel attributes to their href values.
     */
    val linkTags: Map<String, List<String>>
        get() = querySelectorAll("link").groupBy(
            keySelector = { it["rel"] ?: "" },
            valueTransform = { it["href"] ?: "" }
        ).filterKeys { it.isNotEmpty() }.mapValues { it.value.filter { href -> href.isNotEmpty() } }

}

/**
 * Gets all elements that match the specified CSS selector.
 * @param selector The CSS selector to match.
 * @return A list of elements that match the selector.
 */
expect fun Document.querySelectorAll(selector: String): List<Element>

/**
 * Gets the first element that matches the specified CSS selector.
 * @param selector The CSS selector to match.
 * @param filter An optional filter function to apply to the elements.
 * @return The first element that matches the selector, or null if it doesn't exist.
 */
@JsExport
@JsName("documentQuerySelector")
fun Document.querySelector(selector: String, filter: (Element) -> Boolean = { true }): Element? = querySelectorAll(selector).firstOrNull(filter)

/**
 * Gets the first element with the specified ID.
 * @param id The ID of the element to get.
 * @return The first element with the specified ID, or null if it doesn't exist.
 */
@JsExport
@JsName("documentGetElementById")
fun Document.getElementById(id: String): Element? = querySelector("#$id")

/**
 * Gets all elements with the specified class name.
 * @param className The class name of the elements to get.
 * @return A list of elements with the specified class name.
 */
@JsExport
@JsName("documentGetElementsByClassName")
fun Document.getElementsByClassName(className: String): List<Element> = querySelectorAll(".$className")

/**
 * Gets the value of an input element.
 * @param name The name of the input element.
 * @return The value of the input element, or null if it doesn't exist.
 */
@JsExport
@JsName("documentInputValue")
fun Document.inputValue(name: String): String? {
    val input = querySelector("input[name=$name]") ?: return null
    return input["value"] ?: input["checked"]
}

/**
 * Gets all elements that match the specified CSS selector within the element.
 * @param selector The CSS selector to match.
 * @return A list of elements that match the selector.
 */
expect fun Element.querySelectorAll(selector: String): List<Element>

/**
 * Gets the first element that matches the specified CSS selector within the element.
 * @param selector The CSS selector to match.
 * @param filter An optional filter function to apply to the elements.
 * @return The first element that matches the selector, or null if it doesn't exist.
 */
@JsExport
@JsName("elementQuerySelector")
fun Element.querySelector(selector: String, filter: (Element) -> Boolean = { true }): Element? = querySelectorAll(selector).firstOrNull(filter)

/**
 * Gets the first element with the specified ID within the element.
 * @param id The ID of the element to get.
 * @return The first element with the specified ID, or null if it doesn't exist.
 */
@JsExport
@JsName("elementGetElementById")
fun Element.getElementById(id: String): Element? = querySelector("#$id")

/**
 * Gets all elements with the specified class name within the element.
 * @param className The class name of the elements to get.
 * @return A list of elements with the specified class name.
 */
@JsExport
@JsName("elementGetElementsByClassName")
fun Element.getElementsByClassName(className: String): List<Element> = querySelectorAll(".$className")

/**
 * Gets the value of an input element within the element.
 * @param name The name of the input element.
 * @return The value of the input element, or null if it doesn't exist.
 */
@JsExport
@JsName("elementInputValue")
fun Element.inputValue(name: String): String? {
    val input = querySelector("input[name=$name]") ?: return null
    return input["value"] ?: input["checked"]
}