package com.twilio.audioswitch

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import com.twilio.audioswitch.AudioDeviceSelector.State.STOPPED
import com.twilio.audioswitch.bluetooth.BluetoothController
import com.twilio.audioswitch.bluetooth.BluetoothDeviceConnectionListener
import java.util.Collections
import kotlin.collections.ArrayList

private const val TAG = "AudioDeviceSelector"

/**
 * This class enables developers to enumerate available audio devices and select which device audio
 * should be routed to.
 */
class AudioDeviceSelector internal constructor(
    private val context: Context,
    private val logger: LogWrapper,
    private val audioManager: AudioManager,
    private val phoneAudioDeviceManager: PhoneAudioDeviceManager,
    private val wiredHeadsetReceiver: WiredHeadsetReceiver
) {

    private var audioDeviceChangeListener: AudioDeviceChangeListener? = null
    private var selectedDevice: AudioDevice? = null
    private var userSelectedDevice: AudioDevice? = null
    private var state: State = STOPPED
    private var wiredHeadsetAvailable = false
    private val availableAudioDevices = ArrayList<AudioDevice>()
    // Saved Audio Settings
    private var savedAudioMode = 0
    private var savedIsMicrophoneMuted = false

    private var savedSpeakerphoneEnabled = false
    private enum class State {
        STARTED, ACTIVATED, STOPPED
    }
    private val EARPIECE_AUDIO_DEVICE = AudioDevice(AudioDevice.Type.EARPIECE, "Earpiece")
    private val SPEAKERPHONE_AUDIO_DEVICE = AudioDevice(AudioDevice.Type.SPEAKERPHONE, "Speakerphone")
    private val WIRED_HEADSET_AUDIO_DEVICE = AudioDevice(AudioDevice.Type.WIRED_HEADSET, "Wired Headset")
    private var bluetoothAudioDevice: AudioDevice? = null
    private val bluetoothController: BluetoothController? = BluetoothController.newInstance(
            context,
            logger,
        object : BluetoothDeviceConnectionListener {
            override fun onBluetoothConnected(
                bluetoothDevice: BluetoothDevice
            ) {
                bluetoothAudioDevice = AudioDevice(
                        AudioDevice.Type.BLUETOOTH,
                        bluetoothDevice.name)
                if (state == State.ACTIVATED) {
                    userSelectedDevice = bluetoothAudioDevice
                }
                enumerateDevices()
            }

            override fun onBluetoothDisconnected() {
                bluetoothAudioDevice = null
                enumerateDevices()
            }
        }
    )
    private val wiredDeviceConnectionListener = object : WiredDeviceConnectionListener {
        override fun onDeviceConnected() {
            wiredHeadsetAvailable = true
            logger.d(TAG, "Wired Headset available")
            if (this@AudioDeviceSelector.state == State.ACTIVATED) {
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
            return AudioDeviceSelector(
                    context,
                    logger,
                    context.getSystemService(Context.AUDIO_SERVICE) as AudioManager,
                    PhoneAudioDeviceManager(context, logger, audioManager),
                    WiredHeadsetReceiver(context, logger)
            )
        }
    }

    init {
        wiredHeadsetReceiver.wiredDeviceConnectionListener = wiredDeviceConnectionListener
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
                bluetoothController?.start()
                wiredHeadsetReceiver.start()
                /*
                 * Enumerate devices when the wired headset receiver does not broadcast an action.
                 * The broadcast receiver will not broadcast an action when a wired headset is not
                 * initially plugged in. This task is intentionally run immediately after the wired
                 * headset broadcast receiver task runs.
                 */
                val handler = Handler()
                handler.post {
                    if (!wiredHeadsetAvailable) {
                        enumerateDevices()
                    }
                }
                state = State.STARTED
            }
            State.STARTED, State.ACTIVATED -> {
            }
        }
    }

    /**
     * Stops listening for audio device changes if [ ][AudioDeviceSelector.start] has already been invoked. [ ][AudioDeviceSelector.deactivate] will also get called if a device has been activated with
     * [AudioDeviceSelector.activate].
     */
    fun stop() {
        when (state) {
            State.ACTIVATED -> {
                deactivate()
                wiredHeadsetReceiver.stop()
                bluetoothController?.stop()
                state = STOPPED
            }
            State.STARTED -> {
                wiredHeadsetReceiver.stop()
                bluetoothController?.stop()
                state = STOPPED
            }
            STOPPED -> {
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
            State.STARTED -> {
                savedAudioMode = audioManager.mode
                savedIsMicrophoneMuted = audioManager.isMicrophoneMute
                savedSpeakerphoneEnabled = audioManager.isSpeakerphoneOn

                // Always set mute to false for WebRTC
                mute(false)
                setAudioFocus()
                if (selectedDevice != null) {
                    activate(selectedDevice!!)
                }
                state = State.ACTIVATED
            }
            State.ACTIVATED -> // Activate the newly selected device
                if (selectedDevice != null) {
                    activate(selectedDevice!!)
                }
            STOPPED -> throw IllegalStateException()
        }
    }

    private fun activate(audioDevice: AudioDevice) {
        when (audioDevice.type) {
            AudioDevice.Type.BLUETOOTH -> {
                enableSpeakerphone(false)
                bluetoothController?.activate()
            }
            AudioDevice.Type.EARPIECE, AudioDevice.Type.WIRED_HEADSET -> {
                enableSpeakerphone(false)
                bluetoothController?.deactivate()
            }
            AudioDevice.Type.SPEAKERPHONE -> {
                enableSpeakerphone(true)
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
            State.ACTIVATED -> {
                bluetoothController?.deactivate()

                // Restore stored audio state
                audioManager.mode = savedAudioMode
                mute(savedIsMicrophoneMuted)
                enableSpeakerphone(savedSpeakerphoneEnabled)
                audioManager.abandonAudioFocus(null)
                state = State.STARTED
            }
            State.STARTED, STOPPED -> {
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
    val audioDevices: List<AudioDevice>
        get() {
            return Collections.unmodifiableList(ArrayList(availableAudioDevices))
        }

    private fun setAudioFocus() {
        // Request audio focus before making any device switch.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener { i: Int -> }
                    .build()
            audioManager.requestAudioFocus(focusRequest)
        } else {
            audioManager.requestAudioFocus(
                    { focusChange: Int -> },
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        }
        /*
         * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
         * required to be in this mode when playout and/or recording starts for
         * best possible VoIP performance. Some devices have difficulties with speaker mode
         * if this is not set.
         */
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
    }

    private fun enumerateDevices() {
        availableAudioDevices.clear()
        bluetoothAudioDevice?.let { availableAudioDevices.add(it) }
        if (wiredHeadsetAvailable) {
            availableAudioDevices.add(WIRED_HEADSET_AUDIO_DEVICE)
        }
        if (phoneAudioDeviceManager.hasEarpiece() && !wiredHeadsetAvailable) {
            availableAudioDevices.add(EARPIECE_AUDIO_DEVICE)
        }
        if (phoneAudioDeviceManager.hasSpeakerphone()) {
            availableAudioDevices.add(SPEAKERPHONE_AUDIO_DEVICE)
        }

        // Check whether the user selected device is still present
        if (!userSelectedDevicePresent(availableAudioDevices)) {
            userSelectedDevice = null
        }

        // Select the audio device
        selectedDevice = if (userSelectedDevice != null && userSelectedDevicePresent(availableAudioDevices)) {
            userSelectedDevice
        } else if (availableAudioDevices.size > 0) {
            availableAudioDevices[0]
        } else {
            null
        }

        // Activate the device if in the active state
        if (state == State.ACTIVATED) {
            activate()
        }
        if (audioDeviceChangeListener != null) {
            if (selectedDevice != null) {
                audioDeviceChangeListener!!.invoke(
                        availableAudioDevices,
                        AudioDevice(selectedDevice!!.type, selectedDevice!!.name))
            } else {
                audioDeviceChangeListener!!.invoke(availableAudioDevices, null)
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

    private fun enableSpeakerphone(enable: Boolean) {
        audioManager.isSpeakerphoneOn = enable
    }

    private fun mute(mute: Boolean) {
        audioManager.isMicrophoneMute = mute
    }
}