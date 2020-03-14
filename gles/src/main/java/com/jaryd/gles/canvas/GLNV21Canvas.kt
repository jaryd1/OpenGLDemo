package com.jaryd.gles.canvas

import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import com.jaryd.gles.GLESHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GLNV21Canvas():GLImageCanvas(mFragmentShader = FRAGMENT_SHADER) {

    private var mUVTextureHandler = GLESHelper.GL_NOT_INIT
    private var mUVTexture = GLESHelper.GL_NOT_INIT
    private val matrix = FloatArray(16)

    init {
        mUVTextureHandler = GLES30.glGetUniformLocation(mProgramHandler,"uvTexture")
    }


    override fun getMvpMatrix(): FloatArray {
        Matrix.setIdentityM(matrix,0)
        Matrix.rotateM(matrix,0,90f,0f,0f,1f)
        return matrix
    }

    fun drawFrame(data:ByteArray, width:Int, height:Int){
        if( !isInitialized) return
        GLES30.glViewport(0,0,mDisplayWidth,mDisplayHeight)
        GLES30.glClearColor(0f,0f,1f,1f)
        GLES30.glClear((GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT))
        GLES30.glUseProgram(mProgramHandler)

        if(mUVTexture == GLESHelper.GL_NOT_INIT){
            mUVTexture = GLESHelper.creatYUVTextureID(width/2,height/2,true)
        }

        if(mTextureId == GLESHelper.GL_NOT_INIT){
            mTextureId = GLESHelper.creatYUVTextureID(width,height,false)
        }
        GLES30.glUniformMatrix4fv(mMvpMatrixHandler,1,false,getMvpMatrix(),0)

        val y_buffer = ByteBuffer.allocateDirect(width*height).put(data,0,width*height)
        val uv_buffer = ByteBuffer.allocateDirect(width*height/2).put(data,width*height,width*height/2)
        y_buffer.position(0)
        uv_buffer.position(0)


        // y plane
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,mTextureId)
        GLES30.glTexSubImage2D(GLES30.GL_TEXTURE_2D,0,0,0,width,height,
        GLES30.GL_LUMINANCE,GLES30.GL_UNSIGNED_BYTE,y_buffer)
        GLES30.glUniform1i(mInputTextureHandler,0)

        // uv plane
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,mUVTexture)
        GLES30.glTexSubImage2D(GLES30.GL_TEXTURE_2D,0,0,0,
            width/2,height/2,GLES30.GL_LUMINANCE_ALPHA,GLES30.GL_UNSIGNED_BYTE,
            uv_buffer)
        GLES30.glUniform1i(mUVTextureHandler,1)

        GLES30.glBindVertexArray(mVAOHandler)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES,6,GLES30.GL_UNSIGNED_INT,0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,0)
        GLES30.glUseProgram(0)
    }

    companion object{

        private val FRAGMENT_SHADER = "" +
                "precision mediump float;\n" +
                "varying vec2 textureCoordinate;\n" +
                "uniform sampler2D inputTexture;\n" +
                "uniform sampler2D uvTexture;\n" +
                "void main(){\n" +
                "vec3 yuv;\n" +
                "vec3 rgb;\n" +
                "yuv.r=texture2D(inputTexture,textureCoordinate).r;\n" +
                "yuv.g=texture2D(uvTexture,textureCoordinate).a-0.5;\n" +
                "yuv.b=texture2D(uvTexture,textureCoordinate).r-0.5;\n" +
                "rgb = mat3(1.0,1.0,1.0,0.0,-0.39465,2.03211,1.13983,-0.58060,0.0)* yuv;" +
                "gl_FragColor = vec4(rgb,1.0);\n" +
                "}"
    }
}