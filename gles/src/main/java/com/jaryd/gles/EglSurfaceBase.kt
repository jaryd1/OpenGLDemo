package com.jaryd.gles

import android.opengl.EGL14
import android.opengl.GLES30
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

open class EglSurfaceBase (protected var mEglCore: EglCore) {

    private var mEGLSurface = EGL14.EGL_NO_SURFACE
    private var mWidth = -1
    private var mHeight = -1

    /**
     * Returns the surface's width, in pixels.
     *
     *
     * If this is called on a window surface, and the underlying surface is in the process
     * of changing size, we may not see the new size right away (e.g. in the "surfaceChanged"
     * callback).  The size should match after the next buffer swap.
     */
    val width: Int
        get() = if (mWidth < 0) {
            mEglCore.querySurface(mEGLSurface, EGL14.EGL_WIDTH)
        } else {
            mWidth
        }

    /**
     * Returns the surface's height, in pixels.
     */
    val height: Int
        get() = if (mHeight < 0) {
            mEglCore.querySurface(mEGLSurface, EGL14.EGL_HEIGHT)
        } else {
            mHeight
        }

    /**
     * 获取当前帧的缓冲
     * @return
     */
    val currentFrame: ByteBuffer
        get() {
            val width = width
            val height = height
            val buf = ByteBuffer.allocateDirect(width * height * 4)
            buf.order(ByteOrder.LITTLE_ENDIAN)
            GLES30.glReadPixels(
                0, 0, width, height,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buf
            )
            buf.rewind()
            return buf
        }

    /**
     * Creates a window surface.
     *
     *
     * @param surface May be a Surface or SurfaceTexture.
     */
    fun createWindowSurface(surface: Any) {
        if (mEGLSurface !== EGL14.EGL_NO_SURFACE) {
            throw IllegalStateException("surface already created")
        }
        mEGLSurface = mEglCore.createWindowSurface(surface)

        // Don't cache width/height here, because the size of the underlying surface can change
        // out from under us (see e.g. HardwareScalerActivity).
        //mWidth = mEglCore.querySurface(mEGLSurface, EGL14.EGL_WIDTH);
        //mHeight = mEglCore.querySurface(mEGLSurface, EGL14.EGL_HEIGHT);
    }

    /**
     * Creates an off-screen surface.
     */
    fun createOffscreenSurface(width: Int, height: Int) {
        if (mEGLSurface !== EGL14.EGL_NO_SURFACE) {
            throw IllegalStateException("surface already created")
        }
        mEGLSurface = mEglCore.createOffscreenSurface(width, height)
        mWidth = width
        mHeight = height
    }

    /**
     * Release the EGL surface.
     */
    fun releaseEglSurface() {
        mEglCore.releaseSurface(mEGLSurface)
        mEGLSurface = EGL14.EGL_NO_SURFACE
        mHeight = -1
        mWidth = mHeight
    }

    /**
     * Makes our EGL context and surface current.
     */
    fun makeCurrent() {
        mEglCore.makeCurrent(mEGLSurface)
    }

    /**
     * Makes our EGL context and surface current for drawing, using the supplied surface
     * for reading.
     */
    fun makeCurrentReadFrom(readSurface: EglSurfaceBase) {
        mEglCore.makeCurrent(mEGLSurface, readSurface.mEGLSurface)
    }

    /**
     * Calls eglSwapBuffers.  Use this to "publish" the current frame.
     *
     * @return false on failure
     */
    fun swapBuffers(): Boolean {
        val result = mEglCore.swapBuffers(mEGLSurface)
        if (!result) {
            Log.d(TAG, "WARNING: swapBuffers() failed")
        }
        mEglCore.makeNothingCurrent()
        return result
    }

    /**
     * Sends the presentation time stamp to EGL.
     *
     * @param nsecs Timestamp, in nanoseconds.
     */
    fun setPresentationTime(nsecs: Long) {
        mEglCore.setPresentationTime(mEGLSurface, nsecs)
    }

    companion object {
        protected val TAG = "EglSurfaceBase"
    }

}
