package com.twilio.video.app.util

import android.content.Context
import android.content.SharedPreferences
import com.facebook.android.crypto.keychain.AndroidConceal
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain
import com.facebook.crypto.Crypto
import com.facebook.crypto.CryptoConfig
import android.util.Base64
import com.facebook.crypto.Entity
import com.facebook.soloader.SoLoader

class SecurePreferences(
    context: Context,
    private val preferences: SharedPreferences
) {

    private val entity: Entity = Entity.create(context.packageName)
    private val crypto: Crypto

    init {
        SoLoader.init(context, false)
        val keyChain = SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256)
        crypto = AndroidConceal.get().createCrypto256Bits(keyChain)
    }

    fun putString(key: String, value: String): Boolean {
        return preferences.edit().putString(key, encrypt(value)).commit()
    }

    fun getString(key: String): String? {
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