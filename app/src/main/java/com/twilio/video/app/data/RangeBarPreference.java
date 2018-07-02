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

package com.twilio.video.app.data;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.appyvet.rangebar.IRangeBarFormatter;
import com.appyvet.rangebar.RangeBar;
import com.twilio.video.app.R;

/**
 * RangeBarPreference allows to save any range in default shared preferences by saving range start
 * and range end values.
 *
 * <p>Customizable options:
 *
 * @attr name entries - collection of string to use as display value
 * @attr name heightResId - range bar height
 * @attr name startTick - range bar start
 * @attr name endTick - range bar end
 * @attr name pinRadius - size of pin
 * @attr name startKey - string used as key to save value in shared preferences.
 * @attr name endKey - string used as key to save value in shared preferences.
 */
public class RangeBarPreference extends Preference {

    /** Default shared preferences instance. */
    private SharedPreferences sharedPreferences;

    /** Customizable range bar. */
    private RangeBar rangeBar;

    /**
     * RangeBar pin visible text formatter. If entries array is provided utilizes string values from
     * the provided array, otherwise falls back to integers.
     */
    private IRangeBarFormatter formatter;

    /** Range of values. */
    private String[] entries;

    /** Range start key - used in shared preferences to save range start point. */
    private String startKey;

    /** Range end key - used in shared preferences to save range end point. */
    private String endKey;

    /** RangeBar provided setting. NOTE: uses this value if no entries provided. */
    private int startTick;

    /** RangeBar provided setting. NOTE: uses this value if no entries provided. */
    private int endTick;

    /** RangeBar provided setting. Specifies the size of tick used. */
    private float pinRadius;

    /** RangeBar height. Applies custom height from xml file. */
    private int heightResId;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RangeBarPreference(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public RangeBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public RangeBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RangeBarPreference(Context context) {
        super(context);
        init(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        // obtain resources for range bar height calculations.
        Resources resources = holder.itemView.getContext().getResources();

        rangeBar = (RangeBar) holder.findViewById(R.id.range_bar);

        // apply custom height
        rangeBar.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        resources.getDimensionPixelSize(heightResId)));

        // setup preference range
        if (entries != null) {
            startTick = 0;
            endTick = entries.length - 1;
        }

        rangeBar.setTickStart(startTick);
        rangeBar.setTickEnd(endTick);

        // obtain selected range
        int start = sharedPreferences.getInt(startKey, -1);
        int end = sharedPreferences.getInt(endKey, -1);

        // no values saved previously
        if (start == -1 || end == -1) {

            // obtain range from entries array size as default setting
            start = startTick;
            end = endTick;
        }

        rangeBar.setRangePinsByValue(start, end);

        // apply pin size
        if (pinRadius != -1.0f) {
            rangeBar.setPinRadius(pinRadius);
        }

        // apply range bar value text formatter visible on choosing pins
        if (rangeBar != null) {

            if (formatter != null) {
                rangeBar.setFormatter(formatter);
            }

            // save all changes prefs while moving pins
            rangeBar.setOnRangeBarChangeListener(
                    new RangeBar.OnRangeBarChangeListener() {
                        @Override
                        public void onRangeChangeListener(
                                RangeBar rangeBar,
                                int leftPinIndex,
                                int rightPinIndex,
                                String leftPinValue,
                                String rightPinValue) {

                            sharedPreferences
                                    .edit()
                                    .putInt(startKey, leftPinIndex)
                                    .putInt(endKey, rightPinIndex)
                                    .apply();
                        }
                    });
        }
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        // apply custom layout
        setLayoutResource(R.layout.range_bar_preference);

        if (attrs != null) {
            TypedArray stylables =
                    context.getTheme()
                            .obtainStyledAttributes(attrs, R.styleable.RangeBarPreference, 0, 0);

            // range bar height
            heightResId = stylables.getResourceId(R.styleable.RangeBarPreference_height, -1);

            // range bar displayable string entries
            int entriesResId = stylables.getResourceId(R.styleable.RangeBarPreference_entries, -1);
            if (entriesResId != -1) {
                entries = context.getResources().getStringArray(entriesResId);
            }

            // range start and endpoints
            startTick = stylables.getInteger(R.styleable.RangeBarPreference_startTick, -1);
            endTick = stylables.getInteger(R.styleable.RangeBarPreference_endTick, -1);

            // pid size
            pinRadius = stylables.getDimension(R.styleable.RangeBarPreference_pinRadius, -1.0f);

            // range start and end points names to use when saving
            startKey = stylables.getString(R.styleable.RangeBarPreference_startKey);
            endKey = stylables.getString(R.styleable.RangeBarPreference_endKey);
        }

        // initialize formatter to use entries array or fall back to entries
        formatter =
                new IRangeBarFormatter() {
                    @Override
                    public String format(String value) {
                        try {
                            int index = Integer.parseInt(value);
                            return (index >= 0 && index < entries.length) ? entries[index] : value;
                        } catch (Exception e) {
                            return value;
                        }
                    }
                };

        // obtain default preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }
}
