package com.twilio.video.app.util

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import kotlin.reflect.KClass

fun FragmentManager.replaceFragment(
    fragment: Fragment,
    @IdRes fragmentContainer: Int
) {

    commit {
        addToBackStack(null)
        replace(fragmentContainer, findFragment(fragment::class) ?: fragment)
    }
}

fun FragmentManager.findFragment(fragment: KClass<out Fragment>): Fragment? =
        fragments.find { it::class == fragment }
