package com.project.insole.features.settings.data

import com.project.insole.core.network.InsoleApiClient
import com.project.insole.core.network.InsoleAuthService
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class PairingLinkResponse(
    val linked: Boolean,
    val doctorName: String
)

@Singleton
class DoctorPairingRemoteSource @Inject constructor(
    private val apiClient: InsoleApiClient,
    private val authService: InsoleAuthService
) {
    /**
     * Submits entered 8-character pairing code to the cloud API.
     */
    suspend fun linkDoctor(code: String): Result<PairingLinkResponse> {
        val sessionResult = authService.ensureSession()
        if (sessionResult.isFailure) {
            return Result.failure(sessionResult.exceptionOrNull()!!)
        }

        val body = JSONObject().apply {
            put("code", code)
        }.toString()

        val result = apiClient.post("/v1/pairing/link", body)
        return result.mapCatching { jsonStr ->
            val json = JSONObject(jsonStr)
            PairingLinkResponse(
                linked = json.getBoolean("linked"),
                doctorName = json.optString("doctor_name", "Your Doctor")
            )
        }
    }

    /**
     * Checks current pairing status with the cloud server.
     */
    suspend fun checkStatus(): Result<PairingLinkResponse> {
        val sessionResult = authService.ensureSession()
        if (sessionResult.isFailure) {
            return Result.failure(sessionResult.exceptionOrNull()!!)
        }

        val result = apiClient.get("/v1/pairing/status")
        return result.mapCatching { jsonStr ->
            val json = JSONObject(jsonStr)
            PairingLinkResponse(
                linked = json.getBoolean("linked"),
                doctorName = json.optString("doctor_name", "")
            )
        }
    }

    /**
     * Unlinks currently linked doctor.
     */
    suspend fun unlink(): Result<Unit> {
        val sessionResult = authService.ensureSession()
        if (sessionResult.isFailure) {
            return Result.failure(sessionResult.exceptionOrNull()!!)
        }

        val result = apiClient.delete("/v1/pairing")
        return result.mapCatching { }
    }
}
