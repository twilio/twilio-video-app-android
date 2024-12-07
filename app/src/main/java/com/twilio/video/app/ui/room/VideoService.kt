package com.twilio.video.app.ui.room

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.annotation.RestrictTo
import com.twilio.video.app.sdk.RoomManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

private const val ROOM_NAME_EXTRA = "ROOM_NAME_EXTRA"

@AndroidEntryPoint
class VideoService : Service() {

    @Inject
    lateinit var roomManager: RoomManager

    @SuppressLint("RestrictedApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        setupForegroundService(intent)
        Timber.d("VideoService created")
        isServiceStarted = true
        videoService = this@VideoService
        return START_NOT_STICKY
    }

    @SuppressLint("RestrictedApi")
    override fun onDestroy() {
        super.onDestroy()
        Timber.d("VideoService destroyed")
        stopForeground(true)
        isServiceStarted = false
        videoService = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun enableScreenShare(roomName: String, enable: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val roomNotification = RoomNotification(this@VideoService)
            startForeground(
                ONGOING_NOTIFICATION_ID,
                roomNotification.buildNotification(roomName),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or
                        if (enable) {
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                        } else {
                            0
                        }
            )
        }
    }

    private fun setupForegroundService(intent: Intent?) {
        intent?.let {
            it.getStringExtra(ROOM_NAME_EXTRA)?.let { roomName ->
                val roomNotification = RoomNotification(this@VideoService)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    startForeground(
                        ONGOING_NOTIFICATION_ID,
                        roomNotification.buildNotification(roomName),
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or
                                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                    )
                } else {
                    startForeground(
                        ONGOING_NOTIFICATION_ID,
                        roomNotification.buildNotification(roomName)
                    )
                }
            }
        }
    }

    companion object {
        @RestrictTo(RestrictTo.Scope.TESTS)
        internal var isServiceStarted: Boolean = false
        internal var videoService: VideoService? = null

        fun startService(context: Context, roomName: String) {
            Intent(context, VideoService::class.java).let { intent ->
                intent.putExtra(ROOM_NAME_EXTRA, roomName)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        fun enableScreenShare(roomName: String) {
            videoService?.enableScreenShare(roomName, true)
        }

        fun disableScreenShare(roomName: String) {
            videoService?.enableScreenShare(roomName, false)
        }

        fun stopService(context: Context) {
            Intent(context, VideoService::class.java).let { context.stopService(it) }
        }
    }
}
