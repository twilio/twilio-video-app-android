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

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import com.twilio.video.app.R;
import java.util.Locale;

/**
 * NumberPreferenceDialogFragmentCompat allows to instantiate custom {@link NumberPreference} dialog
 * and modify preference values.
 */
public class NumberPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    /** Input field. */
    private EditText numberInput;

    /**
     * Creates new instance of {@link NumberPreferenceDialogFragmentCompat}.
     *
     * @param key preference key.
     * @return new instance.
     */
    public static NumberPreferenceDialogFragmentCompat newInstance(String key) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_KEY, key);

        NumberPreferenceDialogFragmentCompat fragment = new NumberPreferenceDialogFragmentCompat();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        // init input field
        numberInput = view.findViewById(R.id.edit);

        // obtain reference to preference
        DialogPreference preference = getPreference();

        // setup saved preference value or fallback to default
        int value = 0;
        if (preference instanceof NumberPreference) {
            value = ((NumberPreference) preference).getNumber();
        }

        // forward value to preference view
        final String numberValue = String.format(Locale.getDefault(), "%d", value);
        numberInput.setText(numberValue);
        numberInput.setSelection(numberValue.length());
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {

            // obtain string version of number
            final String numberString = numberInput.getText().toString();

            try {

                // convert string to number value
                final int newValue = Integer.parseInt(numberString);

                // obtain reference to preference
                DialogPreference preference = getPreference();

                // save number in preferences
                if (preference instanceof NumberPreference) {
                    NumberPreference numberPreference = (NumberPreference) preference;

                    if (numberPreference.callChangeListener(newValue)) {
                        numberPreference.setNumber(newValue);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
