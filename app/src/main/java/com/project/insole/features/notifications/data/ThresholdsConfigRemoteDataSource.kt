package com.project.insole.features.notifications.data

import com.project.insole.features.notifications.domain.ThresholdsConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote source for the versioned thresholds config.
 *
 * Endpoint contract (served by cloud/server): `GET /v1/config/thresholds`
 *
 * ```json
 * {"version":"v1","thermal_delta_warning_c":2.2,"thermal_persistence_count":3,
 *  "thermal_cooldown_minutes":60,"pressure_warning":200,"pressure_cooldown_minutes":30}
 * ```
 *
 * SCAFFOLD (TD-4/TD-8 pattern): the app has no HTTP client yet — the sync
 * module will implement [fetch] against the endpoint above and map the
 * snake_case JSON onto [ThresholdsConfig]. Until then this always fails and
 * the repository keeps serving the cached/bundled config.
 */
@Singleton
class ThresholdsConfigRemoteDataSource @Inject constructor() {

    suspend fun fetch(): Result<ThresholdsConfig> {
        return Result.failure(
            IllegalStateException("Cloud sync module not wired yet — no HTTP client in the app")
        )
    }
}
