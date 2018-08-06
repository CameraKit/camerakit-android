package com.camerakit.type

data class CameraSize(val width: Int, val height: Int) : Comparable<CameraSize> {

    fun area(): Int {
        return width * height
    }

    fun aspectRatio(): Float {
        if (width == 0 || height == 0) {
            return 1f
        }

        return width.toFloat() / height.toFloat()
    }

    override fun compareTo(other: CameraSize): Int {
        val areaDiff = width * height - other.width * other.height
        if (areaDiff > 0) {
            return 1
        } else if (areaDiff < 0) {
            return -1
        } else {
            return 0
        }
    }

}