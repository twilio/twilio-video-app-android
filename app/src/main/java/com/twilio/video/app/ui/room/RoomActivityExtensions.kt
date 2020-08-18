package com.twilio.video.app.ui.room

import io.uniflow.androidx.flow.AndroidDataFlow
import io.uniflow.androidx.flow.onEvents
import io.uniflow.androidx.flow.onStates

fun RoomActivity.onStates(dataFlow: AndroidDataFlow, action: (RoomViewState) -> Unit) {
    onStates(dataFlow) { state ->
        if (state is RoomViewState) action.invoke(state)
    }
}

fun RoomActivity.onEvents(dataFlow: AndroidDataFlow, action: (RoomViewEffect) -> Unit) {
    onEvents(dataFlow) { eventWrapper ->
        eventWrapper.take()?.let { event ->
            if (event is RoomViewEffect) action.invoke(event)
        }
    }
}
