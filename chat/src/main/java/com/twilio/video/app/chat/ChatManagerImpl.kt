package com.twilio.video.app.chat

import android.content.Context
import com.twilio.conversations.ConversationsClient
import com.twilio.video.app.chat.ConnectionState.Connecting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ChatManagerImpl(
    private val context: Context,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ChatManager {

    private val stateFlow = MutableStateFlow(ChatState())
    private val chatScope = CoroutineScope(coroutineDispatcher)
    override val chatState = stateFlow

    override fun connect(token: String, chatName: String) {
        updateState { it.copy(connectionState = Connecting) }
        ConversationsClient.setLogLevel(ConversationsClient.LogLevel.VERBOSE)
        val props = ConversationsClient
                .Properties
                .newBuilder()
                .setCommandTimeout(30000)
                .createProperties()
//        ConversationsClient.create(context, token, props, mConversationsClientCallback)
    }

    private fun updateState(action: (oldState: ChatState) -> ChatState) {
        chatScope.launch {
            stateFlow.value = action(stateFlow.value)
            Timber.d("New ChatManager state: ${stateFlow.value}")
        }
    }
}
