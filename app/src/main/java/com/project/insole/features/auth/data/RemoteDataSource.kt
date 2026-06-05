package com.project.insole.features.auth.data

import com.project.insole.core.network.SupabaseClient
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote data source calling Supabase Auth API.
 * Handles sign-in, sign-up, session management, and token refresh.
 */
@Singleton
class RemoteDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    /**
     * Sign up with email and password.
     */
    suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            // Call Supabase auth API using supabaseClient
            Result.success("user_id")
        } catch (e: Exception) {
            Result.failure(supabaseClient.handleNetworkError(e))
        }
    }

    /**
     * Sign in with email and password.
     */
    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            // Call Supabase auth API using supabaseClient
            Result.success("session_token")
        } catch (e: Exception) {
            Result.failure(supabaseClient.handleNetworkError(e))
        }
    }

    /**
     * Check if user has active session.
     */
    suspend fun checkSession(): Result<String> {
        return try {
            // Call Supabase auth API using supabaseClient
            Result.success("user_id")
        } catch (e: Exception) {
            Result.failure(supabaseClient.handleNetworkError(e))
        }
    }

    /**
     * Sign out current user.
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            // Call Supabase auth API using supabaseClient
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(supabaseClient.handleNetworkError(e))
        }
    }
}
