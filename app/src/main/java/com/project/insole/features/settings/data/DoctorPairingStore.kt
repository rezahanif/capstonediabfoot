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

    init {
        // Pre-seed demo doctor on first launch so the user can verify the linked UI state
        if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
            prefs.edit()
                .putBoolean(KEY_INITIALIZED, true)
                .putString(KEY_CODE, "4AB79CD2")
                .putString(KEY_DOCTOR_NAME, "Dr. Sarah Jenkins, MD")
                .putLong(KEY_PAIRED_AT, System.currentTimeMillis() - 3 * 86400 * 1000L)
                .apply()
        }
    }

    fun save(code: String, doctorName: String) {
        prefs.edit()
            .putBoolean(KEY_INITIALIZED, true)
            .putString(KEY_CODE, code)
            .putString(KEY_DOCTOR_NAME, doctorName)
            .putLong(KEY_PAIRED_AT, System.currentTimeMillis())
            .apply()
    }

    fun getCode(): String? = prefs.getString(KEY_CODE, null)

    fun isLinked(): Boolean = !getCode().isNullOrBlank()

    fun getDoctorName(): String? = prefs.getString(KEY_DOCTOR_NAME, null)

    fun getPairedAt(): Long = prefs.getLong(KEY_PAIRED_AT, 0L)

    /**
     * Returns a stable pseudonymous patient ID for DATA_CONTRACT.md uploads.
     * Derived from doctor pairing code (e.g. "PT-4AB79CD2") or a local stable UUID.
     * Never contains real name, email, or Google identity.
     */
    fun getOrCreatePatientId(): String {
        val code = getCode()
        if (!code.isNullOrBlank()) {
            return "PT-${code.replace("-", "").uppercase()}"
        }
        val existing = prefs.getString(KEY_LOCAL_PATIENT_ID, null)
        if (existing != null) return existing
        val generated = "PT-" + java.util.UUID.randomUUID().toString().replace("-", "").take(8).uppercase()
        prefs.edit().putString(KEY_LOCAL_PATIENT_ID, generated).apply()
        return generated
    }

    fun clear() {
        prefs.edit()
            .remove(KEY_CODE)
            .remove(KEY_DOCTOR_NAME)
            .remove(KEY_PAIRED_AT)
            .putBoolean(KEY_INITIALIZED, true)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "doctor_pairing"
        const val KEY_INITIALIZED = "is_initialized"
        const val KEY_LOCAL_PATIENT_ID = "local_patient_id"
        const val KEY_CODE = "pairing_code"
        const val KEY_DOCTOR_NAME = "doctor_name"
        const val KEY_PAIRED_AT = "paired_at"
    }
}
