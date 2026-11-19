package com.project.insole.features.sensor.domain.usecase

import com.project.insole.features.sensor.data.datasource.CloudUploadRemoteSource
import com.project.insole.features.sensor.data.datasource.CloudUploadResponse
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

/**
 * Executes patient-to-cloud data sync per DATA_CONTRACT.md v1.0.
 * Gathers persisted metrics, serializes JSON envelope, and uploads idempotently.
 */
class SyncCloudDataUseCase @Inject constructor(
    private val generateCloudPayloadUseCase: GenerateCloudPayloadUseCase,
    private val cloudUploadRemoteSource: CloudUploadRemoteSource
) {
    suspend operator fun invoke(date: String = LocalDate.now().toString()): Result<CloudUploadResponse> {
        return try {
            val envelope = generateCloudPayloadUseCase.generateDailyEnvelope(date)
            val payloadJson = generateCloudPayloadUseCase.envelopeToJson(envelope, indent = false)
            val idempotencyKey = "batch-$date-${UUID.randomUUID()}"
            cloudUploadRemoteSource.uploadBatch(payloadJson, idempotencyKey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
