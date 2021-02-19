package com.twilio.video.app.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

private const val chatToken = ""
private const val chatName = ""

class ChatTestActivity : AppCompatActivity() {

    private lateinit var chatManager: ChatManager
    private var uiStateJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_test)

        chatManager = ChatManagerImpl(applicationContext)
    }

    override fun onStart() {
        super.onStart()

        uiStateJob = lifecycleScope.launch {
            chatManager.chatState.collect { state: ChatState -> handleChatState(state) }
        }
        chatManager.connect(chatToken, chatName)
    }

    private fun handleChatState(state: ChatState) {
        Timber.d(state.toString())
    }

    override fun onStop() {
        uiStateJob?.cancel()
        super.onStop()
    }
}
