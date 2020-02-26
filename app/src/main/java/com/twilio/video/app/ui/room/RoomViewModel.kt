package com.twilio.video.app.ui.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.data.api.model.RoomProperties
import kotlinx.coroutines.launch

class RoomViewModel(private val tokenService: TokenService): ViewModel() {

    // TODO Use a ViewState instead of RoomEvents
    private val mutableRoomEvents = MutableLiveData<RoomEvent>()

    val roomEvents: LiveData<RoomEvent> = mutableRoomEvents
    fun retrieveToken(roomProperties: RoomProperties, identity: String): String {
        lateinit var token: String
        viewModelScope.launch {
            token = tokenService.getToken(identity, roomProperties)
        }
        return token
    }

    class RoomViewModelFactory(private val tokenService: TokenService): ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RoomViewModel(tokenService) as T
        }

    }
}
