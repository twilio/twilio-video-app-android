package com.twilio.video.app.sdk

import android.content.Context
import com.twilio.video.Room
import com.twilio.video.Video
import com.twilio.video.app.data.api.TokenService

class VideoClient(
    private val context: Context,
    private val connectOptionsFactory: ConnectOptionsFactory,
    private val tokenService: TokenService
) {

    suspend fun connect(
        identity: String,
        roomName: String,
        roomListener: Room.Listener
    ): String {
        val token = tokenService.getToken(identity, roomName)
        Video.connect(
                context,
                connectOptionsFactory.newInstance(token, roomName),
                roomListener)
        return token
    }
}
