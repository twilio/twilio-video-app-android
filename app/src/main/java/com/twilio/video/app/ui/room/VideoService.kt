package com.twilio.video.app.ui.room

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.twilio.video.app.ui.room.RoomState.Disconnected
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

private const val ROOM_NAME_EXTRA = "ROOM_NAME_EXTRA"

class VideoService : LifecycleService() {

    companion object {
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

    @Inject lateinit var roomManager: RoomManager

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        roomManager.viewEvents.observe(this, Observer { bindRoomEvents(it) })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        setupForegroundService(intent)
        Timber.d("VideoService created")
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("VideoService destroyed")
    }

    private fun setupForegroundService(intent: Intent?) {
        intent?.let { it.getStringExtra(ROOM_NAME_EXTRA)?.let { roomName ->
            val roomNotification = RoomNotification(this@VideoService)
            startForeground(
                    ONGOING_NOTIFICATION_ID,
                    roomNotification.buildNotification(roomName))
        } }
    }

    private fun bindRoomEvents(nullableRoomState: RoomState?) {
        nullableRoomState?.let { roomEvent ->
            when (roomEvent) {
                is Disconnected -> stopSelf()
                else -> {}
            }
        }
    }
}