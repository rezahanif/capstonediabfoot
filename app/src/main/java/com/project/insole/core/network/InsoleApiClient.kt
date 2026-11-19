package com.project.insole.core.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Standard HTTP client for Insole Cloud API interactions.
 * Handles JWT authorization header injection, base URL resolution, and network error mapping.
 */
@Singleton
open class InsoleApiClient @Inject constructor(
    private val sessionManager: SessionManager
) {
    private val tag = "InsoleApiClient"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()

            // Attach Bearer token if session is authenticated
            val token = sessionManager.getToken()
            if (!token.isNullOrBlank() && original.header("Authorization") == null) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            val response = chain.proceed(requestBuilder.build())
            if (response.code == 401) {
                // Invalid or expired token
                logW("Received 401 Unauthorized for ${original.url} — clearing session")
                sessionManager.clearSession()
            }
            response
        }
        .build()

    private fun executeCall(
        method: String,
        url: String,
        jsonBody: String?,
        headers: Map<String, String>
    ): Result<String> {
        try {
            logD("$method $url" + (if (jsonBody != null) " (payload size: ${jsonBody.length} chars)" else ""))
            val reqBuilder = Request.Builder().url(url)
            headers.forEach { (k, v) -> reqBuilder.addHeader(k, v) }

            when (method) {
                "GET" -> reqBuilder.get()
                "POST" -> reqBuilder.post((jsonBody ?: "").toRequestBody(jsonMediaType))
                "DELETE" -> reqBuilder.delete()
            }

            val response = client.newCall(reqBuilder.build()).execute()
            val body = response.body?.string().orEmpty()
            logD("$method $url -> ${response.code}")
            return if (response.isSuccessful) {
                Result.success(body)
            } else {
                logE("$method $url failed with code ${response.code}: $body")
                Result.failure(ApiException(response.code, body.ifBlank { response.message }))
            }
        } catch (e: IOException) {
            logE("$method $url network error: ${e.message}", e)
            return Result.failure(IOException(e.toUserFriendlyMessage(), e))
        } catch (e: Exception) {
            logE("$method $url unexpected error: ${e.message}", e)
            return Result.failure(Exception(e.toUserFriendlyMessage(), e))
        }
    }

    /**
     * Localhost fallback is a debug-only convenience (ADB reverse). Release builds
     * fail honestly instead of silently probing the phone itself.
     */
    private fun shouldFallBackToLocal(primaryResult: Result<String>): Boolean =
        primaryResult.isFailure &&
            primaryResult.exceptionOrNull() is IOException &&
            ApiConfig.localFallbackEnabled &&
            ApiConfig.baseUrl.startsWith("https://")

    open suspend fun get(path: String, headers: Map<String, String> = emptyMap()): Result<String> = withContext(Dispatchers.IO) {
        val primaryResult = executeCall("GET", "${ApiConfig.baseUrl}$path", null, headers)
        if (shouldFallBackToLocal(primaryResult)) {
            val fallbackResult = executeCall("GET", "${ApiConfig.LOCAL_FALLBACK_URL}$path", null, headers)
            if (fallbackResult.isSuccess) {
                logW("Primary API failed with network error; fell back to local USB/dev bridge for GET $path")
                return@withContext fallbackResult
            }
        }
        primaryResult
    }

    open suspend fun post(
        path: String,
        jsonBody: String,
        headers: Map<String, String> = emptyMap()
    ): Result<String> = withContext(Dispatchers.IO) {
        val primaryResult = executeCall("POST", "${ApiConfig.baseUrl}$path", jsonBody, headers)
        if (shouldFallBackToLocal(primaryResult)) {
            val fallbackResult = executeCall("POST", "${ApiConfig.LOCAL_FALLBACK_URL}$path", jsonBody, headers)
            if (fallbackResult.isSuccess) {
                logW("Primary API failed with network error; fell back to local USB/dev bridge for POST $path")
                return@withContext fallbackResult
            }
        }
        primaryResult
    }

    open suspend fun delete(path: String, headers: Map<String, String> = emptyMap()): Result<String> = withContext(Dispatchers.IO) {
        val primaryResult = executeCall("DELETE", "${ApiConfig.baseUrl}$path", null, headers)
        if (shouldFallBackToLocal(primaryResult)) {
            val fallbackResult = executeCall("DELETE", "${ApiConfig.LOCAL_FALLBACK_URL}$path", null, headers)
            if (fallbackResult.isSuccess) {
                logW("Primary API failed with network error; fell back to local USB/dev bridge for DELETE $path")
                return@withContext fallbackResult
            }
        }
        primaryResult
    }

    private fun logD(msg: String) {
        try {
            Log.d(tag, msg)
        } catch (_: Throwable) {
            println("[$tag DEBUG] $msg")
        }
    }

    private fun logW(msg: String) {
        try {
            Log.w(tag, msg)
        } catch (_: Throwable) {
            println("[$tag WARN] $msg")
        }
    }

    private fun logE(msg: String, tr: Throwable? = null) {
        try {
            Log.e(tag, msg, tr)
        } catch (_: Throwable) {
            println("[$tag ERROR] $msg: $tr")
        }
    }
}

class ApiException(val code: Int, val rawMessage: String) : Exception(mapUserMessage(code, rawMessage)) {
    companion object {
        fun mapUserMessage(code: Int, raw: String): String = when (code) {
            401 -> "Incorrect email or password. Please try again."
            409 -> "An account with this email already exists. Try signing in instead."
            400 -> raw.trim().ifBlank { "Please check the information entered and try again." }
            403 -> "This app is for patients only. Doctors can sign in through the Doctor Web Portal."
            404 -> "Requested service or resource not found."
            500, 502, 503 -> "Server is temporarily unavailable. Please try again in a few moments."
            else -> raw.ifBlank { "Request failed ($code). Please try again." }
        }
    }
}

fun Throwable.toUserFriendlyMessage(): String = when (this) {
    is ApiException -> message ?: "Request failed. Please try again."
    is java.net.UnknownHostException -> "No internet connection. Please connect to Wi-Fi or mobile data."
    is java.net.ConnectException,
    is javax.net.ssl.SSLException,
    is java.net.SocketTimeoutException -> "Cannot reach the server. Please check your internet connection."
    is IllegalArgumentException,
    is IllegalStateException -> message ?: "Operation could not be completed."
    else -> {
        val msg = message.orEmpty()
        when {
            msg.contains("Unable to resolve host", ignoreCase = true) ||
            msg.contains("No address associated", ignoreCase = true) ->
                "No internet connection. Please connect to Wi-Fi or mobile data."
            msg.contains("failed to connect", ignoreCase = true) ||
            msg.contains("ECONNREFUSED", ignoreCase = true) ||
            msg.contains("SSL", ignoreCase = true) ||
            msg.contains("timeout", ignoreCase = true) ->
                "Cannot reach the server. Please check your internet connection."
            else -> msg.ifBlank { "Something went wrong. Please try again." }
        }
    }
}
