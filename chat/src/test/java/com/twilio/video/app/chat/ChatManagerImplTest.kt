package com.twilio.video.app.chat

import android.content.Context
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.conversations.CallbackListener
import com.twilio.conversations.Conversation
import com.twilio.conversations.ConversationsClient
import com.twilio.conversations.ConversationsClient.SynchronizationStatus
import com.twilio.conversations.ConversationsClientListener
import com.twilio.conversations.ErrorInfo
import com.twilio.conversations.ErrorInfo.CANNOT_GET_MESSAGE_BY_INDEX
import com.twilio.conversations.ErrorInfo.CLIENT_ERROR
import com.twilio.conversations.ErrorInfo.CONVERSATION_NOT_FOUND
import com.twilio.conversations.Message
import com.twilio.video.app.chat.ChatEvent.ClientConnectFailure
import com.twilio.video.app.chat.ChatEvent.ClientSynchronizationFailure
import com.twilio.video.app.chat.ChatEvent.ConversationJoinFailure
import com.twilio.video.app.chat.ChatEvent.GetMessagesFailure
import com.twilio.video.app.chat.ConnectionState.Connected
import com.twilio.video.app.chat.ConnectionState.Connecting
import com.twilio.video.app.chat.ConnectionState.Disconnected
import com.twilio.video.app.chat.sdk.ConversationsClientWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class ChatManagerImplTest {

    private val testScope: CoroutineScope = CoroutineScope(TestCoroutineDispatcher())
    private val conversationsClientWrapper = mock<ConversationsClientWrapper>()
    private val context = mock<Context>()
    private val conversation = mock<Conversation>()
    private val client = mock<ConversationsClient>()
    private val chatManager: ChatManager = ChatManagerImpl(context, conversationsClientWrapper, testScope)

    @Test
    fun `Successfully connecting to the client should update the connection state to connected`() {
            whenever(conversationsClientWrapper.create(eq(context), eq("token"), isA(), isA()))
                    .thenAnswer {
                        (it.getArgument(3) as CallbackListener<ConversationsClient>).onSuccess(client)
                    }
            whenever(client.addListener(isA())).thenAnswer {
                (it.getArgument(0) as ConversationsClientListener).onClientSynchronization(SynchronizationStatus.COMPLETED)
            }
            whenever(client.getConversation(eq("chat"), isA())).thenAnswer {
                (it.getArgument(1) as CallbackListener<Conversation>).onSuccess(conversation)
            }
            val messages = listOf<Message>(mock {
                whenever(mock.messageBody).thenReturn("Test Message")
                whenever(mock.sid).thenReturn("123")
            })
            whenever(conversation.getLastMessages(eq(MESSAGE_READ_COUNT), isA())).thenAnswer {
                (it.getArgument(1) as CallbackListener<List<Message>>).onSuccess(messages)
            }
            val expectedMessages = listOf(ChatMessage("123", "Test Message"))
            val expectedStates = listOf(
                    ChatState(),
                    ChatState(Connecting),
                    ChatState(Connected, expectedMessages)
            )
            val testValues = mutableListOf<ChatState>()
            val testJob = testScope.launch { chatManager.chatState.collect { testValues.add(it) } }

            chatManager.connect("token", "chat")
            testJob.cancel()
            assertThat(testValues, equalTo(expectedStates))
    }

    @Test
    fun `Failing to connect the client should update the connection state to disconnected and send a ClientConnectFailure event`() {
        whenever(conversationsClientWrapper.create(eq(context), eq("token"), isA(), isA()))
                .thenAnswer {
                    (it.getArgument(3) as CallbackListener<ConversationsClient>)
                            .onError(ErrorInfo(CLIENT_ERROR, "Test error!"))
                }
        val expectedStates = listOf(
                ChatState(),
                ChatState(Connecting),
                ChatState(Disconnected)
        )
        val expectedEvents = listOf(
                ClientConnectFailure
        )
        val testStates = mutableListOf<ChatState>()
        val testEvents = mutableListOf<ChatEvent>()
        val testChatStateJob = testScope.launch { chatManager.chatState.collect { testStates.add(it) } }
        val testChatEventJob = testScope.launch { chatManager.chatEvents.collect { testEvents.add(it) } }

        chatManager.connect("token", "chat")
        testChatStateJob.cancel()
        testChatEventJob.cancel()
        assertThat(testStates, equalTo(expectedStates))
        assertThat(testEvents, equalTo(expectedEvents))
    }

    @Test
    fun `Failing to synchronize the client should update the connection state to disconnected and send a ClientSynchronizationFailure event`() {
        whenever(conversationsClientWrapper.create(eq(context), eq("token"), isA(), isA()))
                .thenAnswer {
                    (it.getArgument(3) as CallbackListener<ConversationsClient>).onSuccess(client)
                }
        whenever(client.addListener(isA())).thenAnswer {
            (it.getArgument(0) as ConversationsClientListener).onClientSynchronization(SynchronizationStatus.FAILED)
        }
        val expectedStates = listOf(
                ChatState(),
                ChatState(Connecting),
                ChatState(Disconnected)
        )
        val expectedEvents = listOf(
                ClientSynchronizationFailure
        )
        val testStates = mutableListOf<ChatState>()
        val testEvents = mutableListOf<ChatEvent>()
        val testChatStateJob = testScope.launch { chatManager.chatState.collect { testStates.add(it) } }
        val testChatEventJob = testScope.launch { chatManager.chatEvents.collect { testEvents.add(it) } }

        chatManager.connect("token", "chat")
        testChatStateJob.cancel()
        testChatEventJob.cancel()
        assertThat(testStates, equalTo(expectedStates))
        assertThat(testEvents, equalTo(expectedEvents))
    }

    @Test
    fun `Failing to join the conversation should update the connection state to disconnected and send a ConversationJoinFailure event`() {
        whenever(conversationsClientWrapper.create(eq(context), eq("token"), isA(), isA()))
                .thenAnswer {
                    (it.getArgument(3) as CallbackListener<ConversationsClient>).onSuccess(client)
                }
        whenever(client.addListener(isA())).thenAnswer {
            (it.getArgument(0) as ConversationsClientListener).onClientSynchronization(SynchronizationStatus.COMPLETED)
        }
        whenever(client.getConversation(eq("chat"), isA())).thenAnswer {
            (it.getArgument(1) as CallbackListener<Conversation>)
                    .onError(ErrorInfo(CONVERSATION_NOT_FOUND, "Test error!"))
        }
        val expectedStates = listOf(
                ChatState(),
                ChatState(Connecting),
                ChatState(Disconnected)
        )
        val expectedEvents = listOf(
                ConversationJoinFailure
        )
        val testStates = mutableListOf<ChatState>()
        val testEvents = mutableListOf<ChatEvent>()
        val testChatStateJob = testScope.launch { chatManager.chatState.collect { testStates.add(it) } }
        val testChatEventJob = testScope.launch { chatManager.chatEvents.collect { testEvents.add(it) } }

        chatManager.connect("token", "chat")
        testChatStateJob.cancel()
        testChatEventJob.cancel()
        assertThat(testStates, equalTo(expectedStates))
        assertThat(testEvents, equalTo(expectedEvents))
    }

    @Test
    fun `Failing to get the messages should update the connection state to disconnected and send a GetMessagesFailure event`() {
        whenever(conversationsClientWrapper.create(eq(context), eq("token"), isA(), isA()))
                .thenAnswer {
                    (it.getArgument(3) as CallbackListener<ConversationsClient>).onSuccess(client)
                }
        whenever(client.addListener(isA())).thenAnswer {
            (it.getArgument(0) as ConversationsClientListener).onClientSynchronization(SynchronizationStatus.COMPLETED)
        }
        whenever(client.getConversation(eq("chat"), isA())).thenAnswer {
            (it.getArgument(1) as CallbackListener<Conversation>).onSuccess(conversation)
        }
        whenever(conversation.getLastMessages(eq(MESSAGE_READ_COUNT), isA())).thenAnswer {
            (it.getArgument(1) as CallbackListener<List<Message>>)
                    .onError(ErrorInfo(CANNOT_GET_MESSAGE_BY_INDEX, "Test error!"))
        }
        val expectedStates = listOf(
                ChatState(),
                ChatState(Connecting),
                ChatState(Disconnected)
        )
        val expectedEvents = listOf(
                GetMessagesFailure
        )
        val testStates = mutableListOf<ChatState>()
        val testEvents = mutableListOf<ChatEvent>()
        val testChatStateJob = testScope.launch { chatManager.chatState.collect { testStates.add(it) } }
        val testChatEventJob = testScope.launch { chatManager.chatEvents.collect { testEvents.add(it) } }

        chatManager.connect("token", "chat")
        testChatStateJob.cancel()
        testChatEventJob.cancel()
        assertThat(testStates, equalTo(expectedStates))
        assertThat(testEvents, equalTo(expectedEvents))
    }

    @Test
    fun `Successfully disconnecting the client should update the connection state to disconnected`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `Successfully disconnecting the client should set the old client reference to null`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `Should be able to connect a new client after calling disconnect`() {
        TODO("Not yet implemented")
    }
}
