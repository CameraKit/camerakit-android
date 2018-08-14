package camerakit.android.demo.ext

import android.content.res.Resources
import android.util.DisplayMetrics

fun Number.dpToPx() : Int {
    val displayMetrics = Resources.getSystem().displayMetrics
    return (this.toFloat() * (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
}
