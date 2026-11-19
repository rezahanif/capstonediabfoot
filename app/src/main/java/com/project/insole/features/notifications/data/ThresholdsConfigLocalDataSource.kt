package com.project.insole.features.notifications.data

import android.content.Context
import com.project.insole.features.notifications.domain.ThresholdsConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local cache of the last successfully fetched thresholds config.
 * Primitive SharedPreferences fields — no JSON dependency, JVM-safe.
 */
@Singleton
class ThresholdsConfigLocalDataSource @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Null = nothing cached yet; caller falls back to ThresholdsConfig.DEFAULT. */
    fun load(): ThresholdsConfig? {
        val version = prefs.getString(KEY_VERSION, null) ?: return null
        return ThresholdsConfig(
            version = version,
            thermalDeltaWarningC = prefs.getFloat(KEY_THERMAL_DELTA, ThresholdsConfig.DEFAULT.thermalDeltaWarningC),
            thermalPersistenceCount = prefs.getInt(KEY_THERMAL_PERSISTENCE, ThresholdsConfig.DEFAULT.thermalPersistenceCount),
            thermalCooldownMinutes = prefs.getInt(KEY_THERMAL_COOLDOWN_MIN, ThresholdsConfig.DEFAULT.thermalCooldownMinutes),
            pressureWarning = prefs.getInt(KEY_PRESSURE_WARNING, ThresholdsConfig.DEFAULT.pressureWarning),
            pressureCooldownMinutes = prefs.getInt(KEY_PRESSURE_COOLDOWN_MIN, ThresholdsConfig.DEFAULT.pressureCooldownMinutes),
            pressurePersistenceCount = prefs.getInt(KEY_PRESSURE_PERSISTENCE, ThresholdsConfig.DEFAULT.pressurePersistenceCount),
            userWeightKg = if (prefs.contains(KEY_USER_WEIGHT_KG)) prefs.getFloat(KEY_USER_WEIGHT_KG, 0f) else null
        )
    }

    fun save(config: ThresholdsConfig) {
        val editor = prefs.edit()
            .putString(KEY_VERSION, config.version)
            .putFloat(KEY_THERMAL_DELTA, config.thermalDeltaWarningC)
            .putInt(KEY_THERMAL_PERSISTENCE, config.thermalPersistenceCount)
            .putInt(KEY_THERMAL_COOLDOWN_MIN, config.thermalCooldownMinutes)
            .putInt(KEY_PRESSURE_WARNING, config.pressureWarning)
            .putInt(KEY_PRESSURE_COOLDOWN_MIN, config.pressureCooldownMinutes)
            .putInt(KEY_PRESSURE_PERSISTENCE, config.pressurePersistenceCount)

        if (config.userWeightKg != null) {
            editor.putFloat(KEY_USER_WEIGHT_KG, config.userWeightKg)
        } else {
            editor.remove(KEY_USER_WEIGHT_KG)
        }
        editor.apply()
    }

    private companion object {
        const val PREFS_NAME = "thresholds_config"
        const val KEY_VERSION = "version"
        const val KEY_THERMAL_DELTA = "thermal_delta_warning_c"
        const val KEY_THERMAL_PERSISTENCE = "thermal_persistence_count"
        const val KEY_THERMAL_COOLDOWN_MIN = "thermal_cooldown_minutes"
        const val KEY_PRESSURE_WARNING = "pressure_warning"
        const val KEY_PRESSURE_COOLDOWN_MIN = "pressure_cooldown_minutes"
        const val KEY_PRESSURE_PERSISTENCE = "pressure_persistence_count"
        const val KEY_USER_WEIGHT_KG = "user_weight_kg"
    }
}
