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
package com.twilio.video.app.data

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.appyvet.rangebar.IRangeBarFormatter
import com.appyvet.rangebar.RangeBar
import com.twilio.video.app.R

/**
 * RangeBarPreference allows to save any range in default shared preferences by saving range start
 * and range end values.
 */
class RangeBarPreference : Preference {
    /**
     * RangeBar pin visible text formatter. If entries array is provided utilizes string values from
     * the provided array, otherwise falls back to integers.
     */
    private var formatter: IRangeBarFormatter? = null

    /** Range of values.  */
    var entries: Array<String>? = null

    /** Range start key - used in shared preferences to save range start point.  */
    private var startKey: String? = null

    /** Range end key - used in shared preferences to save range end point.  */
    private var endKey: String? = null

    /** RangeBar provided setting. NOTE: uses this value if no entries provided.  */
    private var startTick = 0

    /** RangeBar provided setting. NOTE: uses this value if no entries provided.  */
    private var endTick = 0

    /** RangeBar provided setting. Specifies the size of tick used.  */
    private var pinRadius = 0f

    /** RangeBar height. Applies custom height from xml file.  */
    private var heightResId = 0

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        // obtain resources for range bar height calculations.
        val resources = holder.itemView.context.resources
        /** Customizable range bar.  */
        val rangeBar1 = holder.findViewById(R.id.range_bar) as RangeBar

        // apply custom height
        rangeBar1.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(heightResId))

        // setup preference range
        entries?.let {
            startTick = 0
            endTick = it.size - 1
        }
        rangeBar1.tickStart = startTick.toFloat()
        rangeBar1.tickEnd = endTick.toFloat()

        // obtain selected range
        var start = sharedPreferences.getInt(startKey, -1)
        var end = sharedPreferences.getInt(endKey, -1)

        // no values saved previously
        if (start == -1 || end == -1) {

            // obtain range from entries array size as default setting
            start = startTick
            end = endTick
        }
        rangeBar1.setRangePinsByValue(start.toFloat(), end.toFloat())

        // apply pin size
        if (pinRadius != -1.0f) {
            rangeBar1.setPinRadius(pinRadius)
        }

        // apply range bar value text formatter visible on choosing pins
        if (formatter != null) {
            rangeBar1.setFormatter(formatter)
        }

        // save all changes prefs while moving pins
        rangeBar1.setOnRangeBarChangeListener { rangeBar: RangeBar?, leftPinIndex: Int, rightPinIndex: Int, leftPinValue: String?, rightPinValue: String? ->
            sharedPreferences
                    .edit()
                    .putInt(startKey, leftPinIndex)
                    .putInt(endKey, rightPinIndex)
                    .apply()
        }
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        // apply custom layout
        layoutResource = R.layout.range_bar_preference
        if (attrs != null) {
            val stylables = context.theme
                    .obtainStyledAttributes(attrs, R.styleable.RangeBarPreference, 0, 0)

            // range bar height
            heightResId = stylables.getResourceId(R.styleable.RangeBarPreference_height, -1)

            // range start and endpoints
            startTick = stylables.getInteger(R.styleable.RangeBarPreference_startTick, -1)
            endTick = stylables.getInteger(R.styleable.RangeBarPreference_endTick, -1)

            // pid size
            pinRadius = stylables.getDimension(R.styleable.RangeBarPreference_pinRadius, -1.0f)

            // range start and end points names to use when saving
            startKey = stylables.getString(R.styleable.RangeBarPreference_startKey)
            endKey = stylables.getString(R.styleable.RangeBarPreference_endKey)
        }

        // initialize formatter to use entries array or range bar values if entries array is null
        formatter = IRangeBarFormatter { value: String ->
                val index = value.toInt()
                entries?.let { entries ->
                    if (index >= 0 && index <= entries.lastIndex) entries[index] else value
                } ?: value
            }
    }
}
