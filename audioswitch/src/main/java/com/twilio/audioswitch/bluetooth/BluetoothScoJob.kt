package com.twilio.audioswitch.bluetooth

import android.os.Handler
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.android.SystemClockWrapper
import com.twilio.audioswitch.bluetooth.BluetoothDeviceConnectionListener.ConnectionError
import java.util.concurrent.TimeoutException

internal const val TIMEOUT = 5000L
private const val TAG = "BluetoothScoManager"

internal abstract class BluetoothScoJob(
    private val logger: LogWrapper,
    private val bluetoothScoHandler: Handler,
    private val systemClockWrapper: SystemClockWrapper
) {

    var bluetoothScoRunnable: BluetoothScoRunnable = BluetoothScoRunnable()
    var deviceListener: BluetoothDeviceConnectionListener? = null

    protected abstract val scoAction: () -> Unit
    protected abstract val timeoutError: ConnectionError

    fun executeBluetoothScoJob() {
        bluetoothScoRunnable = BluetoothScoRunnable()
        bluetoothScoHandler.post(bluetoothScoRunnable)
        logger.d(TAG, "Scheduled bluetooth sco job")
    }

    fun cancelBluetoothScoJob() {
        bluetoothScoHandler.removeCallbacks(bluetoothScoRunnable)
        logger.d(TAG, "Canceled bluetooth sco job")
    }

    inner class BluetoothScoRunnable : Runnable {

        private val startTime = systemClockWrapper.elapsedRealtime()
        private var elapsedTime = 0L

        override fun run() {
            if (elapsedTime < TIMEOUT) {
                logger.d(TAG, "Invoking bluetooth sco action")
                scoAction.invoke()
                elapsedTime = systemClockWrapper.elapsedRealtime() - startTime
                bluetoothScoHandler.postDelayed(this, 500)
            } else {
                logger.e(TAG, "Bluetooth sco job timed out", TimeoutException())
                deviceListener?.onBluetoothConnectionError(timeoutError)
                cancelBluetoothScoJob()
            }
        }
    }
}
