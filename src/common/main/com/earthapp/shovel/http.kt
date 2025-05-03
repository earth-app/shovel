package com.earthapp.shovel

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.utils.io.charsets.Charsets
import kotlinx.io.IOException
import kotlin.collections.contains
import kotlin.js.JsExport
import kotlin.jvm.JvmOverloads

internal const val PARALLEL_COUNT = 32
internal expect val engine: HttpClientEngine

internal val client
    get() = HttpClient(engine) {
        expectSuccess = false
        followRedirects = false
    }

internal const val USER_AGENT = "Ktor HTTP Client, Tabroom API v1"

internal val cache = mutableMapOf<String, Document>()

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
        throw IOException("Error happened when trying to fetch document '$this': ${e.message}", e)
    }

    if (!res.status.isSuccess()) throw IOException("Failed to fetch document '$this': ${res.status}\n${res.bodyAsText(Charsets.UTF_8)}")

    val text = res.bodyAsText(Charsets.UTF_8)
    cache[this] = Document(this, text)

    return Document(this, text)
}

/**
 * Closes the client. This should be called when the API is no longer needed.
 */
fun closeClient() = client.close()

/**
 * Clears the document cache.
 */
fun clearCache() = cache.clear()

/**
 * Represents an HTML element.
 */
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
     * The text content of the element.
     */
    val textContent: String,
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
}

/**
 * Represents an HTML document.
 */
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
 * @return The first element that matches the selector, or null if it doesn't exist.
 */
fun Document.querySelector(selector: String): Element? = querySelectorAll(selector).firstOrNull()

/**
 * Gets the first element with the specified ID.
 * @param id The ID of the element to get.
 * @return The first element with the specified ID, or null if it doesn't exist.
 */
fun Document.getElementById(id: String): Element? = querySelector("#$id")

/**
 * Gets all elements with the specified class name.
 * @param className The class name of the elements to get.
 * @return A list of elements with the specified class name.
 */
fun Document.getElementsByClassName(className: String): List<Element> = querySelectorAll(".$className")

/**
 * Gets the value of an input element.
 * @param name The name of the input element.
 * @return The value of the input element, or null if it doesn't exist.
 */
fun Document.inputValue(name: String): String? {
    val input = querySelector("input[name=$name]") ?: return null
    return input["value"] ?: input["checked"]
}