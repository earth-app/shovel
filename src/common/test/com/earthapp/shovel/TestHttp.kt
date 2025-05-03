package com.earthapp.shovel

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.headers
import io.ktor.http.isSuccess
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class TestHttp {

    @Test
    fun testGet() = runTest {
        val res = client.get("https://httpbin.org/get") {
            headers {
                append("User-Agent", USER_AGENT)
            }
        }

        assertTrue { res.status.isSuccess() }
    }

    @Test
    fun testPost() = runTest {
        val res = client.post("https://httpbin.org/post") {
            headers {
                append("User-Agent", USER_AGENT)
            }
        }

        assertTrue { res.status.isSuccess() }
    }

    @Test
    fun testPut() = runTest {
        val res = client.put("https://httpbin.org/put") {
            headers {
                append("User-Agent", USER_AGENT)
            }
        }

        assertTrue { res.status.isSuccess() }
    }

    @Test
    fun testPatch() = runTest {
        val res = client.patch("https://httpbin.org/patch") {
            headers {
                append("User-Agent", USER_AGENT)
            }
        }

        assertTrue { res.status.isSuccess() }
    }

    @Test
    fun testDelete() = runTest {
        val res = client.delete("https://httpbin.org/delete") {
            headers {
                append("User-Agent", USER_AGENT)
            }
        }

        assertTrue { res.status.isSuccess() }
    }

}