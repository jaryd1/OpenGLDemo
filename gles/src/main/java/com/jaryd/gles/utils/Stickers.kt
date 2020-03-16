package com.jaryd.gles.utils

import android.content.Context
import android.util.JsonReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

object Stickers {
    var enable = false
    val stickers = ArrayList<StickerData>()
    fun init(context: Context,name:String){
        val dir = File(context.getExternalFilesDir(null),name)
        if(!dir.exists() || !dir.isDirectory){
            enable = false
            return
        }
        enable = true
        val reader = JsonReader(InputStreamReader(
                FileInputStream(File(dir,"json")),"UTF-8"))
        reader.beginObject()
        while(reader.hasNext()){
            if(reader.nextName() =="stickerList") {
                reader.beginArray()
                while(reader.hasNext())
                    stickers.add(parse(reader))
                reader.endArray()
            }
        }
        reader.endObject()
    }

    private fun parse(reader:JsonReader):StickerData{
        val sticker = StickerData()

        reader.beginObject()
        while(reader.hasNext()){
            when(reader.nextName()){
                "action"    ->sticker.action = reader.nextInt()
                "baseScale" ->sticker.baseScale = reader.nextDouble()
                "duration"  ->sticker.duration = reader.nextInt()
                "startIndex"  ->sticker.endIndex = reader.nextInt()
                "endIndex"  ->sticker.endIndex = reader.nextInt()
                "frames"    ->sticker.frames = reader.nextInt()
                "height"    ->sticker.height = reader.nextInt()
                "width"     ->sticker.width = reader.nextInt()
                "maxcount"  ->sticker.maxcount = reader.nextInt()
                "offsetX"   ->sticker.offsetX = reader.nextDouble()
                "offsetY"   ->sticker.offsetY = reader.nextDouble()
                "stickerLooping" ->sticker.stickerLooping = reader.nextInt()
                "stickerName"   ->sticker.stickerName = reader.nextString()
                "type"      ->sticker.type = reader.nextString()
                "centerIndexList" ->{
                    reader.beginArray()
                    while(reader.hasNext()){
                        sticker.centerIndexList.add(reader.nextInt())
                    }
                    reader.endArray()
                }
                else        ->reader.skipValue()
            }
        }
        reader.endObject()
        return sticker
    }
}