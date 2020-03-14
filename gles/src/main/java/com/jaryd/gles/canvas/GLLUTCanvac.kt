package com.jaryd.gles.canvas

import android.opengl.GLES30
import com.jaryd.gles.GLESHelper
import java.nio.ByteBuffer

class GLLUTCanvac(val lutBuffer:ByteBuffer,val srcWidth:Int,val srcHeight:Int)
        :GLImageCanvas(mFragmentShader = FRAGMENT_SHADER) {

    private var LookUpTableHandler = GLESHelper.GL_NOT_INIT
    private var LookUpTableTextureId = GLESHelper.GL_NOT_INIT
    private var IntensityHandler = GLESHelper.GL_NOT_INIT

    var intensity = 0f //0~1

    init {
        LookUpTableHandler = GLES30.glGetUniformLocation(mProgramHandler,"lookuptable")
        IntensityHandler = GLES30.glGetUniformLocation(mProgramHandler,"intensity")
    }


    override fun onDrawbefore() {
        super.onDrawbefore()

        if(LookUpTableTextureId == GLESHelper.GL_NOT_INIT){
            LookUpTableTextureId = GLESHelper.creatTextureID(mTextureType,srcWidth,srcHeight)
        }
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(mTextureType,LookUpTableTextureId)
        lutBuffer.position(0)
        GLES30.glTexSubImage2D(mTextureType,0,0,0,srcWidth,srcHeight,
            GLES30.GL_RGBA,GLES30.GL_UNSIGNED_BYTE,lutBuffer)
        GLES30.glUniform1i(LookUpTableHandler,1)
        GLES30.glUniform1f(IntensityHandler,intensity)
    }

    override fun onDrawAfter() {
        super.onDrawAfter()
        GLES30.glBindTexture(mTextureType,0)
    }

    companion object{
        private val FRAGMENT_SHADER = "" +
                "precision mediump float;\n" +
                "varying vec2 textureCoordinate;\n" +
                "uniform sampler2D inputTexture;\n" +
                "uniform sampler2D lookuptable;\n" +
                "uniform highp float intensity;\n" +
                "void main(){\n" +
                "   highp vec4 textureColor = texture2D(inputTexture,textureCoordinate);\n" +
                "   highp float blueColor = textureColor.b * 63.0;\n" +
                "   highp vec2 quad1;\n" +
                "   quad1.y = floor(floor(blueColor) / 8.0);\n" +
                "   quad1.x = floor(blueColor) - (quad1.y * 8.0);\n" +
                "   highp vec2 quad2;\n" +
                "   quad2.y = floor(ceil(blueColor) / 8.0);\n" +
                "   quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n" +
                "   highp vec2 texPos1;\n" +
                "   texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
                "   texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
                "   highp vec2 texPos2;\n" +
                "   texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
                "   texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
                "   lowp vec4 newColor1 = texture2D(lookuptable, texPos1);\n" +
                "   lowp vec4 newColor2 = texture2D(lookuptable, texPos2);\n" +
                "   lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n" +
                "   gl_FragColor = mix(textureColor, vec4(newColor.rgb, textureColor.w), intensity);\n" +
                "}"
    }
}