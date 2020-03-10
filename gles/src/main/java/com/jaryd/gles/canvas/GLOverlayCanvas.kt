package com.jaryd.gles.canvas

import android.opengl.GLES30
import android.opengl.Matrix
import com.jaryd.gles.GLESHelper
import com.jaryd.gles.utils.CoordinationUtils
import java.nio.ByteBuffer
import java.nio.FloatBuffer

/**
 * 水印
 */

class GLOverlayCanvas(val buffer:ByteBuffer, val srcWidth:Int, val srcHeight:Int)
    : GLImageCanvas() {


    private val matrix = FloatArray(16)

    private fun domatrix(){
        Matrix.setIdentityM(matrix,0)
        Matrix.rotateM(matrix,0,180f,0f,0f,1f)
    }



    override fun onDrawAfter() {
        GLES30.glViewport(0,0,srcWidth,srcHeight)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA,GLES30.GL_ONE_MINUS_SRC_ALPHA)
        domatrix()
        val old_matrix = mMvpMatrix
        mMvpMatrix = matrix
        onDrawTexture(buffer,srcWidth,srcHeight)
        mMvpMatrix = old_matrix

    }
}