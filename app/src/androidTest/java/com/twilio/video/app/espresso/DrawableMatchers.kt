package com.twilio.video.app.espresso

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
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
        return if (expectedDrawable != null) {
            makeBitmap(drawable)?.sameAs(makeBitmap(expectedDrawable)) ?: false
        } else {
            false
        }
    }

    override fun describeTo(description: Description) {
        description.appendText("with drawable from resource id: $expectedId")
        targetContext.resources.getResourceEntryName(expectedId)?.let { description.appendText("[$it]") }
    }

    private fun makeBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
