package com.twilio.video.app.ui.room

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.twilio.video.ConnectOptions
import com.twilio.video.Video
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.data.api.model.RoomProperties
import kotlinx.coroutines.launch

class RoomViewModel(private val context: Context,
                    private val tokenService: TokenService,
                    private val roomManager: RoomManager): ViewModel() {

    // TODO Use a ViewState instead of RoomEvents
    private val mutableRoomEvents = MutableLiveData<RoomEvent>()

    val roomEvents: LiveData<RoomEvent> = mutableRoomEvents
    fun retrieveToken(roomProperties: RoomProperties, identity: String): String {
        lateinit var token: String
        viewModelScope.launch {
            token = tokenService.getToken(identity, roomProperties)
            Video.connect(
                    context,
                    connectOptions,
                    roomManager.roomListener)
        }
        return token
    }

    private suspend fun connectToRoom(context: Context,
                                      token: String,
                                      roomName: String,
                                      sharedPreferences: SharedPreferences,
                                      connectOptions: ConnectOptions) {

    }

    class RoomViewModelFactory(
            private val context: Context
            private val tokenService: TokenService,
            private val roomManager: RoomManager): ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RoomViewModel(context, tokenService, roomManager) as T
        }

    }
}
