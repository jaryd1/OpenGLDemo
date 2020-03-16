package com.jaryd.gles.utils

data class StickerData(
    var action: Int = 0,
    var baseScale: Double = 0.0,
    var centerIndexList: ArrayList<Int> = arrayListOf(),
    var duration: Int = 0,
    var startIndex: Int = 0,
    var endIndex: Int = 0,
    var frames: Int = 0,
    var width: Int = 0,
    var height: Int = 0,
    var maxcount: Int = 0,
    var offsetX: Double = 0.0,
    var offsetY: Double = 0.0,
    var stickerLooping: Int = 0,
    var stickerName: String = "",
    var type: String = ""
)