package com.camerakit.surface.opengl

import android.opengl.GLES20.glGenFramebuffers
import android.opengl.GLES20.glGenTextures
import kotlin.reflect.KMutableProperty0

class GLUtil {

    companion object {
        fun genTexture(dstProperty: KMutableProperty0<Int>): Boolean {
            val textures = IntArray(1)
            glGenTextures(1, textures, 0)
            if (textures[0] == 0) {
                return false
            }

            dstProperty.set(textures[0])
            return true
        }

        fun genFramebuffer(dstProperty: KMutableProperty0<Int>): Boolean {
            val framebuffers = IntArray(1)
            glGenFramebuffers(1, framebuffers, 0)
            if (framebuffers[0] == 0) {
                return false
            }

            dstProperty.set(framebuffers[0])
            return true
        }
    }

}