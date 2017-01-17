package com.twilio.video.app.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.twilio.video.app.R;

import butterknife.ButterKnife;

public class ParticipantThumbView extends ParticipantView {

    public ParticipantThumbView(Context context) {
        super(context);
        init(context, null);
    }

    public ParticipantThumbView(Context context,
                                AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ParticipantThumbView(Context context,
                                AttributeSet attrs,
                                int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ParticipantThumbView(Context context,
                                AttributeSet attrs,
                                int defStyleAttr,
                                int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
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

        int resId = R.drawable.participant_background;
        if (state == State.SELECTED) {
            resId = R.drawable.participant_selected_background;
        }
        selectedLayout.setBackground(ContextCompat.getDrawable(getContext(), resId));
    }
}
