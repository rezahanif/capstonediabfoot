package com.project.insole.features.sensor.domain.usecase

import com.project.insole.core.network.SessionManager
import com.project.insole.core.ble.InsoleBleManager
import com.project.insole.core.database.dao.DailyFootMetricDao
import com.project.insole.core.database.dao.HourlyStepDao
import com.project.insole.core.database.dao.StepDao
import com.project.insole.features.sensor.domain.model.export.*
import com.project.insole.features.settings.data.DoctorPairingStore
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * Generates versioned cloud upload envelopes adhering strictly to cloud/DATA_CONTRACT.md v1.0.
 * Edge-computes daily clinical batches from Room database records (steps, hourly activity,
 * and daily foot thermal/pressure metrics).
 */
class GenerateCloudPayloadUseCase(
    private val dailyFootMetricDao: DailyFootMetricDao,
    private val stepDao: StepDao,
    private val hourlyStepDao: HourlyStepDao,
    private val deviceIdProvider: () -> String,
    private val patientIdProvider: () -> String
) {
    @Inject
    constructor(
        dailyFootMetricDao: DailyFootMetricDao,
        stepDao: StepDao,
        hourlyStepDao: HourlyStepDao,
        bleManager: InsoleBleManager,
        sessionManager: SessionManager,
        doctorPairingStore: DoctorPairingStore
    ) : this(
        dailyFootMetricDao = dailyFootMetricDao,
        stepDao = stepDao,
        hourlyStepDao = hourlyStepDao,
        deviceIdProvider = {
            val leftAddr = bleManager.connectedAddressFor(isLeft = true) ?: "UNPAIRED"
            val rightAddr = bleManager.connectedAddressFor(isLeft = false) ?: "UNPAIRED"
            "L:$leftAddr+R:$rightAddr"
        },
        patientIdProvider = {
            sessionManager.getPseudoId()?.takeIf { it.isNotBlank() }
                ?: doctorPairingStore.getOrCreatePatientId()
        }
    )

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun generateDailyEnvelope(dateStr: String): UploadEnvelope {
        val metric = dailyFootMetricDao.getMetricsForDate(dateStr)
        val step = stepDao.getStepsForDate(dateStr)
        val hourly = hourlyStepDao.getHourlyStepsForDate(dateStr).firstOrNull() ?: emptyList()

        val deviceId = deviceIdProvider()
        val patientId = patientIdProvider()

        // Estimate active walking minutes from hourly step counts (approx 80 steps/min)
        val activeMins = hourly.sumOf { (it.count / 80).coerceIn(0, 60) }

        val dailyActivity = BatchDailyActivity(
            totalSteps = step?.count ?: 0,
            leftSteps = step?.leftCount ?: 0,
            rightSteps = step?.rightCount ?: 0,
            activeMinutesEstimated = activeMins
        )

        val useCleanLeft = (metric?.cleanLeftSamples ?: 0) > 0
        val useCleanRight = (metric?.cleanRightSamples ?: 0) > 0
        val cleanWear = metric?.cleanWearMinutes ?: 0
        val effectiveWearMinutes = if (cleanWear > 0) cleanWear else metric?.wearMinutes ?: 0

        val leftTemp = SiteTemperature(
            minC = if (useCleanLeft) metric!!.cleanLeftMinTemp else metric?.leftMinTemp ?: 0f,
            maxC = if (useCleanLeft) metric!!.cleanLeftMaxTemp else metric?.leftMaxTemp ?: 0f,
            avgC = if (useCleanLeft) metric!!.cleanLeftAvgTemp else metric?.leftAvgTemp ?: 0f,
            samples = if (useCleanLeft) metric!!.cleanLeftSamples else metric?.leftSamples ?: 0,
            siteId = "MIDFOOT"
        )

        val rightTemp = SiteTemperature(
            minC = if (useCleanRight) metric!!.cleanRightMinTemp else metric?.rightMinTemp ?: 0f,
            maxC = if (useCleanRight) metric!!.cleanRightMaxTemp else metric?.rightMaxTemp ?: 0f,
            avgC = if (useCleanRight) metric!!.cleanRightAvgTemp else metric?.rightAvgTemp ?: 0f,
            samples = if (useCleanRight) metric!!.cleanRightSamples else metric?.rightSamples ?: 0,
            siteId = "MIDFOOT"
        )

        val episodes = mutableListOf<ClinicalEpisode>()
        if (metric != null && ((metric.cleanLeftSamples > 0 && metric.cleanRightSamples > 0) || (metric.leftSamples > 0 && metric.rightSamples > 0))) {
            val asymmetry = if (metric.cleanLeftSamples > 0 && metric.cleanRightSamples > 0) {
                metric.cleanThermalAsymmetryC
            } else {
                metric.thermalAsymmetryC
            }
            // Sustained 2-day hotspot (Armstrong/IWGDF) or legacy record if consecutive tracker is 0
            val isSustainedHotspot = metric.consecutiveThermalDays >= 2 || (metric.consecutiveThermalDays == 0 && asymmetry >= 2.2f)
            if (isSustainedHotspot && asymmetry >= 2.2f) {
                val warmerSide = if (metric.cleanLeftSamples > 0) {
                    if (metric.cleanLeftAvgTemp >= metric.cleanRightAvgTemp) "L" else "R"
                } else {
                    if (metric.leftAvgTemp >= metric.rightAvgTemp) "L" else "R"
                }
                episodes.add(
                    ClinicalEpisode(
                        type = "THERMAL_ASYMMETRY",
                        foot = metric.sustainedThermalSide?.take(1) ?: warmerSide,
                        siteOrRegion = "MIDFOOT",
                        start = "${dateStr}T00:00:00Z",
                        end = "${dateStr}T23:59:00Z",
                        durationMin = effectiveWearMinutes,
                        peakValue = asymmetry,
                        thresholdVersion = "v1"
                    )
                )
            }
            if (metric.leftPeakPressureKpa >= 200f) {
                episodes.add(
                    ClinicalEpisode(
                        type = "HIGH_PRESSURE",
                        foot = "L",
                        siteOrRegion = "MTH1",
                        start = "${dateStr}T00:00:00Z",
                        end = "${dateStr}T23:59:00Z",
                        durationMin = effectiveWearMinutes,
                        peakValue = metric.leftPeakPressureKpa,
                        thresholdVersion = "v1"
                    )
                )
            }
            if (metric.rightPeakPressureKpa >= 200f) {
                episodes.add(
                    ClinicalEpisode(
                        type = "HIGH_PRESSURE",
                        foot = "R",
                        siteOrRegion = "MTH1",
                        start = "${dateStr}T00:00:00Z",
                        end = "${dateStr}T23:59:00Z",
                        durationMin = effectiveWearMinutes,
                        peakValue = metric.rightPeakPressureKpa,
                        thresholdVersion = "v1"
                    )
                )
            }
        }

        val batch = DailyBatch(
            period = BatchPeriod(
                start = "${dateStr}T00:00:00Z",
                end = "${dateStr}T23:59:00Z"
            ),
            wearMinutes = metric?.wearMinutes ?: 0,
            dailyActivity = dailyActivity,
            temperature = TemperatureBlock(left = leftTemp, right = rightTemp),
            episodes = episodes,
            promptLog = emptyList(),
            riskLevel = metric?.riskLevel ?: "LOW",
            isValidSwell = metric?.isValidSwell ?: false
        )

        return UploadEnvelope(
            schemaVersion = "1.0",
            patientId = patientId,
            deviceId = deviceId,
            calibrationVersion = "none",
            generatedAt = Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
            batch = batch
        )
    }

    suspend fun generateWeeklyEnvelopes(): List<UploadEnvelope> {
        val today = LocalDate.now()
        return (6 downTo 0).map { offset ->
            val dateStr = today.minusDays(offset.toLong()).format(dateFormatter)
            generateDailyEnvelope(dateStr)
        }
    }

    /**
     * Serializes an UploadEnvelope into valid JSON matching DATA_CONTRACT.md schema exactly.
     * Implemented deterministically without third-party library bloat.
     */
    fun envelopeToJson(envelope: UploadEnvelope, indent: Boolean = true): String {
        val pad = if (indent) "  " else ""
        val nl = if (indent) "\n" else ""

        val b = envelope.batch
        val act = b.dailyActivity
        val temp = b.temperature

        val episodesJson = b.episodes.joinToString(",$nl") { ep ->
            """$pad$pad$pad{
$pad$pad$pad$pad"type": "${ep.type}",
$pad$pad$pad$pad"foot": "${ep.foot}",
$pad$pad$pad$pad"site_or_region": "${ep.siteOrRegion}",
$pad$pad$pad$pad"start": "${ep.start}",
$pad$pad$pad$pad"end": "${ep.end}",
$pad$pad$pad$pad"duration_min": ${ep.durationMin},
$pad$pad$pad$pad"peak_value": ${"%.2f".format(Locale.US, ep.peakValue)},
$pad$pad$pad$pad"threshold_version": "${ep.thresholdVersion}"
$pad$pad$pad}"""
        }

        return """{
$pad"schema_version": "${envelope.schemaVersion}",
$pad"patient_id": "${envelope.patientId}",
$pad"device_id": "${envelope.deviceId}",
$pad"calibration_version": "${envelope.calibrationVersion}",
$pad"generated_at": "${envelope.generatedAt}",
$pad"batch": {
$pad$pad"period": {
$pad$pad$pad"start": "${b.period.start}",
$pad$pad$pad"end": "${b.period.end}"
$pad$pad},
$pad$pad"wear_minutes": ${b.wearMinutes},
$pad$pad"daily_activity": {
$pad$pad$pad"total_steps": ${act.totalSteps},
$pad$pad$pad"left_steps": ${act.leftSteps},
$pad$pad$pad"right_steps": ${act.rightSteps},
$pad$pad$pad"active_minutes_estimated": ${act.activeMinutesEstimated}
$pad$pad},
$pad$pad"temperature": {
$pad$pad$pad"left": { "min_c": ${"%.2f".format(Locale.US, temp.left.minC)}, "max_c": ${"%.2f".format(Locale.US, temp.left.maxC)}, "avg_c": ${"%.2f".format(Locale.US, temp.left.avgC)}, "samples": ${temp.left.samples}, "site_id": "${temp.left.siteId}" },
$pad$pad$pad"right": { "min_c": ${"%.2f".format(Locale.US, temp.right.minC)}, "max_c": ${"%.2f".format(Locale.US, temp.right.maxC)}, "avg_c": ${"%.2f".format(Locale.US, temp.right.avgC)}, "samples": ${temp.right.samples}, "site_id": "${temp.right.siteId}" }
$pad$pad},
$pad$pad"episodes": [${if (episodesJson.isNotEmpty()) "$nl$episodesJson$nl$pad$pad" else ""}],
$pad$pad"prompt_log": [],
$pad$pad"risk_level": "${b.riskLevel}",
$pad$pad"is_valid_swell": ${b.isValidSwell}
$pad}
}"""
    }

    /**
     * Serializes multiple daily envelopes into a JSON list.
     */
    fun envelopesToJson(envelopes: List<UploadEnvelope>): String {
        return "[\n" + envelopes.joinToString(",\n") { envelopeToJson(it, indent = true) } + "\n]"
    }
}
