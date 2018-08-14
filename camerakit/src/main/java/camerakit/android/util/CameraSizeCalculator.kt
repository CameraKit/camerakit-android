package camerakit.android.util

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

}