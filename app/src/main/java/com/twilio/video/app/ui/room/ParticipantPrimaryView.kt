/*
 * Copyright (C) 2021 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.twilio.video.app.ui.room

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import com.twilio.video.app.databinding.ParticipantPrimaryViewBinding

internal class ParticipantPrimaryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ParticipantView(context, attrs, defStyleAttr) {

    private val binding: ParticipantPrimaryViewBinding =
        ParticipantPrimaryViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val transcriptionTextWidget: TextView
    init {
        videoLayout = binding.videoLayout
        videoIdentity = binding.videoIdentity
        videoView = binding.video
        selectedLayout = binding.selectedLayout
        stubImage = binding.stub
        selectedIdentity = binding.selectedIdentity
        transcriptionTextWidget = binding.videoTranscriptionText
        setIdentity(identity)
        setState(state)
        setMirror(mirror)
        setScaleType(scaleType)
    }

    var transcriptionText: String
        set(value) {
            transcriptionTextWidget.text = value
        }
        get() = transcriptionTextWidget.text.toString()

    fun showIdentityBadge(show: Boolean) {
        binding.videoIdentity.visibility = if (show) VISIBLE else GONE
    }
}
