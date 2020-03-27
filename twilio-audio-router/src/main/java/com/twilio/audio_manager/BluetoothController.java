package com.twilio.audio_manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class BluetoothController {
    private static final String TAG = "BluetoothController";
    private final @NonNull AudioManager audioManager;
    private final @NonNull Context context;
    private final @NonNull Listener listener;
    private final @Nullable BluetoothAdapter bluetoothAdapter;
    private @Nullable BluetoothDevice bluetoothDevice;

    public interface Listener {
        void onBluetoothConnected(@NonNull BluetoothDevice bluetoothDevice);

        void onBluetoothDisconnected();
    }

    public BluetoothController(@NonNull Context context, @NonNull Listener listener) {
        this.context = context;
        this.listener = listener;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void start() {
        if (bluetoothAdapter != null) {
            // Use the profile proxy to detect a bluetooth device that is already connected
            bluetoothAdapter.getProfileProxy(
                    context,
                    new BluetoothProfile.ServiceListener() {
                        @Override
                        public void onServiceConnected(int profile, BluetoothProfile proxy) {
                            List<BluetoothDevice> bluetoothDeviceList = proxy.getConnectedDevices();
                            for (BluetoothDevice device : bluetoothDeviceList) {
                                Log.d(TAG, "Bluetooth " + device.getName() + " connected");
                                bluetoothDevice = device;
                                listener.onBluetoothConnected(device);
                            }
                        }

                        @Override
                        public void onServiceDisconnected(int profile) {
                            Log.d(TAG, "Bluetooth disconnected");
                            bluetoothDevice = null;
                            listener.onBluetoothDisconnected();
                        }
                    },
                    BluetoothProfile.HEADSET);

            // Register for bluetooth device connection and audio state changes
            context.registerReceiver(
                    broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
            context.registerReceiver(
                    broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
            context.registerReceiver(
                    broadcastReceiver,
                    new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
        }
    }

    public void stop() {
        if (bluetoothAdapter != null) {
            context.unregisterReceiver(broadcastReceiver);
        }
    }

    public void activate() {
        audioManager.startBluetoothSco();
    }

    public void deactivate() {
        audioManager.stopBluetoothSco();
    }

    private BroadcastReceiver broadcastReceiver =
            new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action != null) {
                        switch (action) {
                            case BluetoothDevice.ACTION_ACL_CONNECTED:
                                BluetoothDevice connectedBluetoothDevice =
                                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                                if (isHeadsetDevice(connectedBluetoothDevice)) {
                                    Log.d(
                                            TAG,
                                            "Bluetooth "
                                                    + connectedBluetoothDevice.getName()
                                                    + " connected");
                                    bluetoothDevice = connectedBluetoothDevice;
                                    listener.onBluetoothConnected(bluetoothDevice);
                                }
                                break;
                            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                                BluetoothDevice disconnectedBluetoothDevice =
                                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                                if (disconnectedBluetoothDevice.equals(bluetoothDevice)) {
                                    bluetoothDevice = null;
                                }
                                Log.d(TAG, "Bluetooth disconnected");
                                listener.onBluetoothDisconnected();
                                break;
                            case AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED:
                                int state =
                                        intent.getIntExtra(
                                                AudioManager.EXTRA_SCO_AUDIO_STATE,
                                                AudioManager.SCO_AUDIO_STATE_ERROR);

                                if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                                    Log.d(TAG, "Bluetooth Sco Audio connected");
                                } else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
                                    Log.d(TAG, "Bluetooth Sco Audio disconnected");
                                }
                                break;
                        }
                    }
                }
            };

    private boolean isHeadsetDevice(BluetoothDevice bluetoothDevice) {
        BluetoothClass bluetoothClass = bluetoothDevice.getBluetoothClass();
        if (bluetoothClass != null) {
            int deviceClass = bluetoothClass.getDeviceClass();
            return deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE
                    || deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET
                    || deviceClass == BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO;
        }
        return false;
    }
}
