package com.jaryd.gles.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.jaryd.gles.GLESHelper
import java.io.File
import java.nio.ByteBuffer

class StickerTexture(context: Context,stickerData: StickerData){
     var width = 0 //图片原始宽高
     var height = 0
     var scale = 0f //图片按人脸缩放
     var offsetX =0f //图片原始大小偏移系数
     var offsetY =0f
     var centerIndex = 0 //中心点索引 与人脸sdk配合使用
     var counts = 0 //图片个数
     var name = "" //滤镜名称
     var duration = 0 //单图片持续时间
     var currentIndex = 0 //图片索引
     var textures = arrayListOf<ByteBuffer>() //图片buffer
    var  lastTime = -1L
    var  currentDuration = 0
    var  textureId = -1

    init {
        width = stickerData.width
        height = stickerData.height
        scale = stickerData.baseScale.toFloat()
        offsetX = stickerData.offsetX.toFloat()
        offsetY = stickerData.offsetY.toFloat()
        centerIndex = stickerData.centerIndexList[0]
        counts = stickerData.frames
        name = stickerData.stickerName
        duration = stickerData.duration
        currentIndex = 0
        textureId = -1

        val dir = File(context.getExternalFilesDir(null),"cat/$name")
        for (i in 0 until counts){
            val file = File(dir,"${name}_00${i}.png")
            val bitmap = BitmapFactory.decodeStream(file.inputStream())

            val buffer = ByteBuffer.allocateDirect(bitmap.byteCount)
            bitmap.copyPixelsToBuffer(buffer)
            bitmap.recycle()
            textures.add(buffer)
        }

    }

    fun updateIndex(milliseconds:Long){
        if(lastTime == -1L){
            lastTime = milliseconds
            currentDuration = 0
            currentIndex = 0
        }else{
            currentDuration += (milliseconds-lastTime).toInt()
            currentIndex = (currentDuration/duration)%counts
            lastTime = milliseconds
        }
    }

    companion object{
        fun createStickers(context: Context,stickerdatas:ArrayList<StickerData>):ArrayList<StickerTexture>{
            val result = arrayListOf<StickerTexture>()
            stickerdatas.forEachIndexed { index, stickerData ->
                val stickerTexture = StickerTexture(context,stickerData)
                result.add(stickerTexture)
            }
            return result
        }
    }

}