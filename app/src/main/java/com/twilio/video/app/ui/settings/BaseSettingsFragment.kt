package com.twilio.video.app.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.twilio.video.app.data.NumberPreference
import com.twilio.video.app.data.NumberPreferenceDialogFragmentCompat
import com.twilio.video.app.util.get
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

abstract class BaseSettingsFragment : PreferenceFragmentCompat() {

    @Inject
    internal lateinit var sharedPreferences: SharedPreferences

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        if (preference == null) {
            return
        }

        // show custom dialog preference
        if (preference is NumberPreference) {
            NumberPreferenceDialogFragmentCompat.newInstance(preference.key)?.let { dialog ->
                dialog.setTargetFragment(this, 0)
                dialog.show(requireFragmentManager(), tag)
            }
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    /*
     * This function sets the ListPreference value based on the current or default
     * value of the preference.
     */
    protected fun setListPreferenceValue(arrayId: Int, key: String, defaultValue: String) {
        val valueIndex = resources
            .getStringArray(arrayId).indexOf(sharedPreferences.get(key, defaultValue))
        (findPreference(key) as ListPreference).setValueIndex(valueIndex)
    }

    /*
     * This function sets the NumberPreference value based on the current or default value
     * of the preference.
     */
    protected fun setNumberPreferenceValue(key: String, defaultValue: Int) {
        (findPreference(key) as NumberPreference).apply {
            val numberValue = sharedPreferences.get(key, defaultValue)
            summary = numberValue.toString()
            number = numberValue
        }
    }
}