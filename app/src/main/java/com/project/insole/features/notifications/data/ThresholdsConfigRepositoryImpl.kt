package com.project.insole.features.notifications.data

import com.project.insole.features.notifications.domain.ThresholdsConfig
import com.project.insole.features.notifications.domain.ThresholdsConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first thresholds config (TD-8): starts from the local cache or
 * the bundled defaults, and re-attempts the cloud fetch on [refresh].
 * A failed or invalid fetch never changes the active config.
 */
@Singleton
class ThresholdsConfigRepositoryImpl @Inject constructor(
    private val localDataSource: ThresholdsConfigLocalDataSource,
    private val remoteDataSource: ThresholdsConfigRemoteDataSource
) : ThresholdsConfigRepository {

    private val _config = MutableStateFlow(localDataSource.load() ?: ThresholdsConfig.DEFAULT)
    override val config: StateFlow<ThresholdsConfig> = _config

    override suspend fun refresh() {
        remoteDataSource.fetch().onSuccess { remote ->
            // A versionless or blank config is invalid — never adopt it.
            if (remote.version.isNotBlank()) {
                localDataSource.save(remote)
                _config.value = remote
            }
        }
        // Failure (offline / scaffold) → keep serving the cached config.
    }
}
