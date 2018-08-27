package com.camerakit.util

import com.camerakit.type.CameraSize
import kotlin.math.absoluteValue

class CameraSizeCalculator(private val sizes: Array<CameraSize>) {

    fun findClosestSizeContainingTarget(target: CameraSize): CameraSize {
        sizes.sort()

        var bestSize = sizes.last()
        var bestArea = Int.MAX_VALUE
        sizes.forEach {
            if (it.width >= target.width
                    && it.height >= target.height
                    && it.area() < bestArea) {
                bestSize = it
                bestArea = it.area()
            }
        }

        return bestSize
    }

    fun findClosestSizeMatchingArea(area: Int): CameraSize {
        sizes.sort()

        var bestSize = sizes.last()
        sizes.forEach {
            if ((area - it.area()).absoluteValue
                    < (area - bestSize.area()).absoluteValue) {
                bestSize = it
            }
        }

        return bestSize
    }

}