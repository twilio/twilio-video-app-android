package com.twilio.audioswitch

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import androidx.annotation.VisibleForTesting
import com.twilio.audioswitch.AudioDevice.BluetoothHeadset
import com.twilio.audioswitch.AudioDevice.Earpiece
import com.twilio.audioswitch.AudioDevice.Speakerphone
import com.twilio.audioswitch.AudioDevice.WiredHeadset
import com.twilio.audioswitch.AudioSwitch.State.ACTIVATED
import com.twilio.audioswitch.AudioSwitch.State.STARTED
import com.twilio.audioswitch.AudioSwitch.State.STOPPED
import com.twilio.audioswitch.android.Logger
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetConnectionListener
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager
import com.twilio.audioswitch.wired.WiredDeviceConnectionListener
import com.twilio.audioswitch.wired.WiredHeadsetReceiver

private const val TAG = "AudioSwitch"

/**
 * This class enables developers to enumerate available audio devices and select which device audio
 * should be routed to. It is strongly recommended that instances of this class are created and
 * accessed from a single application thread. Accessing an instance from multiple threads may cause
 * synchronization problems.
 */
class AudioSwitch {

    private var logger: Logger = Logger()
    private val audioDeviceManager: AudioDeviceManager
    private val wiredHeadsetReceiver: WiredHeadsetReceiver
    internal var audioDeviceChangeListener: AudioDeviceChangeListener? = null
    private var selectedDevice: AudioDevice? = null
    private var userSelectedDevice: AudioDevice? = null
    private var wiredHeadsetAvailable = false
    private val mutableAudioDevices = ArrayList<AudioDevice>()
    private var bluetoothHeadsetManager: BluetoothHeadsetManager? = null
    private val preferredDeviceList: List<Class<out AudioDevice>>

    internal var state: State = STOPPED
    internal enum class State {
        STARTED, ACTIVATED, STOPPED
    }
    internal val bluetoothDeviceConnectionListener = object : BluetoothHeadsetConnectionListener {
        override fun onBluetoothHeadsetStateChanged(headsetName: String?) {
            enumerateDevices(headsetName)
        }

        override fun onBluetoothHeadsetActivationError() {
            if (userSelectedDevice is BluetoothHeadset) userSelectedDevice = null
            enumerateDevices()
        }
    }

    internal val wiredDeviceConnectionListener = object : WiredDeviceConnectionListener {
        override fun onDeviceConnected() {
            wiredHeadsetAvailable = true
            logger.d(TAG, "Wired Headset available")
            if (this@AudioSwitch.state == ACTIVATED) {
                userSelectedDevice = WiredHeadset()
            }
            enumerateDevices()
        }

        override fun onDeviceDisconnected() {
            wiredHeadsetAvailable = false
            enumerateDevices()
        }
    }

    /**
     * A property to configure AudioSwitch logging behavior. AudioSwitch logging is disabled by
     * default.
     */
    var loggingEnabled: Boolean
        /**
         * Returns `true` if logging is enabled. Returns `false` by default.
         */
        get() = logger.loggingEnabled

        /**
         * Toggle whether logging is enabled.
         */
        set(value) {
            logger.loggingEnabled = value
        }

    /**
     * Retrieves the selected [AudioDevice] from [AudioSwitch.selectDevice].
     *
     * @return the selected [AudioDevice]
     */
    val selectedAudioDevice: AudioDevice? get() = selectedDevice

    /**
     * Retrieves the current list of available [AudioDevice]s.
     *
     * @return the current list of [AudioDevice]s
     */
    val availableAudioDevices: List<AudioDevice> = mutableAudioDevices

