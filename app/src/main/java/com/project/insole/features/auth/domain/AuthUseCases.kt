package com.project.insole.features.auth.domain

import com.project.insole.core.network.SessionManager
import com.project.insole.features.auth.data.AuthResult
import com.project.insole.features.auth.data.RemoteDataSource
import javax.inject.Inject

/**
 * Pure Kotlin domain use cases for authentication.
 * No Android or BLE dependencies here - only business logic.
 */

class LoginUseCase @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthResult> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
        }
        val result = remoteDataSource.signIn(trimmedEmail, password)
        return result.mapCatching { authResult ->
            if (authResult.role.lowercase() != "patient") {
                throw IllegalStateException("This app is for patients only. Doctors can sign in through the Doctor Web Portal.")
            }
            authResult
        }
    }
}

class LogoutUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): Result<Unit> {
        sessionManager.clearSession()
        return Result.success(Unit)
    }
}

class CheckSessionUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    operator fun invoke(): Boolean {
        return sessionManager.isAuthenticated()
    }
}
