package com.project.insole.features.auth.domain

import com.project.insole.features.auth.data.AuthResult
import com.project.insole.features.auth.data.RemoteDataSource
import javax.inject.Inject

/**
 * Handles user registration via Insole Cloud API.
 */
class SignUpUseCase @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {
    suspend operator fun invoke(email: String, password: String, displayName: String = ""): Result<AuthResult> {
        val trimmed = email.trim()
        if (!trimmed.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        return remoteDataSource.signUp(trimmed, password, displayName).mapCatching { authResult ->
            if (authResult.role.lowercase() != "patient") {
                throw IllegalStateException("This app is for patients only. Doctors can sign in through the Doctor Web Portal.")
            }
            authResult
        }
    }
}
