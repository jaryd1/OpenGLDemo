package com.jaryd.gles.canvas

import android.graphics.PointF
import android.graphics.Rect
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import com.jaryd.gles.GLESHelper
import com.jaryd.gles.utils.CoordinationUtils
import com.jaryd.gles.utils.StickerTexture

class GLStickerCanvas(val stickers: ArrayList<StickerTexture>):GLImageCanvas() {
    var faceRect:Rect? = null
    var points:Array<PointF>? =null
    var pic_width = 1
    var pic_height = 1
    var pitch = 0f //俯仰角 ，但 face++ sdk 返回有问题
    var yaw = 0f // 偏航角 ，同上
    var needDraw =false
    val modelMatrix = FloatArray(16)
    val viewMatrix = FloatArray(16)
    val projectMatrix = FloatArray(16)
    val matrix = FloatArray(16)

    val vertex_pos_Buffer = GLESHelper.creatFloatBuffer(FloatArray(8))
    val texture_pos_Buffer =GLESHelper.creatFloatBuffer(CoordinationUtils.mTexturesCoor)



    private fun processMatrix(center_x:Float,center_y:Float){
        Matrix.setIdentityM(matrix,0)
        Matrix.setIdentityM(modelMatrix,0)

        Matrix.translateM(modelMatrix,0,center_x,center_y,0f)
        //姿态处理，数据有问题
//        Matrix.rotateM(modelMatrix,0,0f,0f,0f,1f)
//        Matrix.rotateM(modelMatrix,0,pitch,1f,0f,0f)
//        Matrix.rotateM(modelMatrix,0,yaw,0f,1f,0f)
        Matrix.translateM(modelMatrix,0,-center_x,-center_y,0f)
        Matrix.setIdentityM(viewMatrix,0)
        Matrix.setLookAtM(viewMatrix,0,0f,0f,6f,0f
            ,0f,0f,0f,1f,0f)
        Matrix.multiplyMM(matrix,0,viewMatrix,0,modelMatrix,0)

        Matrix.setIdentityM(projectMatrix,0)
        val r = 1/2f  // 摄像机位于(0,0,6),near平面 为3
        val b = 1/2f
        Matrix.frustumM(projectMatrix,0,-r,r,-b,b,3f,9f)

        Matrix.multiplyMM(matrix,0,projectMatrix,0,matrix,0)
    }

    override fun onDrawAfter() {
        super.onDrawAfter()
        if(!needDraw)
            return
        needDraw = false
        faceRect?.apply {
            this.left = this.left*mDisplayWidth/pic_width
            this.right = this.right*mDisplayWidth/pic_width
            this.top = this.top*mDisplayHeight/pic_height
            this.bottom = this.bottom*mDisplayHeight/pic_width

        }
        points?.apply {
            stickers.forEachIndexed { index, sticker ->

                processPoint(sticker.centerIndex)
//
                GLES30.glEnable(GLES30.GL_BLEND)
                GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
                sticker.updateIndex(System.currentTimeMillis())
                if (sticker.textureId == GLESHelper.GL_NOT_INIT)
                    sticker.textureId =
                        GLESHelper.creatTextureID(mTextureType, sticker.width, sticker.height)

                GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
                GLES30.glBindTexture(mTextureType, sticker.textureId)
                GLES30.glTexSubImage2D(
                    mTextureType, 0, 0, 0, sticker.width, sticker.height, GLES30.GL_RGBA,
                    GLES30.GL_UNSIGNED_BYTE, sticker.textures[sticker.currentIndex].position(0)
                )
                GLES30.glUniform1i(mInputTextureHandler, 0)

                vertex_pos_Buffer.clear()

                calculatingCoord(sticker)
                GLES30.glUniformMatrix4fv(mMvpMatrixHandler, 1, false, matrix, 0)

                vertex_pos_Buffer.position(0)
                texture_pos_Buffer.position(0)
                GLES30.glEnableVertexAttribArray(mPositionHandler)
                GLES30.glVertexAttribPointer(
                    mPositionHandler,
                    2,
                    GLES30.GL_FLOAT,
                    false,
                    0,
                    vertex_pos_Buffer
                )

                GLES30.glEnableVertexAttribArray(mTextureCoordinateHandler)
                GLES30.glVertexAttribPointer(
                    mTextureCoordinateHandler,
                    2,
                    GLES30.GL_FLOAT,
                    false,
                    0,
                    texture_pos_Buffer
                )

                GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
                GLES30.glDisableVertexAttribArray(mPositionHandler)
                GLES30.glDisableVertexAttribArray(mTextureCoordinateHandler)
                GLES30.glBindTexture(mTextureType,0)

                GLES30.glDisable(GLES30.GL_BLEND)
            }
        }
    }

    private fun Array<PointF>.calculatingCoord(sticker: StickerTexture) {
        val target_width = (faceRect?.right!! - faceRect?.left!!) * sticker.scale
        val target_height = sticker.height * target_width / sticker.width

        val left = this[sticker.centerIndex].x + target_width * (sticker.offsetX - 0.5f)
        val right = this[sticker.centerIndex].x + target_width * (sticker.offsetX + 0.5f)
        val top = this[sticker.centerIndex].y + target_height * (sticker.offsetY - 0.5f)
        val bottom = this[sticker.centerIndex].y + target_height * (sticker.offsetY + 0.5f)


        val t0x = 2 * left / mDisplayWidth.toFloat() - 1f
        val t1x = 2 * right / mDisplayWidth.toFloat() - 1f
        val t0y = 1f - 2 * bottom / mDisplayHeight.toFloat()
        val t1y = 1f - 2 * top / mDisplayHeight.toFloat()
        processMatrix((t1x+t0x)/2,(t0y+t1y)/2)
        vertex_pos_Buffer.put(
            floatArrayOf(
                t0x,
                t1y,
                t1x,
                t1y,
                t0x,
                t0y,
                t1x,
                t0y
            )
        ) //png 上下颠倒
    }

    private fun processPoint(index:Int){
        points?.apply {
            val point = this[index]
            val temp = point.y
            point.y = pic_height - point.x
            point.x = pic_width-temp
            point.y = point.y*mDisplayHeight/pic_height
            point.x = point.x*mDisplayWidth/pic_width
            this.set(index,point)
        }
    }
}