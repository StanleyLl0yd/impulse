package com.sl.impulse.ui

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.sl.impulse.R

private const val SPLASH_FADE_DURATION_MILLIS = 3_000

@Composable
fun ImpulseRoot() {
    var splashVisible by remember { mutableStateOf(true) }

    if (splashVisible) {
        ImpulseSplash(onFinished = { splashVisible = false })
    } else {
        ImpulseApp()
    }
}

@Composable
private fun ImpulseSplash(onFinished: () -> Unit) {
    val imageAlpha = remember { Animatable(0f) }
    val resources = LocalContext.current.resources
    val splashBitmap = remember(resources) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(resources, R.drawable.impulse_splash),
            ).asImageBitmap()
        } else {
            requireNotNull(
                BitmapFactory.decodeResource(resources, R.drawable.impulse_splash),
            ).asImageBitmap()
        }
    }

    LaunchedEffect(Unit) {
        imageAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = SPLASH_FADE_DURATION_MILLIS,
                easing = LinearEasing,
            ),
        )
        withFrameNanos { }
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("splash-screen"),
    ) {
        Image(
            bitmap = splashBitmap,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .alpha(imageAlpha.value)
                .testTag("splash-image"),
        )
    }
}
