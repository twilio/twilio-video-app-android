package com.twilio.video.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Process
import androidx.core.app.NotificationCompat
import com.twilio.video.app.ui.room.RoomActivity
import timber.log.Timber

const val VIDEO_SERVICE_CHANNEL = "VIDEO_SERVICE_CHANNEL"
const val ONGOING_NOTIFICATION_ID = 1

class VideoService : Service() {

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    companion object {
        fun createIntent(context: Context) = Intent(context, VideoService::class.java)
    }

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
        HandlerThread("VideoService StartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("VideoService created")
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() = Timber.d("VideoService destroyed")
}