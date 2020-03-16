package com.jaryd.gles

import android.graphics.PointF
import android.graphics.Rect
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.remove
import com.jaryd.gles.canvas.*
import com.jaryd.gles.utils.CoordinationUtils
import com.jaryd.gles.utils.StickerTexture
import java.nio.ByteBuffer
import java.nio.FloatBuffer

class GLESRender {

    private var mDisHeight = 0
    private var mDisWidth = 0

    private val canvas = SparseArray<GLImageCanvas>()
    private val CAMERA = 0
    private val IMAGE = 1
    private val MARK = 2
    private val FILTER = 3
    private val NV21 = 4


    init {

        canvas.append(CAMERA, GLImageOESCanvas())
        canvas.append(IMAGE, GLImageCanvas())
    }


    fun setDisplaySize(width:Int,height:Int){
        canvas.forEach { key, value ->
            value.onDisplaySizeChanged(width, height)
        }
        canvas[CAMERA].initFrameBuffer(width,height)
        mDisHeight = height
        mDisWidth = width
    }

    fun useSticker(stickers: ArrayList<StickerTexture>){
        canvas[IMAGE]?.release()
        canvas.delete(IMAGE)
        val stickCanvas = GLStickerCanvas(stickers)
        stickCanvas.onDisplaySizeChanged(mDisWidth,mDisHeight)
        canvas.append(IMAGE,stickCanvas)
    }

    fun setRect(rect: Rect,points:Array<PointF>,pitch:Float,yaw:Float,max_width:Int,max_height:Int){
        canvas[IMAGE]?.apply {
            (this as GLStickerCanvas).apply {
                this.pic_height = max_height
                this.pic_width = max_width
                this.faceRect = rect
                this.points = points
                this.needDraw = true
                this.pitch = pitch
                this.yaw = yaw
            }
        }

    }


    fun setTramsMatrix(matrix:FloatArray){
        (canvas[CAMERA] as GLImageOESCanvas).setTextureTransformMatrix(matrix)
    }

    fun drawFrame(textureId:Int){
            var texture = canvas[CAMERA].drawFrameBuffer(textureId)
//            canvas[FILTER].drawFrame(texture)
            canvas[IMAGE].drawFrame(texture)
    }

    fun drawNV21(nv21:ByteArray,width: Int,height: Int){
        (canvas[NV21] as GLNV21Canvas).drawFrame(nv21,width, height)
    }



    fun drawFrame(data:ByteBuffer,width: Int,height: Int){

    }
}