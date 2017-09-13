package com.twilio.video.app.data;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import com.twilio.video.app.R;

import java.util.Locale;

/**
 * NumberPreference allows to save user input as integer value and limits input type
 * to single one - {@link android.text.InputType#TYPE_CLASS_NUMBER}.
 */
public class NumberPreference extends DialogPreference {

    /**
     * Preference layout resource ID.
     */
    private static final int layoutResId = R.layout.number_preference;

    /**
     * Preference value.
     */
    private int number;

    public NumberPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setNumber(restorePersistedValue ? getPersistedInt(number) : (int) defaultValue);
    }

    @Override
    public CharSequence getSummary() {
        return String.format(Locale.getDefault(), "%d", number);
    }
}
