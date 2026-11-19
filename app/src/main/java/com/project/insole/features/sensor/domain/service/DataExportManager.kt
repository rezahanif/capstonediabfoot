package com.project.insole.features.sensor.domain.service

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.project.insole.core.database.dao.DailyFootMetricDao
import com.project.insole.core.database.dao.StepDao
import com.project.insole.features.sensor.domain.usecase.GenerateCloudPayloadUseCase
import com.project.insole.features.settings.data.DoctorPairingStore
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles generation and device sharing of DATA_CONTRACT.md JSON envelopes and
 * clinical tabular CSV reports from local Room history.
 */
@Singleton
class DataExportManager @Inject constructor(
    private val generateCloudPayloadUseCase: GenerateCloudPayloadUseCase,
    private val dailyFootMetricDao: DailyFootMetricDao,
    private val stepDao: StepDao,
    private val doctorPairingStore: DoctorPairingStore
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun exportTodayJson(): String {
        val today = LocalDate.now().format(dateFormatter)
        val envelope = generateCloudPayloadUseCase.generateDailyEnvelope(today)
        return generateCloudPayloadUseCase.envelopeToJson(envelope)
    }

    suspend fun exportWeeklyJson(): String {
        val envelopes = generateCloudPayloadUseCase.generateWeeklyEnvelopes()
        return generateCloudPayloadUseCase.envelopesToJson(envelopes)
    }

    suspend fun exportWeeklyClinicalCsv(): String {
        val today = LocalDate.now()
        val patientId = doctorPairingStore.getOrCreatePatientId()

        val sb = StringBuilder()
        sb.append("Date,Patient_ID,Wear_Minutes,Total_Steps,Left_Steps,Right_Steps,Left_Avg_Temp_C,Right_Avg_Temp_C,Thermal_Delta_C,Left_Peak_Press_kPa,Right_Peak_Press_kPa,Clinical_Status\n")

        (6 downTo 0).forEach { offset ->
            val dateStr = today.minusDays(offset.toLong()).format(dateFormatter)
            val metric = dailyFootMetricDao.getMetricsForDate(dateStr)
            val step = stepDao.getStepsForDate(dateStr)

            val status = when {
                metric == null || (metric.leftSamples == 0 && metric.rightSamples == 0) -> "NO_DATA"
                metric.thermalAsymmetryC >= 2.2f -> "ATTENTION_ELEVATED_ASYMMETRY"
                metric.thermalAsymmetryC >= 1.5f -> "MILD_WARMTH"
                else -> "BALANCED"
            }

            val row = listOf(
                dateStr,
                patientId,
                (metric?.wearMinutes ?: 0).toString(),
                (step?.count ?: 0).toString(),
                (step?.leftCount ?: 0).toString(),
                (step?.rightCount ?: 0).toString(),
                "%.2f".format(Locale.US, metric?.leftAvgTemp ?: 0f),
                "%.2f".format(Locale.US, metric?.rightAvgTemp ?: 0f),
                "%.2f".format(Locale.US, metric?.thermalAsymmetryC ?: 0f),
                "%.1f".format(Locale.US, metric?.leftPeakPressureKpa ?: 0f),
                "%.1f".format(Locale.US, metric?.rightPeakPressureKpa ?: 0f),
                status
            ).joinToString(",")
            sb.append(row).append("\n")
        }

        return sb.toString()
    }

    /**
     * Writes content to a temporary cache file and invokes the native Android Sharesheet.
     */
    fun shareExport(
        context: Context,
        content: String,
        filename: String,
        mimeType: String,
        chooserTitle: String
    ) {
        val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val file = File(exportDir, filename)
        file.writeText(content, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "DiabFoot Medical Report ($filename)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
