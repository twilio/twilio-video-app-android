/*
 * Copyright (C) 2017 Twilio, Inc.
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
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.twilio.video.I420Frame;
import com.twilio.video.VideoRenderer;
import com.twilio.video.VideoScaleType;
import com.twilio.video.VideoView;
import com.twilio.video.app.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import butterknife.BindView;

abstract class ParticipantView extends FrameLayout implements VideoRenderer {

    protected String identity = "";
    protected int state = State.NO_VIDEO;
    protected boolean mirror = false;
    protected int scaleType = VideoScaleType.ASPECT_BALANCED.ordinal();
    protected boolean overlaySurface = true;

    @BindView(R.id.participant_video_layout) FrameLayout videoLayout;
    @Nullable @BindView(R.id.participant_badge) RelativeLayout identityBadge;
    @BindView(R.id.participant_video_identity) TextView videoIdentity;
    @BindView(R.id.participant_video) VideoView videoView;

    @BindView(R.id.participant_selected_layout) RelativeLayout selectedLayout;
    @BindView(R.id.participant_stub_image) ImageView stubImage;
    @BindView(R.id.participant_selected_identity) TextView selectedIdentity;

    @BindView(R.id.participant_no_audio) ImageView audioToggle;

    public ParticipantView(@NonNull Context context) {
        super(context);
        initParams(context, null);
    }

    public ParticipantView(@NonNull Context context,
                           @Nullable AttributeSet attrs) {
        super(context, attrs);
        initParams(context, attrs);
    }

    public ParticipantView(@NonNull Context context,
                           @Nullable AttributeSet attrs,
                           @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParams(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ParticipantView(@NonNull Context context,
                           @Nullable AttributeSet attrs,
                           @AttrRes int defStyleAttr,
                           @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initParams(context, attrs);
    }

    public void setIdentity(String identity) {
        this.identity = identity;
        videoIdentity.setText(identity);
        selectedIdentity.setText(identity);
    }

    public void setState(int state) {
        this.state = state;
        switch (state) {
            case State.VIDEO:
                selectedLayout.setVisibility(GONE);
                stubImage.setVisibility(GONE);
                selectedIdentity.setVisibility(GONE);

                videoLayout.setVisibility(VISIBLE);
                videoIdentity.setVisibility(VISIBLE);
                videoView.setVisibility(VISIBLE);
                break;
            case State.NO_VIDEO:
            case State.SELECTED:
                videoLayout.setVisibility(GONE);
                videoIdentity.setVisibility(GONE);
                videoView.setVisibility(GONE);

                selectedLayout.setVisibility(VISIBLE);
                stubImage.setVisibility(VISIBLE);
                selectedIdentity.setVisibility(VISIBLE);
                break;
            default:
                break;
        }
    }

    public void setMirror(boolean mirror) {
        this.mirror = mirror;
        videoView.setMirror(this.mirror);
    }

    public void setScaleType(int scaleType) {
        this.scaleType = scaleType;
        videoView.setVideoScaleType(VideoScaleType.values()[this.scaleType]);
    }

    public void setMuted(boolean muted) {
        audioToggle.setVisibility(muted ? VISIBLE : GONE);
    }

    @Override
    public void renderFrame(I420Frame frame) {
        videoView.renderFrame(frame);
    }

    protected void initParams(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray stylables = context.getTheme()
                    .obtainStyledAttributes(attrs, R.styleable.ParticipantView, 0, 0);

            // obtain identity
            int identityResId = stylables.getResourceId(R.styleable.ParticipantView_identity, -1);
            identity = (identityResId != -1) ? context.getString(identityResId) : "";

            // obtain state
            state = stylables.getInt(R.styleable.ParticipantView_state, ParticipantView.State.NO_VIDEO);

            // obtain mirror
            mirror = stylables.getBoolean(R.styleable.ParticipantView_mirror, false);

            // obtain scale type
            scaleType = stylables.getInt(R.styleable.ParticipantView_type,
                    VideoScaleType.ASPECT_BALANCED.ordinal());

            // obtain overlay
            overlaySurface = stylables.getBoolean(R.styleable.ParticipantView_overlaySurface, true);

            stylables.recycle();
        }
    }

    @IntDef({
            ParticipantView.State.VIDEO,
            ParticipantView.State.NO_VIDEO,
            ParticipantView.State.SELECTED
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface State {
        int VIDEO = 0;
        int NO_VIDEO = 1;
        int SELECTED = 2;
    }
}
