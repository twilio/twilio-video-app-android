package com.twilio.video.compatibility;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

/*
 * This view is used to validate cases where applications or other libraries contain attribute names
 * already defined in the Video SDK. The current attributes represent the attribute names of
 * VideoView and VideoTextureView before Video Android 5.4.0. All attributes in Video Android 5.4.0+
 * are prefixed with "tvi" to prevent attribute name collisions that result in compile time failures
 * when merging application resources.
 */
public class CustomView extends View {
    private boolean mirror = false;
    private boolean overlaySurface = false;
    private int videoScaleType = 0;

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a =
                context.getTheme()
                        .obtainStyledAttributes(
                                attrs, com.twilio.video.compatibility.R.styleable.CustomView, 0, 0);

        try {
            mirror =
                    a.getBoolean(
                            com.twilio.video.compatibility.R.styleable.CustomView_mirror, false);
            videoScaleType =
                    a.getInteger(
                            com.twilio.video.compatibility.R.styleable.CustomView_scaleType, 0);
            overlaySurface =
                    a.getBoolean(
                            com.twilio.video.compatibility.R.styleable.CustomView_overlaySurface,
                            false);
        } finally {
            a.recycle();
        }
    }
}
