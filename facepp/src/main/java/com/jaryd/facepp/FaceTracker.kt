package com.jaryd.facepp

import android.content.Context
import com.megvii.facepp.sdk.Facepp
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class FaceTracker {
    private val mLock by lazy { ReentrantLock() }
    private val mCondition by lazy { mLock.newCondition() }
    private lateinit var mThread:TrackThread
    fun initTracker(trackCallBack:(Facepp.Face,max_width:Int,max_height:Int)->Unit){
        mThread = TrackThread(callBack = trackCallBack)
        mLock.withLock {
            mThread.start()
            mThread.waitUntilReady()
        }
    }


    fun prepareFaceTracker(context: Context,oriention:Int,width:Int,height:Int){
        mLock.withLock {
            mThread.prepareFaceTrack(context,oriention,width, height)
        }
    }

    fun trackFace(data:ByteArray,width: Int,height: Int){
        mLock.withLock {
            mThread.trackFace(data, width, height)
        }
    }

    fun destroyTracker(){
        mLock.withLock {
            mThread.quitSafely()
        }
    }


}