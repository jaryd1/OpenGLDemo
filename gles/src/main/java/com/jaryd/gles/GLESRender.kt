package com.jaryd.gles

import android.util.Log
import android.util.SparseArray
import androidx.core.util.forEach
import com.jaryd.gles.canvas.*
import com.jaryd.gles.utils.CoordinationUtils
import java.nio.ByteBuffer
import java.nio.FloatBuffer

class GLESRender {

    private  var mVertexs:FloatBuffer
    private  var mTextures:FloatBuffer
    private  var mInvertTextures:FloatBuffer
    private val canvas = SparseArray<GLImageCanvas>()
    private val CAMERA = 0
    private val IMAGE = 1
    private val MARK = 2
    private val FILTER = 3


    init {
        mVertexs =GLESHelper.creatFloatBuffer(CoordinationUtils.mVertexsCoor)
        mTextures = GLESHelper.creatFloatBuffer(CoordinationUtils.mTexturesCoor)
        mInvertTextures = GLESHelper.creatFloatBuffer(CoordinationUtils.mInvertTexturesCoor)
        canvas.append(CAMERA, GLImageOESCanvas())
        canvas.append(IMAGE, GLImageCanvas())
    }

    fun userWaterMark(buffer: ByteBuffer,width: Int,height: Int){
        canvas.append(MARK,GLOverlayCanvas(buffer,width,height))
    }

    fun useLUTFilter(lut:ByteBuffer,width: Int,height: Int){
        canvas.append(FILTER,GLLUTCanvac(lut,width,height))
    }

    fun setDisplaySize(width:Int,height:Int){
        canvas.forEach { key, value ->
            value.onDisplaySizeChanged(width, height)
        }
        canvas[CAMERA].initFrameBuffer(width,height)
        canvas[FILTER].initFrameBuffer(width,height)
        canvas[MARK]?.initFrameBuffer(width, height)
    }




    fun setTramsMatrix(matrix:FloatArray){
        (canvas[CAMERA] as GLImageOESCanvas).setTextureTransformMatrix(matrix)
    }

    fun drawFrame(textureId:Int){
            var texture = canvas[CAMERA].drawFrameBuffer(textureId)
            texture = canvas[FILTER]?.drawFrameBuffer(texture)?:texture
            texture = canvas[MARK]?.drawFrameBuffer(texture)?:texture
            canvas[IMAGE].drawFrame(texture)
    }



    fun drawFrame(data:ByteBuffer,width: Int,height: Int){

    }
}