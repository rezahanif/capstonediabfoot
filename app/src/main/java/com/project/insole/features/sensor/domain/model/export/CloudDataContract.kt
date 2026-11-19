package com.project.insole.features.sensor.domain.model.export

/**
 * Kotlin data models representing the Patient → Doctor Data Contract v1.0
 * specified in cloud/DATA_CONTRACT.md.
 *
 * All clinical processing happens on the edge (the patient app).
 * The cloud is a dumb, idempotent JSONB store.
 */

data class UploadEnvelope(
    val schemaVersion: String = "1.0",
    val patientId: String,
    val deviceId: String,
    val calibrationVersion: String = "none",
    val generatedAt: String, // ISO-8601 UTC
    val batch: DailyBatch
)

data class DailyBatch(
    val period: BatchPeriod,
    val wearMinutes: Int,
    val dailyActivity: BatchDailyActivity,
    val temperature: TemperatureBlock,
    val episodes: List<ClinicalEpisode> = emptyList(),
    val promptLog: List<PromptLogItem> = emptyList(),
    val riskLevel: String = "LOW",
    val isValidSwell: Boolean = false
)

data class BatchPeriod(
    val start: String, // ISO-8601 UTC: e.g. "2026-10-25T00:00:00Z"
    val end: String    // ISO-8601 UTC: e.g. "2026-10-25T23:59:00Z"
)

data class BatchDailyActivity(
    val totalSteps: Int,
    val leftSteps: Int,
    val rightSteps: Int,
    val activeMinutesEstimated: Int
)

data class TemperatureBlock(
    val left: SiteTemperature,
    val right: SiteTemperature
)

data class SiteTemperature(
    val minC: Float,
    val maxC: Float,
    val avgC: Float,
    val samples: Int,
    val siteId: String = "MIDFOOT" // Confirmed NTC placement per hardware spec
)

data class ClinicalEpisode(
    val type: String, // "THERMAL_ASYMMETRY" | "HIGH_PRESSURE"
    val foot: String, // "L" | "R"
    val siteOrRegion: String, // "MIDFOOT" | "HALLUX" | "MTH1" | "MTH2" | "MTH4" | "HEEL"
    val start: String,
    val end: String,
    val durationMin: Int,
    val peakValue: Float, // ΔT °C for thermal; peak kPa for pressure
    val thresholdVersion: String = "v1"
)

data class PromptLogItem(
    val promptId: String, // e.g. "THERMAL_L", "PRESSURE_R"
    val shownAt: String,
    val thresholdVersion: String = "v1",
    val relatedEpisodeStart: String? = null
)
