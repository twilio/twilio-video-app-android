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
import android.view.MenuItem
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.twilio.video.app.base.BaseActivity
import javax.inject.Inject

class SettingsActivity : BaseActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    @Inject
    internal lateinit var sharedPreferences: SharedPreferences

    /*
     * This map tracks the settings action bar title for navigations through the backstack.
     * Each time a separate settings fragment is launched, the back stack title map will be updated
     * with the expected title for the current back stack entry count.
     */
    private val backStackTitleMap = mutableMapOf<Int, CharSequence?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsFragment = SettingsFragment()

        /*
         * Initialize the back stack title map with the default settings and listen for
         * changes in the backstack and update the actionbar title.
         */
        backStackTitleMap[supportFragmentManager.backStackEntryCount] = supportActionBar?.title
        supportFragmentManager.addOnBackStackChangedListener {
            supportActionBar?.title = backStackTitleMap[supportFragmentManager.backStackEntryCount]
        }
        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, settingsFragment)
            .commit()
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

        /*
         * Add the action bar settings title for the upcoming preference fragment
         */
        backStackTitleMap[supportFragmentManager.backStackEntryCount + 1] = pref.title
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
