package com.twilio.video.app.ui.room

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RestrictTo
import com.twilio.video.app.sdk.RoomManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import timber.log.Timber

private const val ROOM_NAME_EXTRA = "ROOM_NAME_EXTRA"

@AndroidEntryPoint
class VideoService : Service() {

    @Inject
    lateinit var roomManager: RoomManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        setupForegroundService(intent)
        Timber.d("VideoService created")
        isServiceStarted = true
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("VideoService destroyed")
        stopForeground(true)
        isServiceStarted = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun setupForegroundService(intent: Intent?) {
        intent?.let {
            it.getStringExtra(ROOM_NAME_EXTRA)?.let { roomName ->
                val roomNotification = RoomNotification(this@VideoService)
                startForeground(
                    ONGOING_NOTIFICATION_ID,
                    roomNotification.buildNotification(roomName)
                )
            }
        }
    }

    companion object {
        @RestrictTo(RestrictTo.Scope.TESTS)
        internal var isServiceStarted: Boolean = false

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

        fun stopService(context: Context) {
            Intent(context, VideoService::class.java).let { context.stopService(it) }
        }
    }
}
