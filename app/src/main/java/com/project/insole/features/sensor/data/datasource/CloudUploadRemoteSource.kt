package com.project.insole.features.sensor.data.datasource

import com.project.insole.core.network.InsoleApiClient
import com.project.insole.core.network.InsoleAuthService
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class CloudUploadResponse(
    val stored: Boolean,
    val deduplicated: Boolean
)

@Singleton
class CloudUploadRemoteSource @Inject constructor(
    private val apiClient: InsoleApiClient,
    private val authService: InsoleAuthService
) {
    /**
     * Uploads versioned JSON envelope to POST /v1/uploads with Idempotency-Key.
     */
    suspend fun uploadBatch(payloadJson: String, idempotencyKey: String): Result<CloudUploadResponse> {
        val sessionResult = authService.ensureSession()
        if (sessionResult.isFailure) {
            return Result.failure(sessionResult.exceptionOrNull()!!)
        }

        val headers = mapOf("Idempotency-Key" to idempotencyKey)
        val result = apiClient.post("/v1/uploads", payloadJson, headers)

        return result.mapCatching { jsonStr ->
            val json = JSONObject(jsonStr)
            CloudUploadResponse(
                stored = json.optBoolean("stored", true),
                deduplicated = json.optBoolean("deduplicated", false)
            )
        }
    }
}