    /**
     * Constructs a new AudioSwitch instance.
     *
     * @param context An Android Context.
     * @param loggingEnabled Toggle whether logging is enabled. This argument is false by default.
     * @param audioFocusChangeListener A listener that is invoked when the system audio focus is
     * updated. Note that updates are only sent to the listener after [activate] has been called.
     */
    @JvmOverloads
    constructor(
        context: Context,
        loggingEnabled: Boolean = false,
        audioFocusChangeListener: OnAudioFocusChangeListener = OnAudioFocusChangeListener {},
        preferredDeviceList: List<Class<out AudioDevice>> = defaultPreferredDeviceList,
    ) : this(context.applicationContext, Logger(loggingEnabled), audioFocusChangeListener,
            preferredDeviceList)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal constructor(
        context: Context,
        logger: Logger,
        audioFocusChangeListener: OnAudioFocusChangeListener,
        preferredDeviceList: List<Class<out AudioDevice>>,
        audioDeviceManager: AudioDeviceManager = AudioDeviceManager(context,
            logger,
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager,
            audioFocusChangeListener = audioFocusChangeListener),
        wiredHeadsetReceiver: WiredHeadsetReceiver = WiredHeadsetReceiver(context, logger),
        headsetManager: BluetoothHeadsetManager? = BluetoothHeadsetManager.newInstance(context,
            logger,
            BluetoothAdapter.getDefaultAdapter(),
            audioDeviceManager)
    ) {
        this.logger = logger
        this.audioDeviceManager = audioDeviceManager
        this.wiredHeadsetReceiver = wiredHeadsetReceiver
        this.bluetoothHeadsetManager = headsetManager
        this.preferredDeviceList = getPreferredDeviceList(preferredDeviceList)
        logger.d(TAG, "AudioSwitch($VERSION)")
    }

    private fun getPreferredDeviceList(preferredDeviceList: List<Class<out AudioDevice>>):
            List<Class<out AudioDevice>> {
        require(preferredDeviceList.isNotEmpty() && hasNoDuplicates(preferredDeviceList))

        return if(preferredDeviceList == defaultPreferredDeviceList) defaultPreferredDeviceList
        else {
            val result = defaultPreferredDeviceList.toMutableList()
            result.removeAll(preferredDeviceList)
            preferredDeviceList.forEachIndexed { index, device ->
                result.add(index, device)
            }
            result
        }
    }

