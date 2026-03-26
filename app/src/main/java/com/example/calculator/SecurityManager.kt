package com.example.calculator

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

/**
 * Manages password setup and verification using encrypted shared preferences
 * Passwords are hashed using SHA-256 before being stored
 */
class SecurityManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedSharedPrefs = EncryptedSharedPreferences.create(
        context,
        "secret_password_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val PASSWORD_HASH_KEY = "password_hash"
        private const val PASSWORD_SET_FLAG_KEY = "password_set"
    }

    /**
     * Check if password has been set
     */
    fun isPasswordSet(): Boolean {
        return encryptedSharedPrefs.getBoolean(PASSWORD_SET_FLAG_KEY, false)
    }

    /**
     * Set the password (hashed) securely
     */
    fun setPassword(password: String) {
        val hashedPassword = hashPassword(password)
        encryptedSharedPrefs.edit().apply {
            putString(PASSWORD_HASH_KEY, hashedPassword)
            putBoolean(PASSWORD_SET_FLAG_KEY, true)
            apply()
        }
    }

    /**
     * Verify if the provided password matches the stored one
     */
    fun verifyPassword(password: String): Boolean {
        if (!isPasswordSet()) return false
        val storedHash = encryptedSharedPrefs.getString(PASSWORD_HASH_KEY, "")
        val providedHash = hashPassword(password)
        return storedHash == providedHash
    }

    /**
     * Hash password using SHA-256
     */
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Clear password (for reset functionality)
     */
    fun clearPassword() {
        encryptedSharedPrefs.edit().apply {
            remove(PASSWORD_HASH_KEY)
            putBoolean(PASSWORD_SET_FLAG_KEY, false)
            apply()
        }
    }
}
