package com.twilio.video.app.ui.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

private const val NULL_VIEW_STATE_ERROR = "ViewState can never be null"

abstract class BaseViewModel<VIEW_EVENT, VIEW_STATE : Any>(initialViewState: VIEW_STATE) : ViewModel() {
    internal val mutableViewState = MutableLiveData<VIEW_STATE>().apply { value = initialViewState }
    val viewState: LiveData<VIEW_STATE> = mutableViewState
    internal val viewEffectsSubject = PublishSubject.create<VIEW_STATE>()
    // TODO Use LiveData for view effects.
    val viewEffects = viewEffectsSubject.hide()
    internal val rxDisposables : CompositeDisposable = CompositeDisposable()

    abstract fun processInput(viewEvent: VIEW_EVENT)

    protected fun updateState(action: (oldState: VIEW_STATE) -> VIEW_STATE) {
        withState { currentState ->
            mutableViewState.value = action(currentState)
        }
    }

    protected fun viewEffect(action: (oldState: VIEW_STATE) -> VIEW_STATE) {
        withState {currentState ->
            viewEffectsSubject.onNext(action(currentState))
        }
    }

    protected fun withState(action : (currentState: VIEW_STATE) -> Unit) {
        val oldState = mutableViewState.value
        oldState?.let {
            action(oldState)
        } ?: throw IllegalStateException(NULL_VIEW_STATE_ERROR)
    }

    override fun onCleared() {
        super.onCleared()
        rxDisposables.clear()
    }

    // TODO Figure out better way to test onCleared
    internal fun onClearedTest() {
        onCleared()
    }
}