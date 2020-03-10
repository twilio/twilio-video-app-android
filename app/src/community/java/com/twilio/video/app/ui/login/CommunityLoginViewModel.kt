package com.twilio.video.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twilio.video.app.auth.Authenticator
import com.twilio.video.app.ui.mvvm.BaseViewModel

class CommunityLoginViewModel(private val authenticator: Authenticator):
        BaseViewModel<CommunityLoginViewEvent, CommunityLoginViewState>() {

    override fun processInput(viewEvent: CommunityLoginViewEvent) {
        TODO("Not yet implemented")
    }
}

class CommunityLoginViewModelFactory(private val authenticator: Authenticator) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CommunityLoginViewModel(authenticator) as T
    }
}
