package com.twilio.video.app.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.facebook.android.crypto.keychain.AndroidConceal
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain
import com.facebook.crypto.Crypto
import com.facebook.crypto.CryptoConfig
import com.facebook.crypto.Entity
import com.facebook.soloader.SoLoader

class SecurePreferencesImpl(
    context: Context,
    private val preferences: SharedPreferences
) : SecurePreferences {

    private val entity: Entity = Entity.create(context.packageName)
    private val crypto: Crypto

    init {
        SoLoader.init(context, false)
        val keyChain = SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256)
        crypto = AndroidConceal.get().createCrypto256Bits(keyChain)
    }

    override fun putSecureString(key: String, value: String) {
        return preferences.edit().putString(key, encrypt(value)).apply()
    }

    override fun getSecureString(key: String): String? {
        val encryptedText: String? = preferences.getString(key, null)
        return if (encryptedText != null) decrypt(encryptedText) else null
    }

    private fun encrypt(plainText: String): String {
        val cipherText = crypto.encrypt(plainText.toByteArray(), entity)
        return Base64.encodeToString(cipherText, Base64.DEFAULT)
    }

    private fun decrypt(encryptedText: String): String {
        return String(crypto.decrypt(Base64.decode(encryptedText, Base64.DEFAULT), entity))
    }
}
