package com.twilio.video.app.ui.room

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.os.Process
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.twilio.video.Room.State.DISCONNECTED
import com.twilio.video.app.ui.room.RoomEvent.RoomState
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class VideoService : LifecycleService() {

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    companion object {
        fun startService(context: Context) {
            Intent(context, VideoService::class.java).let { intent ->
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

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            val roomNotification = RoomNotification(this@VideoService)
            startForeground(ONGOING_NOTIFICATION_ID, roomNotification.buildNotification())
        }
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        roomManager.viewEvents.observe(this, Observer { bindRoomEvents(it) })
        HandlerThread("VideoService StartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Timber.d("VideoService created")
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("VideoService destroyed")
    }

    private fun bindRoomEvents(nullableRoomEvent: RoomEvent?) {
        nullableRoomEvent?.let { roomEvent ->
            if (roomEvent is RoomState && roomEvent.room.state == DISCONNECTED) {
                stopSelf()
            }
        }
    }
}