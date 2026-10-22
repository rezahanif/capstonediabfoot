package com.project.insole.features.settings.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local store for the patient↔doctor pairing.
 *
 * Offline-first: the pairing code the doctor shared is stored locally and
 * marked "pending confirmation" until the patient app can reach the cloud
 * API (cloud/server), which is what actually creates the doctor→patient
 * binding server-side. QR-scan pairing is tracked as TD-6.
 */
@Singleton
class DoctorPairingStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(code: String, doctorName: String) {
        prefs.edit()
            .putString(KEY_CODE, code)
            .putString(KEY_DOCTOR_NAME, doctorName)
            .putLong(KEY_PAIRED_AT, System.currentTimeMillis())
            .apply()
    }

    fun getCode(): String? = prefs.getString(KEY_CODE, null)

    fun getDoctorName(): String? = prefs.getString(KEY_DOCTOR_NAME, null)

    fun getPairedAt(): Long = prefs.getLong(KEY_PAIRED_AT, 0L)

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "doctor_pairing"
        const val KEY_CODE = "pairing_code"
        const val KEY_DOCTOR_NAME = "doctor_name"
        const val KEY_PAIRED_AT = "paired_at"
    }
}
