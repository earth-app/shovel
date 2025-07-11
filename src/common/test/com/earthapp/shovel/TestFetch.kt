package com.earthapp.shovel

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class TestFetch {

    @Test
    fun testFetchDocument() = runTest {
        val url = "https://example.com"
        val doc = url.fetchDocument()

        assertEquals(url, doc.url, "URL mismatch: expected $url, got ${doc.url}")
        assertEquals("Example Domain", doc.title, "Title mismatch: expected 'Example Domain', got '${doc.title}'")

        val body = doc.body
        assertFalse { body.outerHTML.isEmpty() }
        assertFalse { body.querySelectorAll("p").isEmpty() }
    }

}