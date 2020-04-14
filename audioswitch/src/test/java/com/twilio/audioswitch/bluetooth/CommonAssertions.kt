package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

internal fun assertBluetoothControllerStart(
    context: Context,
    preConnectedDeviceListener: PreConnectedDeviceListener,
    bluetoothDeviceReceiver: BluetoothHeadsetReceiver,
    deviceListener: BluetoothDeviceConnectionListener,
    bluetoothAdapter: BluetoothAdapter
) {

    assertThat(preConnectedDeviceListener.deviceListener, equalTo(deviceListener))
    assertThat(bluetoothDeviceReceiver.deviceListener, equalTo(deviceListener))
    verify(bluetoothAdapter).getProfileProxy(
            context,
            preConnectedDeviceListener,
            BluetoothProfile.HEADSET)
    verify(context, times(3)).registerReceiver(
            eq(bluetoothDeviceReceiver), isA())
}