package com.project.insole.features.notifications.domain

import kotlinx.coroutines.flow.StateFlow

/**
 * Access to the ACTIVE versioned thresholds config (TD-8).
 *
 * Offline-first: [config] always holds a usable config — the last one
 * fetched from the cloud, cached locally, falling back to the bundled
 * [ThresholdsConfig.DEFAULT]. [refresh] re-attempts the cloud fetch and
 * silently keeps the current config when offline or unchanged.
 */
interface ThresholdsConfigRepository {
    val config: StateFlow<ThresholdsConfig>

    /** Attempts a cloud fetch (GET /v1/config/thresholds). Never throws. */
    suspend fun refresh()
}
