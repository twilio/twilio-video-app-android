package com.twilio.audioswitch;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class enables developers to enumerate available audio devices and select which device audio
 * should be routed to. The AudioDeviceSelector instance must be accessed from the main thread.
 * Additionally, the AudioDeviceChangeListener will always callback on the main thread.
 */
public class AudioDeviceSelector {
    private static final String TAG = "AudioDeviceSelector";

    private final Context context;
    private final AudioManager audioManager;
    private final boolean hasEarpiece;
    private final boolean hasSpeakerphone;
    private final BluetoothController bluetoothController;

    private @Nullable AudioDeviceChangeListener audioDeviceChangeListener;
    private @Nullable AudioDevice selectedDevice;
    private @Nullable AudioDevice userSelectedDevice;
    private @NonNull State state;
    private @NonNull WiredHeadsetReceiver wiredHeadsetReceiver;
    private boolean wiredHeadsetAvailable;
    private ArrayList<AudioDevice> availableAudioDevices = new ArrayList<>();

    // Saved Audio Settings
    private int savedAudioMode;
    private boolean savedIsMicrophoneMuted;
    private boolean savedSpeakerphoneEnabled;

    private enum State {
        STARTED,
        ACTIVE,
        STOPPED
    }

    private AudioDevice EARPIECE_AUDIO_DEVICE =
            new AudioDevice(AudioDevice.Type.EARPIECE, "Earpiece");
    private AudioDevice SPEAKERPHONE_AUDIO_DEVICE =
            new AudioDevice(AudioDevice.Type.SPEAKERPHONE, "Speakerphone");
    private AudioDevice WIRED_HEADSET_AUDIO_DEVICE =
            new AudioDevice(AudioDevice.Type.WIRED_HEADSET, "Wired Headset");
    private @Nullable AudioDevice bluetoothAudioDevice;

    /**
     * Constructs a new AudioDeviceSelector instance.
     *
     * @param context the application context
     */
    public AudioDeviceSelector(@NonNull Context context) {
        ThreadUtils.checkIsOnMainThread();
        this.context = context.getApplicationContext();
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.wiredHeadsetReceiver = new WiredHeadsetReceiver();
        this.bluetoothController =
                new BluetoothController(
                        context,
                        new BluetoothController.Listener() {
                            @Override
                            public void onBluetoothConnected(
                                    @NonNull BluetoothDevice bluetoothDevice) {
                                bluetoothAudioDevice =
                                        new AudioDevice(
                                                AudioDevice.Type.BLUETOOTH,
                                                bluetoothDevice.getName());
                                if (state == State.ACTIVE) {
                                    userSelectedDevice = bluetoothAudioDevice;
                                }
                                enumerateDevices();
                            }

                            @Override
                            public void onBluetoothDisconnected() {
                                bluetoothAudioDevice = null;
                                enumerateDevices();
                            }
                        });
        hasEarpiece = hasEarpiece();
        hasSpeakerphone = hasSpeakerphone();
        state = State.STOPPED;
    }

