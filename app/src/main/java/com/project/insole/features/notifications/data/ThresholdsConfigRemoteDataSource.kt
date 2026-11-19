package com.project.insole.features.notifications.data

import com.project.insole.core.network.InsoleApiClient
import com.project.insole.core.network.InsoleAuthService
import com.project.insole.features.notifications.domain.ThresholdsConfig
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote source for the versioned thresholds config.
 * Fetches active clinical thresholds from GET /v1/config/thresholds.
 */
@Singleton
class ThresholdsConfigRemoteDataSource @Inject constructor(
    private val apiClient: InsoleApiClient,
    private val authService: InsoleAuthService
) {
    suspend fun fetch(): Result<ThresholdsConfig> {
        val sessionResult = authService.ensureSession()
        if (sessionResult.isFailure) {
            return Result.failure(sessionResult.exceptionOrNull()!!)
        }

        val result = apiClient.get("/v1/config/thresholds")
        return result.mapCatching { jsonStr ->
            val json = JSONObject(jsonStr)
            ThresholdsConfig(
                version = json.getString("version"),
                thermalDeltaWarningC = json.getDouble("thermal_delta_warning_c").toFloat(),
                thermalPersistenceCount = json.getInt("thermal_persistence_count"),
                thermalCooldownMinutes = json.getInt("thermal_cooldown_minutes"),
                pressureWarning = json.getInt("pressure_warning"),
                pressureCooldownMinutes = json.getInt("pressure_cooldown_minutes"),
                pressurePersistenceCount = json.optInt("pressure_persistence_count", ThresholdsConfig.DEFAULT.pressurePersistenceCount),
                userWeightKg = if (json.has("user_weight_kg") && !json.isNull("user_weight_kg")) json.getDouble("user_weight_kg").toFloat() else null
            )
        }
    }
}
