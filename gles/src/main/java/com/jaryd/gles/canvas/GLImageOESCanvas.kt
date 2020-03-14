package com.jaryd.gles.canvas

import android.opengl.GLES11Ext
import android.opengl.GLES30
import com.jaryd.gles.GLESHelper

class GLImageOESCanvas(vertexShader:String = VERTEX_SHADER, fragmemtShader:String = FRAGMENT_SHADER)
    : GLImageCanvas(vertexShader,fragmemtShader){
    private var mTransformMatrixHandler= GLESHelper.GL_NOT_INIT
    private var mTransformMatrix:FloatArray? = null

    init {
        mTextureType = GLES11Ext.GL_TEXTURE_EXTERNAL_OES
        mTransformMatrixHandler = GLES30.glGetUniformLocation(mProgramHandler,"transformMatrix")
    }

    fun setTextureTransformMatrix(matrix:FloatArray){
        mTransformMatrix = matrix
    }

    override fun onDrawbefore() {
        super.onDrawbefore()

        if(mTransformMatrix == null){
            mTransformMatrix = floatArrayOf(1F,0F,0F,0F,
                0F,1F,0F,0F,
                0F,0F,1F,0F,
                0F,0F,0F,1F)
        }

        GLES30.glUniformMatrix4fv(mTransformMatrixHandler,1,false,mTransformMatrix,0)
    }

    companion object{
        private val VERTEX_SHADER ="" +
                "uniform mat4 transformMatrix;\n" +
                "uniform mat4 mvpMatrix;\n" +
                "attribute vec4 aPosition; \n" +
                "attribute vec4 aTextureCoord; \n" +
                "varying vec2 textureCoordinate;\n" +
                "void main(){ \n" +
                "   gl_Position = mvpMatrix * aPosition;\n" +
                "   textureCoordinate = (transformMatrix * aTextureCoord).xy;\n" +
                "}"

        private val FRAGMENT_SHADER = "" +
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "varying vec2 textureCoordinate;\n" +
                "uniform samplerExternalOES inputTexture;\n" +
                "void main(){\n" +
                "   gl_FragColor = texture2D(inputTexture,textureCoordinate);\n" +
                "}"

    }
}