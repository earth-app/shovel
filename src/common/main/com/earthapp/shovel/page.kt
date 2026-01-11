package com.earthapp.shovel

/**
 * Gets the language of the document.
 * This checks for the `<html lang="...">` attribute first,
 * then falls back to the `<meta http-equiv="Content-Language">` tag.
 * @return The language of the document as a string, or null if not found.
 */
fun Document.getLanguage(): String? {
    val htmlLang = querySelector("html")?.get("lang")?.trim()
    if (htmlLang != null && htmlLang.isNotEmpty()) return htmlLang

    val metaLang = querySelector("meta[http-equiv='Content-Language']")?.get("content")?.trim()
    return metaLang?.takeIf { it.isNotEmpty() }
}

/**
 * Gets the list of language codes of the document.
 * This splits the language string by commas and trims each code.
 * @return A list of language codes, or an empty list if not found.
 */
fun Document.getLanguageCodes(): List<String> {
    val lang = getLanguage() ?: return emptyList()
    return lang.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

// Head

/**
 * Gets the title of the document.
 * @return The title of the document, or null if not found.
 */
fun Document.getTitle(): String? = querySelector("title")?.textContent?.trim()

/**
 * Gets the description of the document based on the `<meta name="description">` tag.
 * @return The description of the document, or null if not found.
 */
fun Document.getDescription(): String? {
    val meta = querySelector("meta[name='description']") ?: return null
    return meta["content"]?.trim()
}

/**
 * Gets the canonical URL of the document based on the `<link rel="canonical">` tag.
 * @return The canonical URL of the document, or null if not found.
 */
fun Document.getCanonicalUrl(): String? {
    val link = querySelector("link[rel='canonical']") ?: return null
    return link["href"]?.trim()?.takeIf { it.isNotEmpty() }
}

/**
 * Gets the charset of the document based on the `<meta charset>` or `<meta http-equiv="Content-Type">` tag.
 * @return The charset of the document, or null if not found.
 */
fun Document.getCharset(): String? {
    val meta = querySelector("meta[charset]") ?: return null
    return meta["charset"]?.trim()
        ?: meta["http-equiv"]?.takeIf { it.equals("Content-Type", ignoreCase = true) }
            ?.let { meta["content"]?.substringAfter("charset=")?.trim() }
}

/**
 * Gets the viewport settings of the document based on the `<meta name="viewport">` tag.
 * @return The viewport settings as a string, or null if not found.
 */
fun Document.getViewport(): String? {
    val meta = querySelector("meta[name='viewport']") ?: return null
    return meta["content"]?.trim()
}

/**
 * Gets the Open Graph metadata from the document.
 * @return A map of Open Graph properties and their values.
 */
fun Document.getOpenGraphMetadata(): Map<String, String> {
    val ogMetadata = mutableMapOf<String, String>()
    querySelectorAll("meta[property^='og:']").forEach { element ->
        val property = element["property"]?.substringAfter("og:")?.trim() ?: return@forEach
        val content = element["content"]?.trim() ?: return@forEach
        ogMetadata[property] = content
    }

    return ogMetadata
}

/**
 * Gets the Twitter Card metadata from the document.
 * @return A map of Twitter Card properties and their values.
 */
fun Document.getTwitterCardMetadata(): Map<String, String> {
    val twitterMetadata = mutableMapOf<String, String>()
    querySelectorAll("meta[name^='twitter:']").forEach { element ->
        val name = element["name"]?.substringAfter("twitter:")?.trim() ?: return@forEach
        val content = element["content"]?.trim() ?: return@forEach
        twitterMetadata[name] = content
    }

    return twitterMetadata
}

/**
 * Gets the icon links from the document.
 * This includes both `<link rel="icon">` and `<link rel="shortcut icon">`.
 * @return A list of icon URLs.
 */
fun Document.getIconLinks(): List<String> {
    return querySelectorAll("link[rel='icon'], link[rel='shortcut icon']").mapNotNull { element ->
        element["href"]?.trim()?.takeIf { it.isNotEmpty() }
    }
}

/**
 * Gets the favicon URL from the document.
 * This checks for common favicon link types such as:
 * - `<link rel='icon'>`
 * - `<link rel='shortcut icon'>`
 * - `<link rel='apple-touch-icon'>`
 * - `<link rel='apple-touch-icon-precomposed'>`
 * @return The URL of the favicon, or null if not found.
 */
fun Document.getFaviconUrl(): String? {
    return querySelector("link[rel='icon']")?.get("href")
        ?: querySelector("link[rel='shortcut icon']")?.get("href")
        ?: querySelector("link[rel='apple-touch-icon']")?.get("href")
        ?: querySelector("link[rel='apple-touch-icon-precomposed']")?.get("href")
}

/**
 * Gets the stylesheets linked in the document.
 * This includes `<link rel='stylesheet'>` tags.
 * @return A list of stylesheet URLs.
 */
fun Document.getStyleSheets(): List<String> {
    return querySelectorAll("link[rel='stylesheet']").mapNotNull { element ->
        element["href"]?.trim()?.takeIf { it.isNotEmpty() }
    }
}

/**
 * Gets all meta tags in the document as a map of name/property to content.
 * This includes both `<meta name='...'>` and `<meta property='...'>` tags.
 * @return A map of meta tag names/properties and their content.
 */
fun Document.getMetaTags(): Map<String, String> {
    val metaTags = mutableMapOf<String, String>()
    querySelectorAll("meta").forEach { element ->
        val name = element["name"]?.trim() ?: element["property"]?.trim() ?: return@forEach
        val content = element["content"]?.trim() ?: return@forEach
        metaTags[name] = content
    }
    return metaTags
}

/**
 * Gets all link tags in the document head as a list of hrefs.
 * @return A list of link hrefs.
 */
fun Document.getHeadLinks(): List<String> {
    return querySelectorAll("head link").mapNotNull { element ->
        element["href"]?.trim()?.takeIf { it.isNotEmpty() }
    }
}

/**
 * Gets all script tags in the document head as a list of srcs.
 * @return A list of script srcs.
 */
fun Document.getScripts(): List<String> {
    return querySelectorAll("script").mapNotNull { element ->
        element["src"]?.trim()?.takeIf { it.isNotEmpty() }
    }
}

// Body

/**
 * Gets the full text content of the document's body.
 * @return The text content of the body, or null if the body is not found.
 */
fun Document.getBodyText() = body.textContent.trim()

/**
 * Gets the main text content of the document.
 * This is typically the content inside the `<main>` tag or the body if no main tag exists.
 * @return The main text content of the document, or null if not found.
 */
fun Document.getMainText(): String {
    val main = querySelector("main") ?: body
    return main.textContent.trim()
}