package com.jaryd.gles.canvas

import android.opengl.Matrix

/**
 * 镜像、旋转等普通滤镜
 *
 */

class GLCommonFilterCanvas:GLImageCanvas() {

    private val matrix = FloatArray(16)

    override fun onDrawbefore() {
        super.onDrawbefore()
    }

    fun setHorizonFlip(){
        Matrix.setIdentityM(matrix,0)
        matrix[0] = -1f
    }

    fun setVerticalFlip(){
        Matrix.setIdentityM(matrix,0)
        matrix[5] = -1f
    }

    fun rotate(degree:Int){
        Matrix.setIdentityM(matrix,0)
        Matrix.rotateM(matrix,0,degree.toFloat(),0f,0f,1f)
    }



    override fun getMvpMatrix() = matrix
}