    /**
     * Starts listening for audio device changes. **Note:** When audio device listening is no
     * longer needed, [AudioSwitch.stop] should be called in order to prevent a
     * memory leak.
     *
     * @param listener receives audio device change events
     */
    fun start(listener: AudioDeviceChangeListener) {
        audioDeviceChangeListener = listener
        when (state) {
            STOPPED -> {
                bluetoothHeadsetManager?.start(bluetoothDeviceConnectionListener)
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
     * Stops listening for audio device changes if [AudioSwitch.start] has already been
     * invoked. [AudioSwitch.deactivate] will also get called if a device has been activated
     * with [AudioSwitch.activate].
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
     * Performs audio routing and unmuting on the selected device from
     * [AudioSwitch.selectDevice]. Audio focus is also acquired for the client application.
     * **Note:** [AudioSwitch.deactivate] should be invoked to restore the prior audio
     * state.
     */
    fun activate() {
        when (state) {
            STARTED -> {
                audioDeviceManager.cacheAudioState()

                // Always set mute to false for WebRTC
                audioDeviceManager.mute(false)
                audioDeviceManager.setAudioFocus()
                selectedDevice?.let { activate(it) }
                state = ACTIVATED
            }
            ACTIVATED -> selectedDevice?.let { activate(it) }
            STOPPED -> throw IllegalStateException()
        }
    }

    /**
     * Restores the audio state prior to calling [AudioSwitch.activate] and removes
     * audio focus from the client application.
     */
    fun deactivate() {
        when (state) {
            ACTIVATED -> {
                bluetoothHeadsetManager?.deactivate()

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
     * available, no changes are made. If the provided [AudioDevice] is null, an [AudioDevice] is
     * chosen based on the following preference: Bluetooth, Wired Headset, Earpiece, Speakerphone.
     *
     * @param audioDevice The [AudioDevice] to use
     */
    fun selectDevice(audioDevice: AudioDevice?) {
        if (selectedDevice != audioDevice) {
            logger.d(TAG, "Selected AudioDevice = $audioDevice")
            userSelectedDevice = audioDevice
            enumerateDevices()
        }
    }

    private fun hasNoDuplicates(list: List<Class<out AudioDevice>>) =
        list.groupingBy { it }.eachCount().filter { it.value > 1 }.isEmpty()


    private fun activate(audioDevice: AudioDevice) {
        when (audioDevice) {
            is BluetoothHeadset -> {
                audioDeviceManager.enableSpeakerphone(false)
                bluetoothHeadsetManager?.activate()
            }
            is Earpiece, is WiredHeadset -> {
                audioDeviceManager.enableSpeakerphone(false)
                bluetoothHeadsetManager?.deactivate()
            }
            is Speakerphone -> {
                audioDeviceManager.enableSpeakerphone(true)
                bluetoothHeadsetManager?.deactivate()
            }
        }
    }

    private fun enumerateDevices(bluetoothHeadsetName: String? = null) {
        addAvailableAudioDevices(bluetoothHeadsetName)

        if (!userSelectedDevicePresent(mutableAudioDevices)) {
            userSelectedDevice = null
        }

        // Select the audio device
        logger.d(TAG, "Current user selected AudioDevice = $userSelectedDevice")
        selectedDevice = if (userSelectedDevice != null) {
            userSelectedDevice
        } else if (mutableAudioDevices.size > 0) {
            val firstAudioDevice = mutableAudioDevices[0]
            /*
             * If there was an error starting bluetooth sco, then the selected AudioDevice should
             * be the next valid device in the list.
             */
            if (firstAudioDevice is BluetoothHeadset &&
                    bluetoothHeadsetManager?.hasActivationError() == true) {
                mutableAudioDevices[1]
            } else {
                firstAudioDevice
            }
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
                        selectedDevice)
            } ?: run {
                listener.invoke(mutableAudioDevices, null)
            }
        }
    }

    private fun addAvailableAudioDevices(bluetoothHeadsetName: String?) {
        mutableAudioDevices.clear()
        preferredDeviceList.forEach { audioDevice ->
            when (audioDevice) {
                BluetoothHeadset::class.java -> {
                /*
                 * Since the there is a delay between receiving the ACTION_ACL_CONNECTED event and receiving
                 * the name of the connected device from querying the BluetoothHeadset proxy class, the
                 * headset name received from the ACTION_ACL_CONNECTED intent needs to be passed into this
                 * function.
                 */
                bluetoothHeadsetManager?.getHeadset(bluetoothHeadsetName)?.let {
                        mutableAudioDevices.add(it)
                    }
                }
                WiredHeadset::class.java -> {
                    if (wiredHeadsetAvailable) {
                        mutableAudioDevices.add(WiredHeadset())
                    }
                }
                Earpiece::class.java -> {
                    if (audioDeviceManager.hasEarpiece() && !wiredHeadsetAvailable) {
                        mutableAudioDevices.add(Earpiece())
                    }
                }
                Speakerphone::class.java -> {
                    if (audioDeviceManager.hasSpeakerphone()) {
                        mutableAudioDevices.add(Speakerphone())
                    }
                }
            }
        }

        logger.d(TAG, "Available AudioDevice list updated: $availableAudioDevices")
    }

    private fun userSelectedDevicePresent(audioDevices: List<AudioDevice>) =
            userSelectedDevice?.let { selectedDevice ->
                if(selectedDevice is BluetoothHeadset) {
                    // Match any bluetooth headset as a new one may have been connected
                    audioDevices.find { it is BluetoothHeadset }?.let { newHeadset ->
                        userSelectedDevice = newHeadset
                        true
                    } ?: false
                } else {
                    audioDevices.contains(selectedDevice)
                }
            } ?: false

    private fun closeListeners() {
        bluetoothHeadsetManager?.stop()
        wiredHeadsetReceiver.stop()
        audioDeviceChangeListener = null
        state = STOPPED
    }

    companion object {
        /**
         * The version of the AudioSwitch library.
         */
        const val VERSION = BuildConfig.VERSION_NAME

        private val defaultPreferredDeviceList by lazy {
            listOf(BluetoothHeadset::class.java,
                    WiredHeadset::class.java, Earpiece::class.java, Speakerphone::class.java)
        }
    }
}
