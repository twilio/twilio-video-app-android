package com.twilio.audioswitch

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.twilio.audioswitch.android.LogWrapper

private const val TAG = "PhoneAudioDeviceManager"

internal class PhoneAudioDeviceManager(
    private val context: Context,
    private val logger: LogWrapper,
    private val audioManager: AudioManager
) {

    fun hasEarpiece(): Boolean {
        val hasEarpiece = context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
                if (hasEarpiece) {
                    logger.d(TAG, "Earpiece available")
                }
        return hasEarpiece
    }

    fun hasSpeakerphone(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                context.packageManager
                        .hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                if (device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                    logger.d(TAG, "Speakerphone available")
                    return true
                }
            }
            false
        } else {
            logger.d(TAG, "Speakerphone available")
            true
        }
    }
}