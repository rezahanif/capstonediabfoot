package com.project.insole.features.auth.domain

import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Handles user registration.
 * Mocked for now, simulates a network delay and returns success.
 */
class SignUpUseCase @Inject constructor() {
    suspend operator fun invoke(email: String, password: String): Result<String> {
        delay(1500) // Simulate network delay
        return if (email.contains("@")) {
            Result.success("User registered successfully")
        } else {
            Result.failure(Exception("Invalid email format"))
        }
    }
}
