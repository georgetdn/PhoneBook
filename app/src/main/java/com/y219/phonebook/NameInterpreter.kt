package com.y219.phonebook

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

internal data class ContactName(val first: String, val last: String) {
    companion object {
        private val namePart = Regex("""\p{L}[\p{L}\p{M}]*(?:[ .’'\-]\p{L}[\p{L}\p{M}]*)*\.?""")

        fun parse(first: String?, last: String?): ContactName? {
            val firstName = first?.trim() ?: return null
            val lastName = last?.trim() ?: return null
            if (firstName.length > 100 || lastName.length > 100 ||
                !namePart.matches(firstName) || !namePart.matches(lastName)
            ) return null
            return ContactName(firstName, lastName)
        }
    }
}

internal object NameInterpreter {
    // A null result requests clarification. Transport and protocol errors use the offline fallback.
    suspend fun interpret(question: String): ContactName? = withContext(Dispatchers.IO) {
        val endpoint = BuildConfig.INTERPRETATION_URL
        if (endpoint.isBlank()) throw IOException("Interpretation endpoint is not configured")
        val url = URL(endpoint)
        val localDebugEndpoint = BuildConfig.DEBUG && url.protocol == "http" &&
            (url.host == "10.0.2.2" || url.host == "127.0.0.1")
        if (url.protocol != "https" && !localDebugEndpoint) {
            throw IOException("Invalid interpretation endpoint")
        }

        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 3_000
            connection.readTimeout = 18_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            val body = JSONObject().put("question", question).toString().toByteArray(Charsets.UTF_8)
            connection.setFixedLengthStreamingMode(body.size)
            connection.outputStream.use { it.write(body) }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("Interpretation service failed")
            }
            val result = connection.inputStream.bufferedReader(Charsets.UTF_8).use {
                JSONObject(it.readText())
            }
            when (result.getString("status")) {
                "lookup" -> ContactName.parse(
                    result.opt("firstName") as? String, result.opt("lastName") as? String
                ) ?: throw IOException("Invalid name from interpretation service")
                "clarify" -> null
                else -> throw IOException("Invalid interpretation response")
            }
        } finally {
            connection.disconnect()
        }
    }
}
