package com.camerakit.util

import android.content.Context
import android.support.annotation.RawRes

class RawResReader(context: Context, @RawRes resId: Int) {

    val value: String

    init {
        val inputStream = context.resources.openRawResource(resId)
        value = inputStream.bufferedReader().use { it.readText() }
    }

}
