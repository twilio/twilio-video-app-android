package com.twilio.video.app.udf

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * This class provides the boilerplate code needed for a UDF implementation in the ViewModel layer.
 *
 * @param T The type used to encapsulate incoming view events in [processInput]
 * @param U The type used to encapsulate the view state to later be rendered by the UI
 * @param V The type used to represent view effects, which are one time view events that are not
 * persisted
 * @param initialViewState The initial view state
 */
abstract class BaseViewModel<T, U : Any, V>(initialViewState: U) : ViewModel() {
    private val mutableViewState = MutableLiveData(initialViewState)
    private val mutableViewEffects = MutableLiveData<ViewEffect<V>>()
    val viewState: LiveData<U> = mutableViewState
    val viewEffects: LiveData<ViewEffect<V>> = mutableViewEffects

    abstract fun processInput(viewEvent: T)

    protected fun updateState(action: (oldState: U) -> U) {
        withState { currentState ->
            mutableViewState.value = action(currentState)
        }
    }

    protected fun <R> withState(action: (currentState: U) -> R): R =
        // Force unwrap used since an initial view state is always set
        action(mutableViewState.value!!)

    protected fun viewEffect(action: () -> V) {
        mutableViewEffects.value = ViewEffect(action())
    }
}
