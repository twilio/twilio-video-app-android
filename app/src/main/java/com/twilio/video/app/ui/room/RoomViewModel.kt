package com.twilio.video.app.ui.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.data.api.model.RoomProperties
import kotlinx.coroutines.launch
import javax.inject.Inject

class RoomViewModel: ViewModel() {

    // TODO Inject in constructor for easier unit testing
    @Inject lateinit var tokenService: TokenService

    // TODO Use a ViewState instead of RoomEvents
    private val mutableRoomEvents = MutableLiveData<RoomEvent>()
    val roomEvents: LiveData<RoomEvent> = mutableRoomEvents

    fun connectToRoom(roomProperties: RoomProperties, identity: String): String {
        lateinit var token: String
        viewModelScope.launch {
            token = tokenService.getToken(identity, roomProperties)
        }
        return token
    }

    fun connectToRoom() {
    }
}
