package com.project.insole.features.auth.data

import com.project.insole.core.network.InsoleApiClient
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class AuthResult(
    val token: String,
    val role: String,
    val pseudoId: String,
    val displayName: String
)

/**
 * Remote data source for user authentication interacting with Insole Cloud API.
 * Communicates with POST /v1/auth/login and POST /v1/auth/register.
 */
@Singleton
class RemoteDataSource @Inject constructor(
    private val apiClient: InsoleApiClient
) {
    /**
     * Authenticates with email and password.
     */
    suspend fun signIn(email: String, password: String): Result<AuthResult> {
        val payload = JSONObject().apply {
            put("email", email)
            put("password", password)
        }.toString()

        val result = apiClient.post("/v1/auth/login", payload)
        return result.mapCatching { body ->
            val json = JSONObject(body)
            AuthResult(
                token = json.getString("token"),
                role = json.getString("role"),
                pseudoId = json.optString("pseudo_id", ""),
                displayName = json.optString("display_name", "")
            )
        }
    }

    /**
     * Registers a new patient account with email, password, and display name.
     */
    suspend fun signUp(email: String, password: String, displayName: String = ""): Result<AuthResult> {
        val payload = JSONObject().apply {
            put("email", email)
            put("password", password)
            if (displayName.isNotBlank()) {
                put("display_name", displayName)
            }
        }.toString()

        val result = apiClient.post("/v1/auth/register", payload)
        return result.mapCatching { body ->
            val json = JSONObject(body)
            AuthResult(
                token = json.getString("token"),
                role = json.getString("role"),
                pseudoId = json.optString("pseudo_id", ""),
                displayName = json.optString("display_name", "")
            )
        }
    }
}
