package com.twilio.video.app.ui.room;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.twilio.video.app.R;

import butterknife.ButterKnife;

public class ParticipantPrimaryView extends ParticipantView {

    public ParticipantPrimaryView(Context context) {
        super(context);
        init(context, null);
    }

    public ParticipantPrimaryView(Context context,
                                  AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ParticipantPrimaryView(Context context,
                                  AttributeSet attrs,
                                  int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ParticipantPrimaryView(Context context,
                                  AttributeSet attrs,
                                  int defStyleAttr,
                                  int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public void showIdentityBadge(boolean show) {
        identityBadge.setVisibility(show ? VISIBLE : GONE);
    }

    private void init(Context context, AttributeSet attrs) {
        View view = LayoutInflater.from(context).inflate(R.layout.participant_view_primary, this);
        ButterKnife.bind(this, view);

        setIdentity(identity);
        setState(state);
        setMirror(mirror);
        setScaleType(scaleType);
    }
}
