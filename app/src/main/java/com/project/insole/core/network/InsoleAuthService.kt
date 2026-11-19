package com.project.insole.core.network

import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class InsoleAuthService @Inject constructor(
    private val apiClient: InsoleApiClient,
    private val sessionManager: SessionManager
) {
    /**
     * Authenticates with Google ID token.
     */
    open suspend fun loginWithGoogle(idToken: String): Result<Unit> {
        val payload = JSONObject().apply {
            put("id_token", idToken)
        }.toString()

        val result = apiClient.post("/v1/auth/google", payload)
        return result.mapCatching { body ->
            val json = JSONObject(body)
            val token = json.getString("token")
            val role = json.getString("role")
            val pseudoId = json.optString("pseudo_id", "")
            sessionManager.saveSession(token, role, pseudoId)
        }
    }

    /**
     * Development convenience authentication (auto-enabled when ALLOW_DEV_AUTH=true on server).
     */
    open suspend fun loginDevPatient(): Result<Unit> {
        return loginWithGoogle("dev-patient")
    }

    /**
     * Ensures an active session exists before executing patient operations.
     */
    open suspend fun ensureSession(): Result<Unit> {
        if (sessionManager.isAuthenticated()) {
            return Result.success(Unit)
        }
        return loginDevPatient()
    }
}
