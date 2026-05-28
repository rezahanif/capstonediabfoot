package com.project.insole.core.network

/**
 * Supabase Client initialization and generic network error handling.
 * Provides singleton access to Supabase client for auth and database operations.
 */
object SupabaseClient {
    
    /**
     * Initialize Supabase client with project credentials.
     */
    fun initialize(projectUrl: String, anonKey: String) {
        // Supabase client initialization
    }

    /**
     * Handles generic network errors that may occur during API calls.
     */
    fun handleNetworkError(error: Exception): NetworkException {
        return when (error) {
            is java.net.ConnectException -> NetworkException.ConnectionError("Failed to connect to server")
            is java.net.SocketTimeoutException -> NetworkException.TimeoutError("Request timed out")
            else -> NetworkException.UnknownError(error.message ?: "Unknown error occurred")
        }
    }
}

sealed class NetworkException : Exception() {
    data class ConnectionError(override val message: String) : NetworkException()
    data class TimeoutError(override val message: String) : NetworkException()
    data class UnknownError(override val message: String) : NetworkException()
}
