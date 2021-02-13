package com.twilio.video.app.chat

import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.IUser
import java.util.*

class MessageImpl(
        private val text: String,
        private val user: IUser,
        private val date: Date = Calendar.getInstance().time,
        private val id: String = UUID.randomUUID().toString(),
): IMessage {

    override fun getId() = id

    override fun getText() = text

    override fun getUser() = user

    override fun getCreatedAt() = date
}