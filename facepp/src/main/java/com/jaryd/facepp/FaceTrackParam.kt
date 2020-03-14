package com.jaryd.facepp

import com.megvii.facepp.sdk.Facepp

object FaceTrackParam {
    // 是否允许检测
    var canFaceTrack = false

    // 旋转角度
    var rotateAngle = 0

    // 是否相机预览检测，true为预览检测，false为静态图片检测
    var previewTrack = false

    // 是否允许3D姿态角
    var enable3DPose = false

    // 是否允许区域检测
    var enableROIDetect = false

    // 检测区域缩放比例
    var roiRatio = 0f

    // 是否允许106个关键点
    var enable106Points = false

    // 是否后置摄像头
    var isBackCamera = false

    // 是否允许人脸年龄检测
    var enableFaceProperty = false

    // 是否允许多人脸检测
    var enableMultiFace = false

    // 最小人脸大小
    var minFaceSize = 0

    // 检测间隔
    var detectInterval = 0

    // 检测模式
    var trackMode = 0


    fun reset() {
        previewTrack = true
        enable3DPose = false
        enableROIDetect = false
        roiRatio = 0.8f
        enable106Points = true
        isBackCamera = false
        enableFaceProperty = false
        enableMultiFace = true
        minFaceSize = 200
        detectInterval = 25
        trackMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING_FAST
    }
}