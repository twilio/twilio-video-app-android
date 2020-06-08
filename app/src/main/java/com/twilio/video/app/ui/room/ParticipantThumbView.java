/*
 * Copyright (C) 2019 Twilio, Inc.
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

package com.twilio.video.app.ui.room;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.twilio.video.app.R;

public class ParticipantThumbView extends ParticipantView {

    @BindView(R.id.participant_track_switch_off_background)
    View trackSwitchOffBackground;

    @BindView(R.id.participant_track_switch_off_icon)
    ImageView trackSwitchOffImage;

    public ParticipantThumbView(Context context) {
        super(context);
        init(context);
    }

    public ParticipantThumbView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ParticipantThumbView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ParticipantThumbView(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.participant_view, this);
        ButterKnife.bind(this, view);

        setIdentity(identity);
        setState(state);
        setMirror(mirror);
        setScaleType(scaleType);
    }

    @Override
    public void setState(int state) {
        super.setState(state);

        trackSwitchOffBackground.setVisibility(isSwitchOffViewVisible(state));
        trackSwitchOffImage.setVisibility(isSwitchOffViewVisible(state));

        int resId = R.drawable.participant_background;
        if (state == State.SELECTED) {
            resId = R.drawable.participant_selected_background;
        }
        selectedLayout.setBackground(ContextCompat.getDrawable(getContext(), resId));
    }

    private int isSwitchOffViewVisible(int state) {
        return state == State.SWITCHED_OFF ? View.VISIBLE : View.GONE;
    }
}
