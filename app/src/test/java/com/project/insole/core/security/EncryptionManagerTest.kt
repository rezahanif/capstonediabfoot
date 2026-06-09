package com.project.insole.core.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import javax.crypto.SecretKey

/**
 * Unit test for [EncryptionManager].
 * Verifies that data can be encrypted and decrypted correctly using AES-256.
 */
class EncryptionManagerTest {

    @Test
    fun `encryption and decryption should return original string`() {
        // Given
        val originalData = "Timestamp,WalkState,StepCount\n2023-10-27 10:00:00,WALKING,150"
        val secretKey: SecretKey = EncryptionManager.generateSecretKey()

        // When
        val encryptedData = EncryptionManager.encrypt(originalData, secretKey)
        val decryptedData = EncryptionManager.decrypt(encryptedData, secretKey)

        // Then
        assertNotEquals("Encrypted data should not match original", originalData, encryptedData)
        assertEquals("Decrypted data should match original", originalData, decryptedData)
    }

    @Test
    fun `encryption should produce different results for same input due to IV`() {
        // Given
        val data = "Medical data"
        val key = EncryptionManager.generateSecretKey()

        // When
        val encrypted1 = EncryptionManager.encrypt(data, key)
        val encrypted2 = EncryptionManager.encrypt(data, key)

        // Then
        assertNotEquals("Each encryption should have a unique IV", encrypted1, encrypted2)
    }
}
