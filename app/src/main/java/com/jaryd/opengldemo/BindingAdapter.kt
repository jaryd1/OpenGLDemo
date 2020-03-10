package com.jaryd.opengldemo

import android.content.Context
import android.graphics.SurfaceTexture
import android.view.TextureView
import androidx.databinding.BindingAdapter

object BindingAdapter {

    @BindingAdapter("android:onSurfaceTextureAvaliable")
    @JvmStatic
    fun setSurfaceTextureAvaliable(textureView:TextureView,
                                   onSurfaceAvaliable:(context:Context,surface:SurfaceTexture, width:Int, height:Int)->Unit){
        textureView.surfaceTextureListener = object :TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {

            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                return true
            }

            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
                onSurfaceAvaliable.invoke(textureView.context,surface!!,width, height)
            }

        }
    }
}