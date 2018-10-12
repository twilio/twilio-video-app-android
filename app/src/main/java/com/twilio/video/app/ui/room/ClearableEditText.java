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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.twilio.video.app.R;

/**
 * ClearableEditText is an extension for standard EditText with an extra option to setup clear icon
 * with as right compound drawable, which handles clear icon touch event as erase for the contents
 * of user input.
 *
 * @attr name clearIcon - clear action icon to display to the right of EditText input.
 */
public class ClearableEditText extends AppCompatEditText {
    /** Clear icon resource id. */
    private int clearIconResId;

    /** Clear icon drawable. */
    private Drawable clearDrawable;

    public ClearableEditText(Context context) {
        super(context);
        init(context, null);
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        if (attrs != null) {
            TypedArray stylables =
                    context.getTheme()
                            .obtainStyledAttributes(attrs, R.styleable.ClearableEditText, 0, 0);

            // obtain clear icon resource id
            clearIconResId = stylables.getResourceId(R.styleable.ClearableEditText_clearIcon, -1);
            if (clearIconResId != -1) {
                clearDrawable = VectorDrawableCompat.create(getResources(), clearIconResId, null);
            }
        }

        // setup initial clear icon state
        setCompoundDrawablesWithIntrinsicBounds(null, null, clearDrawable, null);
        showClearIcon(getText().toString().length() > 0);

        // update clear icon state after every text change
        addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void afterTextChanged(Editable editable) {
                        showClearIcon(editable.toString().length() > 0);
                    }
                });

        // simulate on clear icon click - delete edit text contents
        setOnTouchListener(
                (view, motionEvent) -> {
                    if (isClearVisible() && motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        ClearableEditText editText = (ClearableEditText) view;
                        Rect bounds = clearDrawable.getBounds();

                        if (motionEvent.getRawX() >= (view.getRight() - bounds.width())) {
                            editText.setText("");
                        }
                    }

                    return false;
                });
    }

    /**
     * Displays clear icon in ClearableEditText.
     *
     * @param show pass true to display icon, otherwise false to hide.
     */
    public void showClearIcon(boolean show) {
        // TODO: should probably use setVisibility method, but seems to not working.
        if (clearDrawable != null) {
            clearDrawable.setAlpha(show ? 255 : 0);
        }
    }

    /**
     * Reflects current state of clear icon.
     *
     * @return true if active, otherwise - false.
     */
    public boolean isClearVisible() {
        return clearDrawable != null && DrawableCompat.getAlpha(clearDrawable) == 255;
    }
}
