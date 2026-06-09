package com.project.insole.core.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Placeholder for End-to-End Encryption (E2EE) using AES-256-GCM.
 * This remains dead code for now to avoid disrupting the current data flow.
 */
object EncryptionManager {

    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
    private const val KEY_LENGTH_BIT = 256

    /**
     * Generates a random AES-256 key.
     * In a real E2EE scenario, this key would be securely shared or derived.
     */
    fun generateSecretKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(KEY_LENGTH_BIT)
        return keyGen.generateKey()
    }

    /**
     * Encrypts a string (e.g., CSV content) using AES-256-GCM.
     * Returns a Base64 encoded string containing [IV + Ciphertext].
     */
    fun encrypt(data: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = ByteArray(IV_LENGTH_BYTE)
        SecureRandom().nextBytes(iv)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
        val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        
        // Prepend IV to the encrypted data
        val combined = iv + encryptedData
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    /**
     * Decrypts a Base64 encoded string using AES-256-GCM.
     */
    fun decrypt(encryptedBase64: String, secretKey: SecretKey): String {
        val combined = Base64.decode(encryptedBase64, Base64.DEFAULT)
        val iv = combined.sliceArray(0 until IV_LENGTH_BYTE)
        val encryptedData = combined.sliceArray(IV_LENGTH_BYTE until combined.size)
        
        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val decryptedData = cipher.doFinal(encryptedData)
        return String(decryptedData, Charsets.UTF_8)
    }
}
