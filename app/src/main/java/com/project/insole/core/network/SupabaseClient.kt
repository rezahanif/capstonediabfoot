package com.project.insole.core.network

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase Client wrapper.
 */
@Singleton
class SupabaseClient @Inject constructor() {
    
    private var isInitialized = false
    private var projectUrl: String? = null
    private var anonKey: String? = null

    /**
     * Initialize Supabase client with project credentials.
     */
    fun initialize(projectUrl: String, anonKey: String) {
        this.projectUrl = projectUrl
        this.anonKey = anonKey
        this.isInitialized = true
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
