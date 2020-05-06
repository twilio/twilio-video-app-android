package com.twilio.video.app.udf

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer

fun <T, U : Any, V> Fragment.observeViewState(
    viewModel: BaseViewModel<T, U, V>,
    action: (viewState: U) -> Unit
) {

    viewModel.viewState.observe(this, Observer { action(it) })
}

fun <T, U : Any, V> Fragment.observeViewEffects(
    viewModel: BaseViewModel<T, U, V>,
    action: (viewEffect: V) -> Unit
) {

    viewModel.viewEffects.observe(this, Observer {
        it.getContentIfNotHandled()?.let { viewEffect -> action(viewEffect) }
    })
}