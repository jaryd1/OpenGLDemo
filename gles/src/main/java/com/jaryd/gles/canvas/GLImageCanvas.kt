package com.jaryd.gles.canvas

import android.opengl.GLES30
import android.opengl.Matrix
import android.text.TextUtils
import android.util.Log
import com.jaryd.gles.GLESHelper
import com.jaryd.gles.utils.CoordinationUtils
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.util.*

open class GLImageCanvas(
    protected  var mVertexShader:String?= VERTEX_SHADE,
    protected var mFragmentShader:String?= FRAGMENT_SHADE
){
    companion object {

        internal val VERTEX_SHADE = "" +
                "uniform mat4 mvpMatrix; \n" +
                "attribute vec4 aPosition; \n" +
                "attribute vec4 aTextureCoord; \n" +
                "varying vec2 textureCoordinate;\n" +
                "void main(){ \n" +
                "   gl_Position = mvpMatrix * aPosition;\n" +
                "   textureCoordinate = aTextureCoord.xy;\n" +
                "}"
        internal val FRAGMENT_SHADE = "" +
                "precision mediump float;\n" +
                "varying vec2 textureCoordinate;\n" +
                "uniform sampler2D inputTexture;\n" +
                "void main(){\n" +
                "   gl_FragColor = texture2D(inputTexture,textureCoordinate);\n" +
                "}"
    }

    protected val mCoordPerVertex = 2
    protected val mVertexCount = 4

    protected var mProgramHandler = 0
    protected var mPositionHandler = 0
    protected var mTextureCoordinateHandler = 0
    protected var mInputTextureHandler = -1
    protected var mMvpMatrixHandler = -1

    protected var mVBOHandler = GLESHelper.GL_NOT_INIT
    protected var mVAOHandler = GLESHelper.GL_NOT_INIT
    protected var mEBOHandler = GLESHelper.GL_NOT_INIT


    protected var mDisplayWidth = 0
    protected var mDisplayHeight = 0

    protected var mFrameWidth = -1
    protected var mFrameHeight = -1

    protected var mFrameBuffers: IntArray? = null
    protected var mFrameBufferTextures: IntArray? = null


    protected var mTextureType = GLES30.GL_TEXTURE_2D

    protected var isInitialized  = false

    protected var mTextureId = GLESHelper.GL_NOT_INIT

    protected val mParameterTasks: LinkedList<Runnable> by lazy { LinkedList<Runnable>() }

    protected var mMvpMatrix = FloatArray(16)

    init {
        initHanders()
        Matrix.setIdentityM(mMvpMatrix,0)
        initVAO()
    }

    open  fun initHanders() {
        if((!TextUtils.isEmpty(mVertexShader)) and !(TextUtils.isEmpty(mFragmentShader))) {
            mProgramHandler = GLESHelper.creatProgram(
                mVertexShader!!,
                mFragmentShader!!
            )

            mPositionHandler = GLES30.glGetAttribLocation(mProgramHandler, "aPosition")
            mTextureCoordinateHandler = GLES30.glGetAttribLocation(mProgramHandler, "aTextureCoord")
            mInputTextureHandler = GLES30.glGetUniformLocation(mProgramHandler, "inputTexture")
            mMvpMatrixHandler = GLES30.glGetUniformLocation(mProgramHandler,"mvpMatrix")

            isInitialized = true

        }else {
            mProgramHandler = GLESHelper.GL_NOT_INIT //no init
            mTextureCoordinateHandler = GLESHelper.GL_NOT_INIT
            mInputTextureHandler = GLESHelper.GL_NOT_INIT
            isInitialized = false
        }
    }


    open fun onDisplaySizeChanged(width: Int,height: Int){
        mDisplayWidth = width
        mDisplayHeight = height
    }

    open fun initFrameBuffer(width: Int,height: Int){
        if(!isInitialized) return
        if(mFrameBuffers != null && (mFrameWidth != width || mFrameHeight != height)) {
            destroyFrameBuffer()
        }
        if(mFrameBuffers == null) {
            mFrameWidth = width
            mFrameHeight = height
            mFrameBuffers = IntArray(1)
            mFrameBufferTextures = IntArray(1)
            GLESHelper.createFrameBuffer(
                mFrameBuffers!!,
                mFrameBufferTextures!!,
                mFrameWidth,
                mFrameHeight
            )
        }
    }

    protected fun initVAO(){
        val vao_array = intArrayOf(mVAOHandler)
        GLES30.glGenVertexArrays(1, vao_array,0)
        mVAOHandler = vao_array[0]
        GLESHelper.checkGlError("gen vao $mVAOHandler")
        GLES30.glBindVertexArray(mVAOHandler)
        GLESHelper.checkGlError("bind vao $mVAOHandler")

        val buffers = intArrayOf(mVBOHandler,mEBOHandler)
        GLES30.glGenBuffers(2, buffers,0)
        mVBOHandler = buffers[0]
        mEBOHandler = buffers[1]

        val vbo_buffer = GLESHelper.creatFloatBuffer(CoordinationUtils.mVBOArray)
        vbo_buffer.position(0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,mVBOHandler)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,vbo_buffer.capacity()* java.lang.Float.SIZE/8,vbo_buffer,GLES30.GL_STATIC_DRAW)

        GLESHelper.checkGlError("bind buffer")

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER,mEBOHandler)
        val ebo_buffer = GLESHelper.creatIntBuffer(intArrayOf(0,1,2,3,2,1))
        ebo_buffer.position(0)
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER,ebo_buffer.capacity()* java.lang.Integer.SIZE/8,ebo_buffer,GLES30.GL_STATIC_DRAW)

        GLESHelper.checkGlError("bind element")

        GLES30.glEnableVertexAttribArray(mPositionHandler)
        GLES30.glVertexAttribPointer(mPositionHandler,mCoordPerVertex,GLES30.GL_FLOAT,false,
            2*mCoordPerVertex* java.lang.Float.SIZE/8,0)

        GLES30.glEnableVertexAttribArray(mTextureCoordinateHandler)
        GLES30.glVertexAttribPointer(mTextureCoordinateHandler,mCoordPerVertex,GLES30.GL_FLOAT,false,
            2*mCoordPerVertex* java.lang.Float.SIZE/8,mCoordPerVertex*java.lang.Float.SIZE/8)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0)
        GLES30.glBindVertexArray(0)

    }

    open fun destroyFrameBuffer(){
        mFrameBufferTextures?.let {
            GLES30.glDeleteFramebuffers(it.size,it,0)
            mFrameBufferTextures = null
        }
        mFrameBuffers?.let {
            GLES30.glDeleteFramebuffers(it.size,it,0)
            mFrameBuffers = null
        }
        mFrameHeight = -1
        mFrameWidth = -1
    }

    open fun release(){
        destroyFrameBuffer()
        if(isInitialized){
            GLES30.glDeleteProgram(mProgramHandler)
            mProgramHandler = GLESHelper.GL_NOT_INIT
        }
    }

    open fun getMvpMatrix()=mMvpMatrix


    open fun drawFrame(textureId:Int){
        if((textureId == GLESHelper.GL_NOT_INIT) || !isInitialized) return
        GLESHelper.checkGlError("before viewport")
        GLES30.glViewport(0,0,mDisplayWidth,mDisplayHeight)
        GLESHelper.checkGlError("viewport")
        GLES30.glClearColor(0f,0f,1f,1f)
        GLES30.glClear((GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT))
        GLESHelper.checkGlError("clear")
        GLES30.glUseProgram(mProgramHandler)
        GLESHelper.checkGlError("use program")
        onDrawbefore()
        runTasks()
        GLESHelper.checkGlError("draw before $mProgramHandler")
        onDrawTexture(textureId)
        onDrawAfter()
        GLES30.glUseProgram(0)

    }

    open fun drawFrame(data:ByteBuffer,width: Int,height: Int){
        if( !isInitialized) return
        GLES30.glViewport(0,0,mDisplayWidth,mDisplayHeight)
        GLES30.glClearColor(0f,0f,1f,1f)
        GLES30.glClear((GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT))
        GLES30.glUseProgram(mProgramHandler)

        onDrawbefore()
        runTasks()
        onDrawTexture(data,width,height)
        onDrawAfter()

        GLES30.glUseProgram(0)
    }

    open fun drawFrameBuffer(textureId: Int):Int{
        if((mFrameBufferTextures == null) or (mFrameBuffers == null) or (textureId == GLESHelper.GL_NOT_INIT)
            or !isInitialized){
            return textureId
        }
        GLES30.glViewport(0,0,mFrameWidth,mFrameHeight)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,mFrameBuffers!![0])
        GLES30.glUseProgram(mProgramHandler)

        onDrawbefore()
        runTasks()
        onDrawTexture(textureId)
        onDrawAfter()
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0)
        GLES30.glUseProgram(0)
        return mFrameBufferTextures!![0]
    }

    open fun drawFrameBuffer(data: ByteBuffer,width: Int,height: Int):Int{
        if((mFrameBufferTextures == null) or (mFrameBuffers == null)
            or !isInitialized){
            return GLESHelper.GL_NOT_INIT
        }
        GLES30.glViewport(0,0,mDisplayWidth,mDisplayHeight)
        GLES30.glClearColor(1f,0f,0f,1f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,mFrameBuffers!![0])
        GLES30.glUseProgram(mProgramHandler)

        onDrawbefore()
        runTasks()
        GLESHelper.checkGlError("draw before")
        onDrawTexture(data,width,height)
        GLESHelper.checkGlError("draw after")
        onDrawAfter()

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0)
        GLES30.glUseProgram(0)
        GLESHelper.checkGlError("draw frame buffer")
        return mFrameBufferTextures!![0]
    }

    private fun onDrawTexture(textureId: Int) {

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLESHelper.checkGlError("activtive texture")
        GLES30.glBindTexture(mTextureType,textureId)
        GLESHelper.checkGlError("bind texture")
        GLES30.glUniform1i(mInputTextureHandler,0)
        GLESHelper.checkGlError("bind texture handle ${mInputTextureHandler}")

        GLES30.glUniformMatrix4fv(mMvpMatrixHandler,1,false,getMvpMatrix(),0)
//
        GLES30.glBindVertexArray(mVAOHandler)
        GLESHelper.checkGlError("bind vertex array")
          GLES30.glDrawElements(GLES30.GL_TRIANGLES,6,GLES30.GL_UNSIGNED_INT,0)
        GLESHelper.checkGlError("draw")
        GLES30.glBindVertexArray(0)

        GLES30.glBindTexture(mTextureType,0)
        GLESHelper.checkGlError("unbind")
    }

    protected fun onDrawTexture(data: ByteBuffer, width: Int,height: Int) {

        GLES30.glUniformMatrix4fv(mMvpMatrixHandler,1,false,getMvpMatrix(),0)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLESHelper.checkGlError("active texture")
        if(mTextureId == GLESHelper.GL_NOT_INIT){
            mTextureId = GLESHelper.creatTextureID(mTextureType)
            GLES30.glBindTexture(mTextureType,mTextureId)
            GLESHelper.checkGlError("bind texture")
            data.position(0)
            GLES30.glTexImage2D(mTextureType,0,GLES30.GL_RGBA,width,height,0,GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,data)
            GLESHelper.checkGlError("glTexImage")
        }else{
            GLES30.glBindTexture(mTextureType,mTextureId)
            GLES30.glTexSubImage2D(mTextureType,0,0,0,width,height,GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,data)
        }

        GLES30.glUniform1i(mInputTextureHandler,0)
        GLESHelper.checkGlError("uniform input texture $mInputTextureHandler")

        GLES30.glBindVertexArray(mVAOHandler)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES,6,GLES30.GL_UNSIGNED_INT,0)
        GLES30.glBindVertexArray(0)

        GLES30.glBindTexture(mTextureType,0)
        GLESHelper.checkGlError("unbind")
    }

    protected fun addTask(task:Runnable){
        synchronized(mParameterTasks){
            mParameterTasks.addLast(task)
        }
    }

    protected fun runTasks(){
        while(!mParameterTasks.isEmpty()){
            mParameterTasks.removeFirst().run()
        }
    }

    protected fun setFloatValue(location:Int,value:Float){
        addTask(Runnable {
            GLES30.glUniform1f(location,value)
        })
    }

    protected fun setFloatVec2(location: Int,vec2:FloatArray){
        addTask(Runnable {
            GLES30.glUniform2fv(location,1,vec2,0)
        })
    }

    protected fun setFloatVec3(location: Int,vec3:FloatArray){
        addTask(Runnable {
            GLES30.glUniform3fv(location,1,vec3,0)
        })
    }

    open protected fun onDrawbefore()=Unit

    open protected fun onDrawAfter()=Unit
}