package com.twilio.video.app.espresso

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.view.menu.ActionMenuItemView
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class DrawableMatcher(
    private val targetContext: Context,
    @param:DrawableRes private val expectedId: Int
) : TypeSafeMatcher<View>(View::class.java) {

    override fun matchesSafely(target: View): Boolean {
        val drawable: Drawable? = when (target) {
            is ActionMenuItemView -> target.itemData.icon
            is ImageView -> target.drawable
            else -> null
        }
        requireNotNull(drawable)

        val resources: Resources = target.context.resources
        val expectedDrawable: Drawable? = resources.getDrawable(expectedId, targetContext.theme)
        return expectedDrawable?.constantState?.let { it == drawable.constantState } ?: false
    }

    override fun describeTo(description: Description) {
        description.appendText("with drawable from resource id: $expectedId")
        targetContext.resources.getResourceEntryName(expectedId)?.let { description.appendText("[$it]") }
    }
}
