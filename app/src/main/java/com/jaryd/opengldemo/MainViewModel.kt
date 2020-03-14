package com.jaryd.opengldemo

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.util.Size
import androidx.lifecycle.ViewModel
import com.jaryd.facepp.FacePPController
import com.jaryd.gles.EglCore
import com.jaryd.gles.GLESHelper
import com.jaryd.gles.GLESRender
import com.jaryd.gles.WindowSurface

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
//                Log.e("point 45","x:${face.points[45].x},y:${face.points[45].y}")
                render.setRect(face.rect,face.points,max_width,max_height)
            }
            FacePPController.prepareFaceTracker(context,90,1080,1920)
            camera.StartPreview()

        }
    }
}