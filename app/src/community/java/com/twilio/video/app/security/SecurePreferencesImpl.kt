package com.twilio.video.app.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

class SecurePreferencesImpl(
    context: Context,
    preferences: SharedPreferences,
) : SecurePreferences {

    private val securePreferences: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        securePreferences = EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun putSecureString(key: String, value: String) {
        securePreferences.edit { putString(key, value) }
    }

    override fun getSecureString(key: String): String? {
        return securePreferences.getString(key, null)
    }

}
