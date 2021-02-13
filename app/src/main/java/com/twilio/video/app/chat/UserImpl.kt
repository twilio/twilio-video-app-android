package com.twilio.video.app.chat

import com.stfalcon.chatkit.commons.models.IUser
import java.util.*

class UserImpl(
        private val name: String,
        private val avatar: String,
        private val id: String = UUID.randomUUID().toString(),
): IUser {

    override fun getId() = id

    override fun getName() = name

    override fun getAvatar() = avatar
}