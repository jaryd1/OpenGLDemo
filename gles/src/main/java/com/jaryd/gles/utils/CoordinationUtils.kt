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
//        val sx = -0.5f
//        val sy = -0.5f
//        val ex = 0.5f
//        val ey =0.5f
//
//
//        internal val mVBOArray = floatArrayOf(
//            sx,sy,0.0f,0.0f,
//            ex,sy,1.0f,0.0f,
//            sx,ey,0.0f,1.0f,
//            ex,ey,1.0f,1.0f
//        )
    }
}