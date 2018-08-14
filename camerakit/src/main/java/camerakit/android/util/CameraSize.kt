package camerakit.android.util

import android.support.annotation.RequiresApi

class CameraSize(val width: Int, val height: Int) : Comparable<CameraSize> {

    companion object {

        operator fun invoke(size: android.hardware.Camera.Size): CameraSize {
            return CameraSize(size.width, size.height)
        }

        @RequiresApi(21)
        operator fun invoke(size: android.util.Size): CameraSize {
            return CameraSize(size.width, size.height)
        }

    }

    fun area(): Int {
        return width * height
    }

    fun aspectRatio(): Float {
        return width.toFloat() / height.toFloat()
    }

    fun asLandscape(): CameraSize {
        return when (width >= height) {
            true -> this
            false -> CameraSize(height, width)
        }
    }

    fun asPortrait(): CameraSize {
        return when (height >= width) {
            true -> this
            false -> CameraSize(height, width)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is CameraSize) {
            return width == other.width
                    && height == other.height
        }

        return false
    }

    override fun toString(): String {
        return "${width}x${height}"
    }

    override fun hashCode(): Int {
        return 31 * (31 + width.hashCode()) + height.hashCode()
    }

    override fun compareTo(other: CameraSize): Int {
        if (area() > other.area()) {
            return 1
        } else if (area() < other.area()) {
            return -1
        } else {
            return 0
        }
    }

}
