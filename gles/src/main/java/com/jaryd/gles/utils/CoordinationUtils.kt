package com.jaryd.gles.utils

import android.opengl.Matrix

class CoordinationUtils {

    companion object {


        internal val mVertexsCoor = floatArrayOf(
            -1.0f,-1.0f,
            1.0f,-1.0f,
            -1.0f,1.0f,
            1.0f,1.0f
        )

        internal val mTexturesCoor = floatArrayOf(
            0.0f,0.0f,
            1.0f,0.0f,
            0.0f,1.0f,
            1.0f,1.0f
        )

        internal val mInvertTexturesCoor = floatArrayOf(
            0.0f,1.0f,
            1.0f,1.0f,
            0.0f,0.0f,
            1.0f,0.0f
        )

        internal val mVBOArray = floatArrayOf(
            -1.0f,-1.0f,0.0f,0.0f,
            1.0f,-1.0f,1.0f,0.0f,
            -1.0f,1.0f,0.0f,1.0f,
            1.0f,1.0f,1.0f,1.0f
        )
    }
}