package com.makn.footballquiz.ui.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** The Activity behind a Compose [Context]; ads and billing need one to show their screens. */
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
