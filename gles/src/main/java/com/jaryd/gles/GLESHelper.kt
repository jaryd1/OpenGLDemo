package com.jaryd.gles

import android.opengl.GLES30
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

object GLESHelper {


        internal val COORD_VERTEX = "position"
        internal val COORD_TEXTURE = "texturecoord"
        internal val MATRIX_MVP = "mvpmatrix"
        internal val MATRIX_TEXTURE = "texturematrix"
        internal val SAMPLER="sampler"
        internal val GL_NOT_INIT = -1
        internal val IDENTITY_MATRIX = FloatArray(16)
        init {
            Matrix.setIdentityM(IDENTITY_MATRIX,0)
        }

    fun createFrameBuffer(buffers:IntArray,textures:IntArray,width:Int,height:Int){
        GLES30.glGenFramebuffers(buffers.size,buffers,0)
        GLES30.glGenTextures(textures.size,textures,0)
        for(i in 0 until buffers.size){

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,textures[i])
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,0, GLES30.GL_RGBA,width,height,0,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE,null)
            GLES30.glTexParameteri(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR
            )
            GLES30.glTexParameteri(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR
            )
            GLES30.glTexParameteri(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT
            )
            GLES30.glTexParameteri(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT
            )
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,buffers[i])
            GLES30.glFramebufferTexture2D(
                GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D,
                textures[i],0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,0)
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0)

        }
    }

    fun bindTexture(handler:Int,textureId:Int,index:Int){
        if(index >15){
            throw RuntimeException("texture too much")
        }
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0+index)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,textureId)
        GLES30.glUniform1i(handler,index)
    }

    fun creatTextureID(type:Int):Int{
        val texture = IntArray(1)
        GLES30.glGenTextures(1,texture,0)
        checkGlError("glGenTextures")
        GLES30.glBindTexture(type,texture[0])
        checkGlError("glBindTexture")
        GLES30.glTexParameteri(
            type, GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_LINEAR
        )
        GLES30.glTexParameteri(
            type, GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_LINEAR
        )
        GLES30.glTexParameteri(
            type, GLES30.GL_TEXTURE_WRAP_S,
            GLES30.GL_CLAMP_TO_EDGE
        )
        GLES30.glTexParameteri(
            type, GLES30.GL_TEXTURE_WRAP_T,
            GLES30.GL_CLAMP_TO_EDGE
        )
        checkGlError("glTexParameter")
        GLES30.glBindTexture(type,0)
        return texture[0]
    }

    fun creatProgram(vertexShade:String,fragmentShade:String):Int{
        val v_shade = compileShade(GLES30.GL_VERTEX_SHADER,vertexShade)
        if(v_shade == 0){
            throw java.lang.RuntimeException("create v_shader failed")
        }
        val f_shade = compileShade(GLES30.GL_FRAGMENT_SHADER,fragmentShade)
        if(v_shade == 0){
            throw java.lang.RuntimeException("create v_shader failed")
        }
        if(f_shade == 0) return 0
        val program = GLES30.glCreateProgram()
        checkGlError("glCreateProgram")
        if(program == 0){
            throw RuntimeException("creat program failed")
        }
        GLES30.glAttachShader(program,v_shade)
        checkGlError("glAttachShader")
        GLES30.glAttachShader(program,f_shade)
        checkGlError("glAttachShader")
        GLES30.glLinkProgram(program)
        val status = IntArray(1)
        GLES30.glGetProgramiv(program,GLES30.GL_LINK_STATUS,status,0)
        if(status[0] != GLES30.GL_TRUE){
            throw RuntimeException("can't link program：${GLES30.glGetProgramInfoLog(program)}")
        }
        GLES30.glDetachShader(program,v_shade)
        GLES30.glDetachShader(program,f_shade)
        GLES30.glDeleteShader(v_shade)
        GLES30.glDeleteShader(f_shade)
        return program
    }

    fun compileShade(type:Int,source:String):Int{
        val shader= GLES30.glCreateShader(type)
        checkGlError("createshader")
        GLES30.glShaderSource(shader,source)
        GLES30.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES30.glGetShaderiv(shader,GLES30.GL_COMPILE_STATUS,compiled,0)
        if(compiled[0] == 0){
            throw RuntimeException("compiled faild：${GLES30.glGetShaderInfoLog(shader)}")
        }
        return shader
    }

    fun creatFloatBuffer(data:FloatArray): FloatBuffer {
        val buffer = ByteBuffer.allocateDirect(data.size*4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(data)
        buffer.position(0)
        return buffer
    }

    fun creatIntBuffer(data:IntArray): IntBuffer {
        val buffer = ByteBuffer.allocateDirect(data.size*4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
            .put(data)
        buffer.position(0)
        return buffer
    }




    fun checkGlError(op:String){
        val error = GLES30.glGetError()
        if(error != GLES30.GL_NO_ERROR){
            throw RuntimeException("$op error：0x${error.toString(16)}")
        }
    }
}