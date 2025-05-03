package com.earthapp.shovel

/**
 * Transforms a query selector into an object.
 * @param selector The selector to query.
 * @param transformer The function to transform the results.
 * @return The transformed object.
 */
fun <T> Document.transformQuerySelectorAll(selector: String, transformer: (List<Element>) -> T): T {
    val elements = querySelectorAll(selector)
    return transformer(elements)
}

/**
 * Transforms a query selector into an object.
 * @param selector The selector to query.
 * @param transformer The function to transform the result.
 * @return The transformed object.
 */
fun <T> Document.transformQuerySelector(selector: String, transformer: (Element) -> T): T {
    val element = querySelector(selector) ?: error("Element not found")
    return transformer(element)
}

/**
 * Transforms a class name into an object.
 * @param className The class name to query.
 * @param transformer The function to transform the results.
 * @return The transformed object.
 */
fun <T> Document.transformClassName(className: String, transformer: (List<Element>) -> T): T {
    val elements = getElementsByClassName(className)
    return transformer(elements)
}

/**
 * Transforms an ID into an object.
 * @param id The ID to query.
 * @param transformer The function to transform the result.
 * @return The transformed object.
 */
fun <T> Document.transformId(id: String, transformer: (Element) -> T): T {
    val element = getElementById(id) ?: error("Element not found")
    return transformer(element)
}