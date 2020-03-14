package com.jaryd.gles

import android.graphics.PointF
import android.graphics.Rect
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
    private val NV21 = 4


    init {
        mVertexs =GLESHelper.creatFloatBuffer(CoordinationUtils.mVertexsCoor)
        mTextures = GLESHelper.creatFloatBuffer(CoordinationUtils.mTexturesCoor)
        mInvertTextures = GLESHelper.creatFloatBuffer(CoordinationUtils.mInvertTexturesCoor)
        canvas.append(CAMERA, GLImageOESCanvas())
        canvas.append(IMAGE, GLImageCanvas())
        canvas.append(NV21, GLNV21Canvas())
        canvas.append(FILTER,GLFACEPPCanvas())
    }


    fun setDisplaySize(width:Int,height:Int){
        canvas.forEach { key, value ->
            value.onDisplaySizeChanged(width, height)
        }
        canvas[CAMERA].initFrameBuffer(width,height)
        canvas[FILTER].initFrameBuffer(width,height)
    }

    fun setRect(rect: Rect,points:Array<out PointF>,max_width:Int,max_height:Int){
        (canvas[FILTER] as GLFACEPPCanvas).apply {
            this.rect = rect
            this.points = points
            this.needReDraw = true
            this.MaxHeight = max_height
            this.MaxWidth =  max_width
        }

    }


    fun setTramsMatrix(matrix:FloatArray){
        (canvas[CAMERA] as GLImageOESCanvas).setTextureTransformMatrix(matrix)
    }

    fun drawFrame(textureId:Int){
            var texture = canvas[CAMERA].drawFrameBuffer(textureId)
            canvas[FILTER].drawFrame(texture)
//            canvas[IMAGE].drawFrame(texture)
    }

    fun drawNV21(nv21:ByteArray,width: Int,height: Int){
        (canvas[NV21] as GLNV21Canvas).drawFrame(nv21,width, height)
    }



    fun drawFrame(data:ByteBuffer,width: Int,height: Int){

    }
}