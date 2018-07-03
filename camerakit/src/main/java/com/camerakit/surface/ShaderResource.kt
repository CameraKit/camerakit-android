package com.camerakit.surface

import android.content.Context
import android.support.annotation.RawRes

internal class ShaderResource(context: Context, @RawRes val resId: Int) {

    private val shaderString: String

    init {
        val inputStream = context.resources.openRawResource(resId)
        shaderString = inputStream.bufferedReader().use { it.readText() }
    }

    override fun toString(): String {
        return shaderString
    }

}