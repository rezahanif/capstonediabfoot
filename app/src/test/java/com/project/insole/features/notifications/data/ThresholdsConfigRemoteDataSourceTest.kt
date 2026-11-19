package com.project.insole.features.notifications.data

import com.project.insole.core.network.InsoleApiClient
import com.project.insole.core.network.InsoleAuthService
import com.project.insole.core.network.SessionManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ThresholdsConfigRemoteDataSourceTest {

    private var mockGetResult: Result<String> = Result.success("""
        {
          "version": "v1.2",
          "thermal_delta_warning_c": 2.2,
          "thermal_persistence_count": 4,
          "thermal_cooldown_minutes": 45,
          "pressure_warning": 210,
          "pressure_cooldown_minutes": 35
        }
    """.trimIndent())

    private lateinit var fakeApiClient: InsoleApiClient
    private lateinit var fakeAuthService: InsoleAuthService
    private lateinit var dataSource: ThresholdsConfigRemoteDataSource

    @Before
    fun setUp() {
        val fakeSession = object : SessionManager(prefs = null) {
            override fun isAuthenticated(): Boolean = true
            override fun getToken(): String = "mock-token"
        }

        fakeApiClient = object : InsoleApiClient(fakeSession) {
            override suspend fun get(path: String, headers: Map<String, String>): Result<String> {
                return mockGetResult
            }
        }

        fakeAuthService = object : InsoleAuthService(fakeApiClient, fakeSession) {
            override suspend fun ensureSession(): Result<Unit> = Result.success(Unit)
        }

        dataSource = ThresholdsConfigRemoteDataSource(fakeApiClient, fakeAuthService)
    }

    @Test
    fun `fetch correctly parses cloud thresholds`() = runBlocking {
        val result = dataSource.fetch()
        assertTrue(result.isSuccess)
        val config = result.getOrNull()!!
        assertEquals("v1.2", config.version)
        assertEquals(2.2f, config.thermalDeltaWarningC, 0.001f)
        assertEquals(4, config.thermalPersistenceCount)
        assertEquals(45, config.thermalCooldownMinutes)
        assertEquals(210, config.pressureWarning)
        assertEquals(35, config.pressureCooldownMinutes)
    }
}
