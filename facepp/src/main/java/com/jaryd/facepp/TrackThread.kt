package com.jaryd.facepp

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.jaryd.facepp.utils.ConUtil
import com.jaryd.facepp.utils.SensorEventUtil
import com.megvii.facepp.sdk.Facepp
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class TrackThread(name:String = "TrackThread",private var callBack:((Facepp.Face,Int,Int)->Unit)?=null):Thread(name) {



    private val mLock by  lazy { ReentrantLock() }
    private val mCondition by lazy { mLock.newCondition() }
    private var mReady =false

    private lateinit var facepp: Facepp
    private lateinit var sensorUtils:SensorEventUtil

    private lateinit var looper: Looper
    private lateinit var handler: Handler

    private val TAG ="track"


    override fun run() {
        Looper.prepare()
        mLock.withLock {
            looper = Looper.myLooper()!!
            handler = Handler(looper)
            mReady = true
            mCondition.signalAll()
        }
        Log.e(TAG,"loop before")
        Looper.loop()
        Log.e(TAG,"loop after")
        mLock.withLock {
            release()
            handler.removeCallbacksAndMessages(null)
            mReady = false
        }
    }

    fun waitUntilReady(){
        mLock.withLock {
            if(!mReady){
                mCondition.await()
            }
        }
    }

    private fun getLooper():Looper?{
        if(!isAlive)
            return null
        return looper
    }

    fun quitSafely():Boolean{
        getLooper()?.quitSafely()
        return true
    }

    fun prepareFaceTrack(context: Context,orientation:Int,width:Int,height:Int){
        waitUntilReady()
        handler.post {
            internalPrepareFaceTracker(context, orientation, width, height)
        }
    }

    fun trackFace(data: ByteArray,width: Int,height: Int){
        waitUntilReady()
        handler.post {
            internalTrackFace(data, width, height)
        }
    }

    @Synchronized
    private fun internalPrepareFaceTracker(context: Context,orientation:Int,
                                           width:Int,height:Int){
        if(!FaceTrackParam.canFaceTrack)
            return
        facepp = Facepp()
        sensorUtils = SensorEventUtil(context)
        ConUtil.acquireWakeLock(context)
        if(!FaceTrackParam.previewTrack){
            FaceTrackParam.rotateAngle = orientation
        }else{
            FaceTrackParam.rotateAngle = if( FaceTrackParam.isBackCamera) orientation
                                            else 360-orientation
        }

        var left = 0
        var top = 0
        var right = width
        var bottom = height

        if(FaceTrackParam.enableROIDetect){
            val line =  height.toFloat()* FaceTrackParam.roiRatio
            left = ((width.toFloat()-line)/2f).toInt()
            top = ((height.toFloat()-line)/2f).toInt()
            right = width-left
            bottom = height-top
        }

        facepp.init(context,ConUtil.getFileContent(context,R.raw.megviifacepp_0_5_2_model))
        val config = facepp.faceppConfig
        config.interval = FaceTrackParam.detectInterval
        config.minFaceSize = FaceTrackParam.minFaceSize
        config.roi_left =left
        config.roi_right = right
        config.roi_top = top
        config.roi_bottom = bottom
        config.one_face_tracking = if(FaceTrackParam.enableMultiFace) 0 else 1
        config.detectionMode = FaceTrackParam.trackMode
        facepp.faceppConfig = config
    }

    private fun internalTrackFace(data:ByteArray,width: Int,height: Int){
        if(!FaceTrackParam.canFaceTrack)
            return
        val faceDetectTime_action = System.currentTimeMillis()
        val orientation = if (FaceTrackParam.previewTrack) sensorUtils.orientation else 0
        var rotation = 0
        when(orientation){
            0 -> rotation = FaceTrackParam.rotateAngle
            1 -> rotation = 0 //90 度
            2 -> rotation = 180 //270
            3 -> rotation = 360-FaceTrackParam.rotateAngle //180
        }

        val config = facepp.faceppConfig
        if(config.rotation != rotation){
            config.rotation = rotation
            facepp.faceppConfig = config
        }

        val faces = facepp.detect(data,width,height,
            if(FaceTrackParam.previewTrack) Facepp.IMAGEMODE_NV21 else Facepp.IMAGEMODE_RGBA)
        if(faces.size >0 && faces[0] != null){
            /*
                width ,height在 camera中相互调换，多次log测试，前摄人脸越往下，rect中的left越小,越往右，rect中的top越小,
                故重构rect
             */
            val y_offset = faces[0].rect.bottom-faces[0].rect.top
            faces[0].rect.bottom = width-faces[0].rect.left
            val x_offset = faces[0].rect.right-faces[0].rect.left
            faces[0].rect.right = height-faces[0].rect.top
            faces[0].rect.top = faces[0].rect.bottom-y_offset
            faces[0].rect.left = faces[0].rect.right-x_offset

            facepp.getLandmarkRaw(faces[0],if(FaceTrackParam.enable106Points) Facepp.FPP_GET_LANDMARK106 else Facepp.FPP_GET_LANDMARK81)
            callBack?.invoke(faces[0],height,width)//width,height调换
        }

    }




    private fun release(){
        Log.e(TAG,"release")
        facepp.release()
    }
}