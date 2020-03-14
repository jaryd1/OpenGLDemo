package com.jaryd.facepp

import android.content.Context
import com.jaryd.facepp.utils.ConUtil
import com.jaryd.facepp.utils.Util
import com.megvii.facepp.sdk.Facepp
import com.megvii.licensemanager.sdk.LicenseManager

object FacePPController {

    private val tracker by lazy { FaceTracker() }

    fun checkLicense(context: Context){
        if(Facepp.getSDKAuthType(ConUtil.
            getFileContent(context,R.raw.megviifacepp_0_5_2_model)) == 2){//离线授权
            FaceTrackParam.canFaceTrack = true
            return
        }
        val licenseManager = LicenseManager(context)
        val uuid =ConUtil.getUUIDString(context)
        val apiName = Facepp.getApiName()
        licenseManager.setAuthTimeBufferMillis(0)
        licenseManager.takeLicenseFromNetwork(Util.CN_LICENSE_URL,uuid,Util.API_KEY,
            Util.API_SECRET,apiName,"1",object :LicenseManager.TakeLicenseCallback{
                override fun onSuccess() {
                    FaceTrackParam.canFaceTrack = true
                }

                override fun onFailed(p0: Int, p1: ByteArray?) {
                    FaceTrackParam.canFaceTrack = false
                    ConUtil.showToast(context,"request license faild")
                }

            })
    }

    fun initTrack(trackCallback:(Facepp.Face,max_width:Int,max_height:Int)->Unit) {
        FaceTrackParam.reset()
        tracker.initTracker(trackCallback)
    }

    fun prepareFaceTracker(context: Context,oriention:Int,width:Int,height:Int){
        tracker.prepareFaceTracker(context, oriention, width, height)
    }

    fun trackFace(data:ByteArray,width: Int,height: Int){
        tracker.trackFace(data, width, height)
    }

    fun destroy(){
        tracker.destroyTracker()
    }


}