package com.project.insole.features.settings.data

import com.project.insole.core.network.InsoleApiClient
import com.project.insole.core.network.InsoleAuthService
import com.project.insole.core.network.SessionManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DoctorPairingRemoteSourceTest {

    private var mockPostResult: Result<String> = Result.success("{\"linked\":true,\"doctor_name\":\"Dr. Sarah\"}")
    private var mockGetResult: Result<String> = Result.success("{\"linked\":true,\"doctor_name\":\"Dr. Sarah\"}")
    private var mockDeleteResult: Result<String> = Result.success("{\"linked\":false}")

    private lateinit var fakeApiClient: InsoleApiClient
    private lateinit var fakeAuthService: InsoleAuthService
    private lateinit var remoteSource: DoctorPairingRemoteSource

    @Before
    fun setUp() {
        val fakeSession = object : SessionManager(prefs = null) {
            override fun isAuthenticated(): Boolean = true
            override fun getToken(): String = "mock-token"
        }

        fakeApiClient = object : InsoleApiClient(fakeSession) {
            override suspend fun post(path: String, jsonBody: String, headers: Map<String, String>): Result<String> {
                return mockPostResult
            }

            override suspend fun get(path: String, headers: Map<String, String>): Result<String> {
                return mockGetResult
            }

            override suspend fun delete(path: String, headers: Map<String, String>): Result<String> {
                return mockDeleteResult
            }
        }

        fakeAuthService = object : InsoleAuthService(fakeApiClient, fakeSession) {
            override suspend fun ensureSession(): Result<Unit> = Result.success(Unit)
        }

        remoteSource = DoctorPairingRemoteSource(fakeApiClient, fakeAuthService)
    }

    @Test
    fun `linkDoctor successfully parses doctor confirmation`() = runBlocking {
        mockPostResult = Result.success("{\"linked\":true,\"doctor_name\":\"Dr. Clinic Admin\"}")

        val result = remoteSource.linkDoctor("Z3AE2YV8")
        assertTrue(result.isSuccess)
        val response = result.getOrNull()!!
        assertTrue(response.linked)
        assertEquals("Dr. Clinic Admin", response.doctorName)
    }

    @Test
    fun `checkStatus returns current linked doctor`() = runBlocking {
        mockGetResult = Result.success("{\"linked\":true,\"doctor_name\":\"Dr. Sarah\"}")

        val result = remoteSource.checkStatus()
        assertTrue(result.isSuccess)
        val response = result.getOrNull()!!
        assertTrue(response.linked)
        assertEquals("Dr. Sarah", response.doctorName)
    }

    @Test
    fun `unlink returns success`() = runBlocking {
        mockDeleteResult = Result.success("{\"linked\":false}")

        val result = remoteSource.unlink()
        assertTrue(result.isSuccess)
    }
}
