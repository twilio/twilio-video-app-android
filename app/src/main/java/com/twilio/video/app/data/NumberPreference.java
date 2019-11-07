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

package com.twilio.video.app.data;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.preference.DialogPreference;
import com.twilio.video.app.R;
import java.util.Locale;

/**
 * NumberPreference allows to save user input as integer value and limits input type to single one -
 * {@link android.text.InputType#TYPE_CLASS_NUMBER}.
 */
public class NumberPreference extends DialogPreference {

    /** Preference layout resource ID. */
    private static final int layoutResId = R.layout.number_preference;

    /** Preference value. */
    private int number;

    public NumberPreference(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public NumberPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public NumberPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceStyle);
    }

    public NumberPreference(Context context) {
        this(context, null);
    }

    /**
     * Obtain number saved in preferences.
     *
     * @return number value.
     */
    public int getNumber() {
        return number;
    }

    /**
     * Save number in preferences.
     *
     * @param number integer value.
     */
    public void setNumber(int number) {
        this.number = number;
        persistInt(number);

        notifyChanged();
    }

    @Override
    public int getDialogLayoutResource() {
        return layoutResId;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    // TODO Use non deprecated method and use SharedPreferences to get persisted value
    // https://issues.corp.twilio.com/browse/AHOYAPPS-111
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setNumber(restorePersistedValue ? getPersistedInt(number) : (int) defaultValue);
    }

    @Override
    public CharSequence getSummary() {
        return String.format(Locale.getDefault(), "%d", number);
    }
}
