package com.twilio.video.app.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChatTestActivity : AppCompatActivity() {

    private var uiStateJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_test)
    }

    override fun onStart() {
        super.onStart()

        val chatToken = ""
        val chatName = ""
        val chatManager: ChatManager = ChatManagerImpl()
        val chatFlow = chatManager.connect(chatToken, chatName)
        uiStateJob = lifecycleScope.launch {
            chatFlow.collect { event: ChatEvent -> handleChatEvent() }
        }
    }

    private fun handleChatEvent(event: ChatEvent) {
    }

    override fun onStop() {
        uiStateJob?.cancel()
        super.onStop()
    }
}
