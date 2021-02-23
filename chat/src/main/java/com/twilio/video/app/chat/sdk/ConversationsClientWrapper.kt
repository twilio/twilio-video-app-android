package com.twilio.video.app.chat.sdk

import android.content.Context
import com.twilio.conversations.CallbackListener
import com.twilio.conversations.ConversationsClient

class ConversationsClientWrapper {
    fun create(
        context: Context,
        token: String,
        properties: ConversationsClient.Properties,
        listener: CallbackListener<ConversationsClient>
    ) = ConversationsClient.create(context, token, properties, listener)
}
