package com.twilio.audioswitch

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.media.AudioManager
import com.twilio.audioswitch.AudioDeviceSelector.State.ACTIVATED
import com.twilio.audioswitch.AudioDeviceSelector.State.STARTED
import com.twilio.audioswitch.AudioDeviceSelector.State.STOPPED
import com.twilio.audioswitch.android.BuildWrapper
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.bluetooth.BluetoothController
import com.twilio.audioswitch.bluetooth.BluetoothDeviceConnectionListener
import kotlin.collections.ArrayList

private const val TAG = "AudioDeviceSelector"

/**
 * This class enables developers to enumerate available audio devices and select which device audio
 * should be routed to.
 */
class AudioDeviceSelector internal constructor(
    private val logger: LogWrapper,
    private val audioDeviceManager: AudioDeviceManager,
    private val wiredHeadsetReceiver: WiredHeadsetReceiver,
    private val bluetoothController: BluetoothController?
) {

    internal var audioDeviceChangeListener: AudioDeviceChangeListener? = null
    private var selectedDevice: AudioDevice? = null
    private var userSelectedDevice: AudioDevice? = null
    private var wiredHeadsetAvailable = false
    private val mutableAudioDevices = ArrayList<AudioDevice>()

    private val EARPIECE_AUDIO_DEVICE = AudioDevice(AudioDevice.Type.EARPIECE, "Earpiece")
    private val SPEAKERPHONE_AUDIO_DEVICE = AudioDevice(AudioDevice.Type.SPEAKERPHONE, "Speakerphone")
    private val WIRED_HEADSET_AUDIO_DEVICE = AudioDevice(AudioDevice.Type.WIRED_HEADSET, "Wired Headset")
    private var bluetoothAudioDevice: AudioDevice? = null
    internal var state: State = STOPPED
    internal enum class State {
        STARTED, ACTIVATED, STOPPED
    }
    internal val bluetoothDeviceConnectionListener = object : BluetoothDeviceConnectionListener {
        override fun onBluetoothConnected(
            bluetoothDevice: BluetoothDevice
        ) {
            bluetoothAudioDevice = AudioDevice(
                    AudioDevice.Type.BLUETOOTH,
                    bluetoothDevice.name)
            if (state == ACTIVATED) {
                userSelectedDevice = bluetoothAudioDevice
            }
            enumerateDevices()
        }

        override fun onBluetoothDisconnected() {
            bluetoothAudioDevice = null
            enumerateDevices()
        }
    }
    internal val wiredDeviceConnectionListener = object : WiredDeviceConnectionListener {
        override fun onDeviceConnected() {
            wiredHeadsetAvailable = true
            logger.d(TAG, "Wired Headset available")
            if (this@AudioDeviceSelector.state == ACTIVATED) {
                userSelectedDevice = WIRED_HEADSET_AUDIO_DEVICE
            }
            enumerateDevices()
        }

        override fun onDeviceDisconnected() {
            wiredHeadsetAvailable = false
            enumerateDevices()
        }
    }

    companion object {
        fun newInstance(context: Context): AudioDeviceSelector {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val logger = LogWrapper()
            val audioDeviceManager =
                    AudioDeviceManager(context, logger, audioManager, BuildWrapper())
            return AudioDeviceSelector(
                    logger,
                    audioDeviceManager,
                    WiredHeadsetReceiver(context, logger),
                    BluetoothController.newInstance(context, logger, audioDeviceManager)
            )
        }
    }

    /**
     * Starts listening for audio device changes. **Note:** When audio device listening is no
     * longer needed, [AudioDeviceSelector.stop] should be called in order to prevent a
     * memory leak.
     *
     * @param listener receives audio device change events
     */
    fun start(listener: AudioDeviceChangeListener) {
        audioDeviceChangeListener = listener
        when (state) {
            STOPPED -> {
                bluetoothController?.start(bluetoothDeviceConnectionListener)
                wiredHeadsetReceiver.start(wiredDeviceConnectionListener)
                enumerateDevices()
                state = STARTED
            }
            else -> {
                logger.d(TAG, "Redundant start() invocation while already in the started or activated state")
            }
        }
    }

    /**
     * Stops listening for audio device changes if [ ][AudioDeviceSelector.start] has already been invoked. [ ][AudioDeviceSelector.deactivate] will also get called if a device has been activated with
     * [AudioDeviceSelector.activate].
     */
    fun stop() {
        when (state) {
            ACTIVATED -> {
                deactivate()
                closeListeners()
            }
            STARTED -> {
                closeListeners()
            }
            STOPPED -> {
                logger.d(TAG, "Redundant stop() invocation while already in the stopped state")
            }
        }
    }

    /**
     * Performs audio routing and unmuting on the selected device from [ ][AudioDeviceSelector.selectDevice]. Audio focus is also acquired for the client
     * application. **Note:** [AudioDeviceSelector.deactivate] should be invoked to
     * restore the prior audio state.
     */
    fun activate() {
        when (state) {
            STARTED -> {
                audioDeviceManager.cacheAudioState()

                // Always set mute to false for WebRTC
                audioDeviceManager.mute(false)
                audioDeviceManager.setAudioFocus()
                if (selectedDevice != null) {
                    activate(selectedDevice!!)
                }
                state = ACTIVATED
            }
            ACTIVATED -> // Activate the newly selected device
                if (selectedDevice != null) {
                    activate(selectedDevice!!)
                }
            STOPPED -> throw IllegalStateException()
        }
    }

    private fun activate(audioDevice: AudioDevice) {
        when (audioDevice.type) {
            AudioDevice.Type.BLUETOOTH -> {
                audioDeviceManager.enableSpeakerphone(false)
                bluetoothController?.activate()
            }
            AudioDevice.Type.EARPIECE, AudioDevice.Type.WIRED_HEADSET -> {
                audioDeviceManager.enableSpeakerphone(false)
                bluetoothController?.deactivate()
            }
            AudioDevice.Type.SPEAKERPHONE -> {
                audioDeviceManager.enableSpeakerphone(true)
                bluetoothController?.deactivate()
            }
        }
    }

    /**
     * Restores the audio state prior to calling [AudioDeviceSelector.activate] and removes
     * audio focus from the client application.
     */
    fun deactivate() {
        when (state) {
            ACTIVATED -> {
                bluetoothController?.deactivate()

                // Restore stored audio state
                audioDeviceManager.restoreAudioState()
                state = STARTED
            }
            STARTED, STOPPED -> {
            }
        }
    }

    /**
     * Selects the desired [AudioDevice]. If the provided [AudioDevice] is not
     * available, no changes are made. If the provided [AudioDevice] is null, an [ ] is chosen based on the following preference: Bluetooth, Wired Headset,
     * Microphone, Speakerphone.
     *
     * @param audioDevice The [AudioDevice] to use
     */
    fun selectDevice(audioDevice: AudioDevice?) {
        userSelectedDevice = audioDevice
        enumerateDevices()
    }

    /**
     * Retrieves the selected [AudioDevice] from [ ][AudioDeviceSelector.selectDevice]
     *
     * @return the selected [AudioDevice]
     */
    val selectedAudioDevice: AudioDevice?
        get() {
            return if (selectedDevice != null) AudioDevice(selectedDevice!!.type, selectedDevice!!.name) else null
        }

    /**
     * Retrieves the current list of available [AudioDevice]s.
     *
     * @return the current list of [AudioDevice]s
     */
    val availableAudioDevices: List<AudioDevice> = mutableAudioDevices

    private fun enumerateDevices() {
        mutableAudioDevices.clear()
        bluetoothAudioDevice?.let { mutableAudioDevices.add(it) }
        if (wiredHeadsetAvailable) {
            mutableAudioDevices.add(WIRED_HEADSET_AUDIO_DEVICE)
        }
        if (audioDeviceManager.hasEarpiece() && !wiredHeadsetAvailable) {
            mutableAudioDevices.add(EARPIECE_AUDIO_DEVICE)
        }
        if (audioDeviceManager.hasSpeakerphone()) {
            mutableAudioDevices.add(SPEAKERPHONE_AUDIO_DEVICE)
        }

        // Check whether the user selected device is still present
        if (!userSelectedDevicePresent(mutableAudioDevices)) {
            userSelectedDevice = null
        }

        // Select the audio device
        selectedDevice = if (userSelectedDevice != null && userSelectedDevicePresent(mutableAudioDevices)) {
            userSelectedDevice
        } else if (mutableAudioDevices.size > 0) {
            mutableAudioDevices[0]
        } else {
            null
        }

        // Activate the device if in the active state
        if (state == ACTIVATED) {
            activate()
        }
        audioDeviceChangeListener?.let { listener ->
            selectedDevice?.let { selectedDevice ->
                listener.invoke(
                        mutableAudioDevices,
                        AudioDevice(selectedDevice.type, selectedDevice.name))
            } ?: run {
                listener.invoke(mutableAudioDevices, null)
            }
        }
    }

    private fun userSelectedDevicePresent(audioDevices: List<AudioDevice>): Boolean {
        for (audioDevice in audioDevices) {
            if (audioDevice == userSelectedDevice) {
                return true
            }
        }
        return false
    }

    private fun closeListeners() {
        bluetoothController?.stop()
        wiredHeadsetReceiver.stop()
        audioDeviceChangeListener = null
        state = STOPPED
    }
}