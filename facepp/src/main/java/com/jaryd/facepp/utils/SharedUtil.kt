package com.jaryd.facepp.utils

import android.content.Context

/**
 * Save Data To SharePreference Or Get Data from SharePreference
 *
 * 通过SharedPreferences来存储数据，自定义类型
 */
class SharedUtil(private val ctx: Context) {
    private val FileName = "megvii"
    fun saveIntValue(key: String?, value: Int) {
        val sharePre = ctx.getSharedPreferences(
            FileName,
            Context.MODE_PRIVATE
        )
        val editor = sharePre.edit()
        editor.putInt(key, value)
        editor.commit()
    }

    fun saveLongValue(key: String?, value: Long) {
        val sharePre = ctx.getSharedPreferences(
            FileName,
            Context.MODE_PRIVATE
        )
        val editor = sharePre.edit()
        editor.putLong(key, value)
        editor.commit()
    }

    fun writeDownStartApplicationTime() {
        val sp =
            ctx.getSharedPreferences(FileName, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        //		Calendar calendar = Calendar.getInstance();
        //Date now = calendar.getTime();
        //		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd:hh-mm-ss");
        val editor = sp.edit()
        //editor.putString("启动时间", now.toString());
        editor.putLong("nowtimekey", now)
        editor.commit()
    }

    fun saveBooleanValue(key: String?, value: Boolean) {
        val sharePre = ctx.getSharedPreferences(
            FileName,
            Context.MODE_PRIVATE
        )
        val editor = sharePre.edit()
        editor.putBoolean(key, value)
        editor.commit()
    }

    fun removeSharePreferences(key: String?) {
        val sharePre = ctx.getSharedPreferences(
            FileName,
            Context.MODE_PRIVATE
        )
        val editor = sharePre.edit()
        editor.remove(key)
        editor.commit()
    }

    operator fun contains(key: String?): Boolean {
        val sharePre = ctx.getSharedPreferences(
            FileName,
            Context.MODE_PRIVATE
        )
        return sharePre.contains(key)
    }

    val allMap: Map<String, Any?>
        get() {
            val sharePre = ctx.getSharedPreferences(
                FileName,
                Context.MODE_PRIVATE
            )
            return sharePre.all as Map<String, Any?>
        }

    fun getIntValueByKey(key: String?): Int {
        val sharePre = ctx.getSharedPreferences(
            FileName,
            Context.MODE_PRIVATE
        )
        return sharePre.getInt(key, -1)
    }

    fun getLongValueByKey(key: String?): Long {
        val sharePre = ctx.getSharedPreferences(
            FileName,
            Context.MODE_PRIVATE
        )
        return sharePre.getLong(key, -1)
    }

    fun saveStringValue(key: String?, value: String?) {
        val sharePre = ctx.getSharedPreferences(
            FileName,
            Context.MODE_PRIVATE
        )
        val editor = sharePre.edit()
        editor.putString(key, value)
        editor.commit()
    }

    fun getStringValueByKey(key: String?): String? {
        val sharePre = ctx.getSharedPreferences(
            FileName,
            Context.MODE_PRIVATE
        )
        return sharePre.getString(key, null)
    }

    fun getBooleanValueByKey(key: String?): Boolean {
        val sharePre = ctx.getSharedPreferences(
            FileName,
            Context.MODE_PRIVATE
        )
        return sharePre.getBoolean(key, false)
    }

    fun getIntValueAndRemoveByKey(key: String?): Int {
        val value = getIntValueByKey(key)
        removeSharePreferences(key)
        return value
    }

    var userkey: String?
        get() = getStringValueByKey("params_userkey")
        set(userkey) {
            saveStringValue("params_userkey", userkey)
        }

}