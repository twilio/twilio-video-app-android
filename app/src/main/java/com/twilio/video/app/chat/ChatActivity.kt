package com.twilio.video.app.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import com.twilio.video.app.R
import com.twilio.video.app.databinding.ActivityChatBinding
import timber.log.Timber

class ChatActivity :
        AppCompatActivity(),
        MessagesListAdapter.OnLoadMoreListener,
        MessagesListAdapter.SelectionListener,
        MessageInput.InputListener,
        MessageInput.AttachmentsListener {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messagesAdapter: MessagesListAdapter<IMessage>
    private lateinit var messageList: MessagesList
    private lateinit var input: MessageInput
    private val localUser = UserImpl("Local User", "Local User Avatar")
    private val remoteUser = UserImpl("Remote User", "Remote User Avatar")
    private val testMessages = listOf(
            MessageImpl("Blah1", localUser),
            MessageImpl("Blah2", localUser),
            MessageImpl("Hi", remoteUser),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messageList = binding.messagesList

        initAdapter()
        initInput()
    }

    private fun initAdapter() {
        messagesAdapter = MessagesListAdapter<IMessage>(localUser.id, ImageLoaderImpl()).apply {
            addToEnd(testMessages, false)
            enableSelectionMode(this@ChatActivity)
            setLoadMoreListener(this@ChatActivity)
            registerViewClickListener(R.id.messageUserAvatar
            ) { _, message ->
                Timber.d("%s avatar click", message.user.name)
            }
        }
        messageList.setAdapter(messagesAdapter)
    }

    private fun initInput() {
        input = binding.input
        input.setInputListener(this)
        input.setAttachmentsListener(this)
    }

    override fun onLoadMore(page: Int, totalItemsCount: Int) {
        Timber.d("onLoadMore: page: $page, totalItemsCount: $totalItemsCount")
    }

    override fun onSelectionChanged(count: Int) {
        Timber.d("onSelectionChanged: Count: $count")
    }

    override fun onSubmit(input: CharSequence?): Boolean {
        Timber.d("onSubmit: $input")
        return true
    }

    override fun onAddAttachments() {
        Timber.d("onAddAttachment")
    }
}