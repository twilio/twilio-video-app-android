package com.twilio.video.app.ui.settings

import android.content.SharedPreferences
import android.view.MenuItem
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.twilio.video.app.base.BaseActivity
import com.twilio.video.app.data.NumberPreference
import com.twilio.video.app.data.NumberPreferenceDialogFragmentCompat
import com.twilio.video.app.util.get

abstract class BaseSettingsFragment : PreferenceFragmentCompat() {

    protected val sharedPreferences: SharedPreferences get() = preferenceManager.sharedPreferences

    override fun onResume() {
        super.onResume()

        (requireActivity() as BaseActivity).supportActionBar?.title = preferenceScreen.title
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (this is SettingsFragment) {
                requireActivity().finish()
            } else {
                parentFragmentManager.popBackStack()
            }
            return true
        }
        return false
    }

    /*
     * This function sets the ListPreference value based on the current or default
     * value of the preference.
     */
    protected fun setListPreferenceValue(arrayId: Int, key: String, defaultValue: String) {
        val valueIndex = resources
            .getStringArray(arrayId).indexOf(sharedPreferences.get(key, defaultValue))
        findPreference<ListPreference>(key)?.setValueIndex(valueIndex)
    }

    /*
     * This function sets the NumberPreference value based on the current or default value
     * of the preference.
     */
    protected fun setNumberPreferenceValue(key: String, defaultValue: Int) {
        findPreference<NumberPreference>(key)?.apply {
            val numberValue = sharedPreferences.get(key, defaultValue)
            summary = numberValue.toString()
            number = numberValue
        }
    }
}
