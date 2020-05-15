package com.twilio.video.app.ui.room

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.twilio.video.app.participant.ParticipantViewState
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

internal class ParticipantAdapter : ListAdapter<ParticipantViewState, ParticipantViewHolder>(
        ParticipantDiffCallback()) {

    private val viewHolderEventsSubject = PublishSubject.create<RoomViewEvent>()
    val viewHolderEvents: Observable<RoomViewEvent> = viewHolderEventsSubject

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder =
            ParticipantViewHolder(ParticipantThumbView(parent.context))

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) =
            holder.bind(getItem(position)) {
                viewEvent -> viewHolderEventsSubject.onNext(viewEvent)
            }

    class ParticipantDiffCallback : DiffUtil.ItemCallback<ParticipantViewState>() {
        override fun areItemsTheSame(
            oldItem: ParticipantViewState,
            newItem: ParticipantViewState
        ): Boolean =
                oldItem.sid == newItem.sid

        override fun areContentsTheSame(
            oldItem: ParticipantViewState,
            newItem: ParticipantViewState
        ): Boolean =
                oldItem == newItem
    }
}