    /**
     * Starts listening for audio device changes. <b>Note:</b> When audio device listening is no
     * longer needed, {@link AudioDeviceSelector#stop()} should be called in order to prevent a
     * memory leak.
     *
     * @param listener receives audio device change events
     */
    public void start(@NonNull AudioDeviceChangeListener listener) {
        ThreadUtils.checkIsOnMainThread();
        this.audioDeviceChangeListener = listener;
        switch (state) {
            case STOPPED:
                bluetoothController.start();
                context.registerReceiver(
                        wiredHeadsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
                /*
                 * Enumerate devices when the wired headset receiver does not broadcast an action.
                 * The broadcast receiver will not broadcast an action when a wired headset is not
                 * initially plugged in. This task is intentionally run immediately after the wired
                 * headset broadcast receiver task runs.
                 */
                final Handler handler = new Handler();
                handler.post(
                        () -> {
                            if (!wiredHeadsetAvailable) {
                                enumerateDevices();
                            }
                        });
                state = State.STARTED;
                break;
            case STARTED:
            case ACTIVE:
                // no-op
                break;
        }
    }

    /**
     * Stops listening for audio device changes if {@link
     * AudioDeviceSelector#start(AudioDeviceChangeListener)} has already been invoked. {@link
     * AudioDeviceSelector#deactivate()} will also get called if a device has been activated with
     * {@link AudioDeviceSelector#activate()}.
     */
    public void stop() {
        ThreadUtils.checkIsOnMainThread();
        switch (state) {
            case ACTIVE:
                deactivate();
                // Fall through after deactivating the active device
            case STARTED:
                context.unregisterReceiver(wiredHeadsetReceiver);
                bluetoothController.stop();
                state = State.STOPPED;
                break;
            case STOPPED:
                // no-op
        }
    }

    /**
     * Performs audio routing and unmuting on the selected device from {@link
     * AudioDeviceSelector#selectDevice(AudioDevice)}. Audio focus is also acquired for the client
     * application. <b>Note:</b> {@link AudioDeviceSelector#deactivate()} should be invoked to
     * restore the prior audio state.
     */
    public void activate() {
        ThreadUtils.checkIsOnMainThread();
        switch (state) {
            case STARTED:
                savedAudioMode = audioManager.getMode();
                savedIsMicrophoneMuted = audioManager.isMicrophoneMute();
                savedSpeakerphoneEnabled = audioManager.isSpeakerphoneOn();

                // Always set mute to false for WebRTC
                mute(false);

                setAudioFocus();

                if (selectedDevice != null) {
                    activate(selectedDevice);
                }
                state = State.ACTIVE;
                break;
            case ACTIVE:
                // Activate the newly selected device
                if (selectedDevice != null) {
                    activate(selectedDevice);
                }
                break;
            case STOPPED:
                throw new IllegalStateException();
        }
    }

    private void activate(@NonNull AudioDevice audioDevice) {
        switch (audioDevice.type) {
            case BLUETOOTH:
                enableSpeakerphone(false);
                bluetoothController.activate();
                break;
            case EARPIECE:
            case WIRED_HEADSET:
                enableSpeakerphone(false);
                bluetoothController.deactivate();
                break;
            case SPEAKERPHONE:
                enableSpeakerphone(true);
                bluetoothController.deactivate();
                break;
        }
    }

    /**
     * Restores the audio state prior to calling {@link AudioDeviceSelector#activate()} and removes
     * audio focus from the client application.
     */
    public void deactivate() {
        ThreadUtils.checkIsOnMainThread();
        switch (state) {
            case ACTIVE:
                bluetoothController.deactivate();

                // Restore stored audio state
                audioManager.setMode(savedAudioMode);
                mute(savedIsMicrophoneMuted);
                enableSpeakerphone(savedSpeakerphoneEnabled);

                audioManager.abandonAudioFocus(null);
                state = State.STARTED;
                break;
            case STARTED:
            case STOPPED:
                // no-op;
                break;
        }
    }

    /**
     * Selects the desired {@link AudioDevice}. If the provided {@link AudioDevice} is not
     * available, no changes are made. If the provided {@link AudioDevice} is null, an {@link
     * AudioDevice} is chosen based on the following preference: Bluetooth, Wired Headset,
     * Microphone, Speakerphone.
     *
     * @param audioDevice The {@link AudioDevice} to use
     */
    public void selectDevice(@Nullable AudioDevice audioDevice) {
        ThreadUtils.checkIsOnMainThread();
        userSelectedDevice = audioDevice;
        enumerateDevices();
    }

    /**
     * Retrieves the selected {@link AudioDevice} from {@link
     * AudioDeviceSelector#selectDevice(AudioDevice)}
     *
     * @return the selected {@link AudioDevice}
     */
    public @Nullable AudioDevice getSelectedAudioDevice() {
        ThreadUtils.checkIsOnMainThread();
        return selectedDevice != null
                ? new AudioDevice(selectedDevice.type, selectedDevice.name)
                : null;
    }

    /**
     * Retrieves the current list of available {@link AudioDevice}s.
     *
     * @return the current list of {@link AudioDevice}s
     */
    public @NonNull List<AudioDevice> getAudioDevices() {
        ThreadUtils.checkIsOnMainThread();
        return Collections.unmodifiableList(new ArrayList<>(availableAudioDevices));
    }

    private void setAudioFocus() {
        // Request audio focus before making any device switch.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes =
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build();
            AudioFocusRequest focusRequest =
                    new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                            .setAudioAttributes(playbackAttributes)
                            .setAcceptsDelayedFocusGain(true)
                            .setOnAudioFocusChangeListener(i -> {})
                            .build();
            audioManager.requestAudioFocus(focusRequest);
        } else {
            audioManager.requestAudioFocus(
                    focusChange -> {},
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
        /*
         * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
         * required to be in this mode when playout and/or recording starts for
         * best possible VoIP performance. Some devices have difficulties with speaker mode
         * if this is not set.
         */
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    private class WiredHeadsetReceiver extends BroadcastReceiver {
        private static final int STATE_UNPLUGGED = 0;
        private static final int STATE_PLUGGED = 1;

        @Override
        public void onReceive(Context context, Intent intent) {
            ThreadUtils.checkIsOnMainThread();
            int state = intent.getIntExtra("state", STATE_UNPLUGGED);
            if (state == STATE_PLUGGED) {
                wiredHeadsetAvailable = true;
                Log.d(TAG, "Wired Headset available");
                if (AudioDeviceSelector.this.state == State.ACTIVE) {
                    userSelectedDevice = WIRED_HEADSET_AUDIO_DEVICE;
                }
                enumerateDevices();
            } else {
                wiredHeadsetAvailable = false;
                enumerateDevices();
            }
        }
    }

    private void enumerateDevices() {
        ThreadUtils.checkIsOnMainThread();
        availableAudioDevices.clear();
        if (bluetoothAudioDevice != null) {
            availableAudioDevices.add(bluetoothAudioDevice);
        }
        if (wiredHeadsetAvailable) {
            availableAudioDevices.add(WIRED_HEADSET_AUDIO_DEVICE);
        }
        if (hasEarpiece && !wiredHeadsetAvailable) {
            availableAudioDevices.add(EARPIECE_AUDIO_DEVICE);
        }
        if (hasSpeakerphone) {
            availableAudioDevices.add(SPEAKERPHONE_AUDIO_DEVICE);
        }

        // Check whether the user selected device is still present
        if (!userSelectedDevicePresent(availableAudioDevices)) {
            userSelectedDevice = null;
        }

        // Select the audio device
        if (userSelectedDevice != null && userSelectedDevicePresent(availableAudioDevices)) {
            selectedDevice = userSelectedDevice;
        } else if (availableAudioDevices.size() > 0) {
            selectedDevice = availableAudioDevices.get(0);
        } else {
            selectedDevice = null;
        }

        // Activate the device if in the active state
        if (state == State.ACTIVE) {
            activate();
        }

        if (audioDeviceChangeListener != null) {
            if (selectedDevice != null) {
                audioDeviceChangeListener.onAvailableAudioDevices(
                        availableAudioDevices,
                        new AudioDevice(selectedDevice.type, selectedDevice.name));
            } else {
                audioDeviceChangeListener.onAvailableAudioDevices(availableAudioDevices, null);
            }
        }
    }

    private boolean userSelectedDevicePresent(List<AudioDevice> audioDevices) {
        for (AudioDevice audioDevice : audioDevices) {
            if (audioDevice.equals(userSelectedDevice)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasEarpiece() {
        boolean hasEarpiece =
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        if (hasEarpiece) {
            Log.d(TAG, "Earpiece available");
        }
        return hasEarpiece;
    }

    private boolean hasSpeakerphone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && context.getPackageManager()
                        .hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
            AudioDeviceInfo[] devices =
                    audioManager.getDevices(android.media.AudioManager.GET_DEVICES_OUTPUTS);
            for (AudioDeviceInfo device : devices) {
                if (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                    Log.d(TAG, "Speakerphone available");
                    return true;
                }
            }
            return false;
        } else {
            Log.d(TAG, "Speakerphone available");
            return true;
        }
    }

    private void enableSpeakerphone(boolean enable) {
        audioManager.setSpeakerphoneOn(enable);
    }

    private void mute(boolean mute) {
        audioManager.setMicrophoneMute(mute);
    }
}
