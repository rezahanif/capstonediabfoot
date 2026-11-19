package com.project.insole.core.network

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages patient authentication session state and JWT token persistence.
 */
@Singleton
open class SessionManager(
    private val prefs: SharedPreferences?
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    open fun getToken(): String? = prefs?.getString(KEY_TOKEN, null)

    open fun getPseudoId(): String? = prefs?.getString(KEY_PSEUDO_ID, null)

    open fun getRole(): String? = prefs?.getString(KEY_ROLE, null)

    open fun isAuthenticated(): Boolean = !getToken().isNullOrBlank()

    open fun saveSession(token: String, role: String, pseudoId: String) {
        prefs?.edit()
            ?.putString(KEY_TOKEN, token)
            ?.putString(KEY_ROLE, role)
            ?.putString(KEY_PSEUDO_ID, pseudoId)
            ?.apply()
    }

    open fun clearSession() {
        prefs?.edit()?.clear()?.apply()
    }

    companion object {
        private const val PREFS_NAME = "insole_cloud_session"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_ROLE = "user_role"
        private const val KEY_PSEUDO_ID = "pseudo_id"
    }
}
