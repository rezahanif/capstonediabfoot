package com.project.insole.features.settings.domain

/**
 * Pure Kotlin domain use cases for settings.
 * No Android or BLE dependencies - only business logic.
 */

class ValidateSettingValueUseCase {
    operator fun invoke(key: String, value: String): Boolean {
        return when (key) {
            "pressure_threshold" -> value.toIntOrNull()?.let { it in 100..255 } ?: false
            "temperature_threshold" -> value.toFloatOrNull()?.let { it in 20f..50f } ?: false
            else -> true
        }
    }
}

class ResetSettingsToDefaultUseCase {
    operator fun invoke(): Map<String, String> {
        return mapOf(
            "pressure_threshold" to "200",
            "temperature_threshold" to "39",
            "notification_enabled" to "true",
            "data_sync_interval" to "300"
        )
    }
}
