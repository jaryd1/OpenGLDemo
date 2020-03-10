package com.jaryd.opengldemo

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.lifecycle.ViewModel
import com.jaryd.gles.EglCore
import com.jaryd.gles.EglCore.Companion.FLAG_RECORDABLE
import com.jaryd.gles.EglCore.Companion.FLAG_TRY_GLES3
import com.jaryd.gles.GLESHelper
import com.jaryd.gles.GLESRender
import com.jaryd.gles.WindowSurface
import java.nio.ByteBuffer
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL11Ext

class MainViewModel:ViewModel() {

    private lateinit var cameraSurfaceTexture: SurfaceTexture

    val onSurfaceTextureAvaliable:(context: Context, surface:SurfaceTexture, width:Int, height:Int)->Unit
        ={
        context,surface, width, height -> kotlin.run {
            val egl = EglCore()
            val window = WindowSurface(egl,surface)
            val bitmap  = BitmapFactory.decodeStream(context.assets.open("watermark.png"))
            val buffer = ByteBuffer.allocateDirect(bitmap.byteCount)
            bitmap.copyPixelsToBuffer(buffer)

            window.makeCurrent()
            val render = GLESRender()
            render.userWaterMark(buffer,bitmap.width,bitmap.height)
            render.setDisplaySize(width, height)
            bitmap.recycle()
            val textureOES = GLESHelper.creatTextureID(GLES11Ext.GL_TEXTURE_EXTERNAL_OES)
             cameraSurfaceTexture = SurfaceTexture(textureOES)
            cameraSurfaceTexture.setOnFrameAvailableListener {
                window.makeCurrent()
                cameraSurfaceTexture.updateTexImage()
                val matrix = FloatArray(16)
                cameraSurfaceTexture.getTransformMatrix(matrix)
                render.setTramsMatrix(matrix)
                render.drawFrame(textureOES)
                window.swapBuffers()
            }
            val camera = CameraImp(context)
            camera.mExpectSize = Size(width, height)
            camera.addSurfaceTexture(cameraSurfaceTexture)
            camera.StartPreview()

        }
    }
}