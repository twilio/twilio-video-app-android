package com.twilio.video.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.os.Process
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.twilio.video.Room.State.DISCONNECTED
import com.twilio.video.app.ui.room.RoomActivity
import com.twilio.video.app.ui.room.RoomEvent
import com.twilio.video.app.ui.room.RoomEvent.RoomState
import com.twilio.video.app.ui.room.RoomManager
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

const val VIDEO_SERVICE_CHANNEL = "VIDEO_SERVICE_CHANNEL"
const val ONGOING_NOTIFICATION_ID = 1

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
            this@VideoService.let { videoService ->
                val pendingIntent: PendingIntent =
                        Intent(videoService, RoomActivity::class.java).let { notificationIntent ->
                            PendingIntent.getActivity(videoService, 0, notificationIntent, 0)
                        }

                createDownloadNotificationChannel(VIDEO_SERVICE_CHANNEL,
                        videoService.getString(R.string.video_chat_notification_channel_title),
                        videoService)

                val notification: Notification = NotificationCompat.Builder(videoService, VIDEO_SERVICE_CHANNEL)
                        .setContentTitle(videoService.getString(R.string.app_name))
                        .setContentText(videoService.getString(R.string.video_chat_notification_message))
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_videocam_green_24px)
                        .setTicker(videoService.getString(R.string.video_chat_notification_message))
                        .build()

                startForeground(ONGOING_NOTIFICATION_ID, notification)
            }
        }

        private fun createDownloadNotificationChannel(channelId: String, channelName: String, context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW).apply {
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)
            }
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