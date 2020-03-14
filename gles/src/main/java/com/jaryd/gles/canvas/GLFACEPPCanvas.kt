package com.jaryd.gles.canvas

import android.graphics.PointF
import android.graphics.Rect
import android.opengl.GLES30
import com.jaryd.gles.GLESHelper

/**
 * face++ 人脸检测 绘制关键点
 */

class GLFACEPPCanvas:GLImageCanvas(VERTEX_SHADER,FRAGMENT_SHADER) {
    var rect:Rect? = null //脸范围
    var points:Array<out PointF>? =null //106关键点
    var needReDraw = false
    var MaxWidth = 1  // 图像大小
    var MaxHeight = 1 //

    val rectBuffer = GLESHelper.creatFloatBuffer(floatArrayOf(0.5f,0.5f,-0.5f,0.5f,-0.5f,-0.5f,0.5f,-0.5f))
    val pointBuffer = GLESHelper.creatFloatBuffer(floatArrayOf(1.0f,0.0f))
    var drawlineHandler = GLESHelper.GL_NOT_INIT
    var colorHandler = GLESHelper.GL_NOT_INIT
    var color = floatArrayOf(1f,0f,0f)

    init {
        drawlineHandler = GLES30.glGetUniformLocation(mProgramHandler,"drawLine")
        colorHandler = GLES30.glGetUniformLocation(mProgramHandler,"color")
    }


    override fun onDrawbefore() {
        super.onDrawbefore()
        if(drawlineHandler !=GLESHelper.GL_NOT_INIT)
            GLES30.glUniform1i(drawlineHandler,0)
    }


    override fun onDrawAfter() {
        super.onDrawAfter()
        if(rect == null && points== null ||  !needReDraw)
            return
        rect?.apply {
            GLES30.glViewport(0,0,mDisplayWidth,mDisplayHeight)

            rectBuffer.clear()
            rectBuffer.put(this.getRect())
            rectBuffer.position(0)
            GLES30.glEnableVertexAttribArray(mPositionHandler)
            GLES30.glVertexAttribPointer(mPositionHandler,2,GLES30.GL_FLOAT,false,0,rectBuffer)
            GLES30.glLineWidth(4f)
            GLES30.glUniform1i(drawlineHandler,1)
            GLES30.glUniform3fv(colorHandler,1,color,0)
            GLES30.glDrawArrays(GLES30.GL_LINE_LOOP,0,4)
            GLES30.glDisableVertexAttribArray(mPositionHandler)
            needReDraw = false
        }

        points?.apply {
            processPoints()
            GLES30.glViewport(0,0,mDisplayWidth,mDisplayHeight)
            GLES30.glEnableVertexAttribArray(mPositionHandler)
            this.forEach {
                pointBuffer.clear()
                pointBuffer.put(floatArrayOf(2*it.x/mDisplayWidth-1,1f-2*it.y/mDisplayHeight))
                pointBuffer.position(0)
                GLES30.glVertexAttribPointer(mPositionHandler,2,GLES30.GL_FLOAT,false,0,pointBuffer)
                GLES30.glDrawArrays(GLES30.GL_POINTS,0,1)
            }
            GLES30.glDisableVertexAttribArray(mPositionHandler)
        }



    }

    /**
     * 原图片大小可能与现显示大小不一样，需按比例处理后再计算标准坐标
     */
    private fun Rect.getRect(): FloatArray {
        this.left = this.left*mDisplayWidth/MaxWidth
        this.right = this.right*mDisplayWidth/MaxWidth
        this.top = this.top*mDisplayHeight/MaxHeight
        this.bottom = this.bottom*mDisplayHeight/MaxHeight
        val xoffset = 2.0f * this.left / mDisplayWidth - 1.0f
        val yoffset = 1.0f - 2.0f * this.top / mDisplayHeight
        val x_length = 2.0f * (this.right - this.left) / mDisplayWidth
        val y_length = -2.0f * (this.bottom - this.top) / mDisplayHeight
        val points = floatArrayOf(
            xoffset, yoffset, xoffset + x_length, yoffset,
            xoffset + x_length, yoffset + y_length, xoffset, yoffset + y_length
        )
        return points
    }

    private fun processPoints(){
        points?.apply {
            this.forEach {
                val temp = it.y
                it.y = MaxHeight-it.x
                it.x = MaxWidth-temp
                it.y = it.y*mDisplayHeight/MaxHeight
                it.x = it.x*mDisplayWidth/MaxWidth
            }
        }
    }

    companion object{
        private val VERTEX_SHADER ="" +
                "uniform mat4 mvpMatrix; \n" +
                "attribute vec4 aPosition; \n" +
                "attribute vec4 aTextureCoord; \n" +
                "varying vec2 textureCoordinate;\n" +
                "void main(){ \n" +
                "   gl_Position = mvpMatrix * aPosition;\n" +
                "   textureCoordinate = aTextureCoord.xy;\n" +
                "   gl_PointSize =8.0;\n" +
                "}"

        private val FRAGMENT_SHADER = "" +
                "precision mediump float;\n" +
                "varying vec2 textureCoordinate;\n" +
                "uniform sampler2D inputTexture;\n" +
                "uniform int drawLine;\n" +
                "uniform vec3 color;\n" +
                "void main(){\n" +
            "   if(drawLine == 0)\n" +
                "   gl_FragColor = texture2D(inputTexture,textureCoordinate);\n" +
                "else\n" +
                "   gl_FragColor = vec4(color,1.0);" +
                "}"
    }
}