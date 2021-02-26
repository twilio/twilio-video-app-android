package com.twilio.video.app.chat

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import com.twilio.conversations.CallbackListener
import com.twilio.conversations.Conversation
import com.twilio.conversations.ConversationsClient
import com.twilio.conversations.ConversationsClient.SynchronizationStatus
import com.twilio.conversations.ConversationsClientListener
import com.twilio.conversations.ErrorInfo
import com.twilio.conversations.Message
import com.twilio.conversations.User
import com.twilio.video.app.chat.ChatEvent.ClientConnectFailure
import com.twilio.video.app.chat.ChatEvent.ConversationJoinFailure
import com.twilio.video.app.chat.ChatEvent.GetMessagesFailure
import com.twilio.video.app.chat.ConnectionState.Connected
import com.twilio.video.app.chat.ConnectionState.Connecting
import com.twilio.video.app.chat.ConnectionState.Disconnected
import com.twilio.video.app.chat.sdk.ConversationsClientWrapper
import java.lang.IllegalStateException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@VisibleForTesting(otherwise = PRIVATE)
internal const val MESSAGE_READ_COUNT = 100

class ChatManagerImpl(
    private val context: Context,
    private val conversationsClientWrapper: ConversationsClientWrapper = ConversationsClientWrapper(),
    private val chatScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : ChatManager {

    private val chatStateFlow = MutableStateFlow(ChatState())
    private val chatEventFlow = MutableSharedFlow<ChatEvent>()
    @VisibleForTesting(otherwise = PRIVATE)
    internal var client: ConversationsClient? = null
    private var conversation: Conversation? = null
    private var chatName: String? = null
    override val chatState = chatStateFlow
    override val chatEvents = chatEventFlow

    override fun connect(token: String, chatName: String) {
        this.chatName = chatName
        updateState { it.copy(connectionState = Connecting) }
        ConversationsClient.setLogLevel(ConversationsClient.LogLevel.VERBOSE)
        val props = ConversationsClient
                .Properties
                .newBuilder()
                .setCommandTimeout(30000)
                .createProperties()
        conversationsClientWrapper.create(context, token, props, conversationsClientCallback)
    }

    override fun disconnect() {
        if (chatState.value.connectionState != Disconnected) {
            client?.shutdown()
            client = null
            updateState { it.copy(connectionState = Disconnected, messages = emptyList()) }
            Timber.d("Shutdown the Twilio Conversations Client")
        } else {
            throw IllegalStateException("Cannot disconnect while in the disconnected state")
        }
    }

    private val conversationsClientCallback: CallbackListener<ConversationsClient> = object : CallbackListener<ConversationsClient> {
        override fun onSuccess(conversationsClient: ConversationsClient) {
            Timber.d("Success creating Twilio Conversations Client, now synchronizing...")
            client = conversationsClient
            conversationsClient.addListener(conversationsClientListener)
        }

        override fun onError(errorInfo: ErrorInfo) {
            Timber.e("Error connecting to client: $errorInfo")
            sendEvent(ClientConnectFailure)
            updateState { it.copy(connectionState = Disconnected) }
        }
    }

    private val conversationCallback: CallbackListener<Conversation> = object : CallbackListener<Conversation> {
        override fun onSuccess(conversation: Conversation) {
            Timber.d("Successfully Joined Conversation")
            this@ChatManagerImpl.conversation = conversation
            conversation.getLastMessages(MESSAGE_READ_COUNT, getMessagesCallback)
        }

        override fun onError(errorInfo: ErrorInfo) {
            Timber.e("Error joining conversation: $errorInfo")
            sendEvent(ConversationJoinFailure)
            updateState { it.copy(connectionState = Disconnected) }
        }
    }

    private val getMessagesCallback = object : CallbackListener<List<Message>> {
        override fun onSuccess(messages: List<Message>) {
            Timber.d("Successfully read ${messages.size} messages")
            updateState {
                it.copy(connectionState = Connected, messages = messages.map { message ->
                    ChatMessage(message.sid, message.messageBody)
                })
            }
        }

        override fun onError(errorInfo: ErrorInfo) {
            Timber.e("Error retrieving the last $MESSAGE_READ_COUNT messages from the conversation: $errorInfo")
            sendEvent(GetMessagesFailure)
            updateState { it.copy(connectionState = Disconnected) }
        }
    }

    private val conversationsClientListener: ConversationsClientListener = object : ConversationsClientListener {
        override fun onConversationAdded(conversation: Conversation) {}
        override fun onConversationUpdated(conversation: Conversation, updateReason: Conversation.UpdateReason) {}
        override fun onConversationDeleted(conversation: Conversation) {}
        override fun onConversationSynchronizationChange(conversation: Conversation) {}
        override fun onError(errorInfo: ErrorInfo) { Timber.e("A client error occurred: $errorInfo") }
        override fun onUserUpdated(user: User, updateReason: User.UpdateReason) {}
        override fun onUserSubscribed(user: User) {}
        override fun onUserUnsubscribed(user: User) {}
        override fun onClientSynchronization(synchronizationStatus: SynchronizationStatus) {
            Timber.d("Client synchronization status: $synchronizationStatus")
            when (synchronizationStatus) {
                SynchronizationStatus.COMPLETED -> joinConversation()
                SynchronizationStatus.FAILED -> {
                    sendEvent(ChatEvent.ClientSynchronizationFailure)
                    updateState { it.copy(connectionState = Disconnected) }
                }
                else -> {}
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

    private fun updateState(action: (oldState: ChatState) -> ChatState) {
        chatScope.launch {
            chatStateFlow.value = action(chatStateFlow.value)
            Timber.d("New ChatState: ${chatStateFlow.value}")
        }
    }

    private fun sendEvent(chatEvent: ChatEvent) {
        chatScope.launch {
            chatEventFlow.emit(chatEvent)
            Timber.d("New ChatEvent: ${chatStateFlow.value}")
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
