package com.sl.impulse.ui

import android.content.Context
import android.content.Intent
import com.sl.impulse.R

internal fun shareResult(context: Context, text: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.share_chooser)))
}
