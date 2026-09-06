package com.sl.impulse

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LauncherAssetTest {
    @Test
    fun launcherArtworkDecodesAndAdaptiveIconRenders() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_foreground)

        assertNotNull(bitmap)
        assertEquals(432, bitmap!!.width)
        assertEquals(432, bitmap.height)

        val icon = context.packageManager.getApplicationIcon(context.packageName)
        val rendered = Bitmap.createBitmap(108, 108, Bitmap.Config.ARGB_8888)
        icon.setBounds(0, 0, rendered.width, rendered.height)
        icon.draw(Canvas(rendered))
    }
}
