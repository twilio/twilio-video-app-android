package com.twilio.video.app.util

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

fun FragmentManager.replaceFragment(@IdRes fragmentContainer: Int, fragment: Fragment) {
    this
        .beginTransaction()
        .replace(fragmentContainer, fragment)
        .addToBackStack(null)
        .commit()
}