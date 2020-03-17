package com.jaryd.opengldemo

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.os.Environment
import android.util.Log
import android.util.Size
import androidx.lifecycle.ViewModel
import com.jaryd.facepp.FacePPController
import com.jaryd.gles.EglCore
import com.jaryd.gles.GLESHelper
import com.jaryd.gles.GLESRender
import com.jaryd.gles.WindowSurface
import com.jaryd.gles.canvas.GLStickerCanvas
import com.jaryd.gles.utils.StickerTexture
import com.jaryd.gles.utils.Stickers
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class MainViewModel:ViewModel() {

    private lateinit var cameraSurfaceTexture: SurfaceTexture
    private lateinit var camera: CameraImp


    val onSurfaceTextureAvaliable:(context: Context, surface:SurfaceTexture, width:Int, height:Int)->Unit
        ={
        context,surface, width, height -> kotlin.run {
            val egl = EglCore()
            val window = WindowSurface(egl,surface)

            window.makeCurrent()
            val render = GLESRender()
            render.setDisplaySize(width, height)
            Stickers.init(context,"cat")
            render.useSticker(StickerTexture.createStickers(context,Stickers.stickers))

            egl.makeNothingCurrent()

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
            camera = CameraImp(context)
            camera.mExpectSize = Size(width, height)
            camera.addSurfaceTexture(cameraSurfaceTexture)
            camera.addNV21CallBack { nv21, width, height ->
                FacePPController.trackFace(nv21,width, height)

            }
            FacePPController.checkLicense(context)
            FacePPController.initTrack { face, max_width, max_height ->
                Log.e("tag","pitch ${face.pitch} ..., yaw ${face.yaw}")
                render.setRect(face.rect,face.points,face.pitch,face.yaw,max_width,max_height)
            }
            FacePPController.prepareFaceTracker(context,90,1080,1920)
            camera.StartPreview()

        }
    }

    //解压贴纸
    fun unZipSticker(context: Context){


        val root = context.getExternalFilesDir(null)
        val cat = File(root,"cat")
        if(cat.exists())
            return
        cat.mkdir()
        val input = ZipInputStream(context.assets.open("cat.zip"))
        var entry:ZipEntry? = input.nextEntry
        while(entry != null){
            var file = File(root,entry.name)
            if(entry.isDirectory){
                if(!file.exists()){
                    file.mkdir()
                }
            }else{
                file.createNewFile()
                val output = FileOutputStream(file)
                output.write(input.readBytes())
                output.flush()
                output.close()
            }
            input.closeEntry()
            entry = input.nextEntry
        }
        input.close()
    }
}