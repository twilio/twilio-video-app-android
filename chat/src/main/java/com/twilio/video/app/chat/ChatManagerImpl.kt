package com.twilio.video.app.chat

import android.content.Context
import com.twilio.conversations.CallbackListener
import com.twilio.conversations.Conversation
import com.twilio.conversations.ConversationsClient
import com.twilio.conversations.ConversationsClient.SynchronizationStatus
import com.twilio.conversations.ConversationsClientListener
import com.twilio.conversations.ErrorInfo
import com.twilio.conversations.User
import com.twilio.video.app.chat.ConnectionState.Connected
import com.twilio.video.app.chat.ConnectionState.Connecting
import com.twilio.video.app.chat.ConnectionState.Disconnected
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
    private var client: ConversationsClient? = null
    private var chatName: String? = null
    override val chatState = stateFlow

    private val conversationsClientCallback: CallbackListener<ConversationsClient> = object : CallbackListener<ConversationsClient> {
        override fun onSuccess(conversationsClient: ConversationsClient) {
            Timber.d("Success creating Twilio Conversations Client, now synchronizing...")
            client = conversationsClient
            conversationsClient.addListener(conversationsClientListener)
        }

        override fun onError(errorInfo: ErrorInfo) {
            Timber.e("Error connecting to client: $errorInfo")
            // TODO test = Failing to connect the client should update the connection state to disconnected and send a ClientConnectFailure event
            updateState { it.copy(connectionState = Disconnected) }
        }
    }

    private val conversationCallback: CallbackListener<Conversation> = object : CallbackListener<Conversation> {
        override fun onSuccess(conversation: Conversation) {
            Timber.d("Success Joining Conversation")
            updateState { it.copy(connectionState = Connected) }
        }

        override fun onError(errorInfo: ErrorInfo) {
            Timber.e("Error joining conversation: $errorInfo")
            // TODO test = Failing to join the conversation should update the connection state to disconnected and send a ClientConnect
            updateState { it.copy(connectionState = Disconnected) }
        }
    }

    private val conversationsClientListener: ConversationsClientListener = object : ConversationsClientListener {
        override fun onConversationAdded(conversation: Conversation) {}
        override fun onConversationUpdated(conversation: Conversation, updateReason: Conversation.UpdateReason) {}
        override fun onConversationDeleted(conversation: Conversation) {}
        override fun onConversationSynchronizationChange(conversation: Conversation) {}
        override fun onError(errorInfo: ErrorInfo) {
            Timber.e("Error connecting to client: $errorInfo")
            updateState { it.copy(connectionState = Disconnected) }
        }
        override fun onUserUpdated(user: User, updateReason: User.UpdateReason) {}
        override fun onUserSubscribed(user: User) {}
        override fun onUserUnsubscribed(user: User) {}
        override fun onClientSynchronization(synchronizationStatus: SynchronizationStatus) {
            Timber.d("Client synchronization status: $synchronizationStatus")
            when (synchronizationStatus) {
                SynchronizationStatus.COMPLETED -> joinConversation()
                SynchronizationStatus.FAILED -> updateState { it.copy(connectionState = Disconnected) } // TODO test =
            }
        }

        override fun onNewMessageNotification(s: String, s1: String, l: Long) {}
        override fun onAddedToConversationNotification(s: String) {}
        override fun onRemovedFromConversationNotification(s: String) {}
        override fun onNotificationSubscribed() {}
        override fun onNotificationFailed(errorInfo: ErrorInfo) {}
        override fun onConnectionStateChange(connectionState: ConversationsClient.ConnectionState) {}
        override fun onTokenExpired() {}
        override fun onTokenAboutToExpire() {}
    }

    override fun connect(token: String, chatName: String) {
        this.chatName = chatName
        updateState { it.copy(connectionState = Connecting) }
        ConversationsClient.setLogLevel(ConversationsClient.LogLevel.VERBOSE)
        val props = ConversationsClient
                .Properties
                .newBuilder()
                .setCommandTimeout(30000)
                .createProperties()
        ConversationsClient.create(context, token, props, conversationsClientCallback)
    }

    private fun updateState(action: (oldState: ChatState) -> ChatState) {
        chatScope.launch {
            stateFlow.value = action(stateFlow.value)
            Timber.d("New ChatManager state: ${stateFlow.value}")
        }
    }

    private fun joinConversation() {
        client?.let { client ->
            chatName?.let { chatName ->
                Timber.d("Retrieving conversation with unique name: $chatName")
                client.getConversation(chatName, conversationCallback)
            }
        }
    }
}
