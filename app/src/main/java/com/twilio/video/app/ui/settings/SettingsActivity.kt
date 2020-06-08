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

package com.twilio.video.app.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.twilio.video.app.base.BaseActivity
import com.twilio.video.app.util.replaceFragment
import javax.inject.Inject

class SettingsActivity : BaseActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    @Inject
    internal lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsFragment = SettingsFragment()

        supportFragmentManager.replaceFragment(settingsFragment, android.R.id.content)
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat?,
        pref: Preference
    ): Boolean {
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment)
        fragment.arguments = args
        fragment.setTargetFragment(caller, 0)

        supportFragmentManager.replaceFragment(fragment, android.R.id.content)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (supportFragmentManager.fragments.size == 0) finish()
    }
}
