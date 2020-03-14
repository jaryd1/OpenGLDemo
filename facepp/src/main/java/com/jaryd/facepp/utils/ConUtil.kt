package com.jaryd.facepp.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.Camera
import android.media.ExifInterface
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.text.TextUtils
import android.util.Base64
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class ConUtil {
    companion object{
        fun isReadKey(context: Context):Boolean{
            var inputStream: InputStream? = null
            val byteArrayOutputStream =
                ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var count = -1
            try {
                inputStream = context.assets.open("key")
                while (inputStream.read(buffer).also { count = it } != -1) {
                    byteArrayOutputStream.write(buffer, 0, count)
                }
                byteArrayOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            val str = String(byteArrayOutputStream.toByteArray())
            var key: String? = null
            var screct: String? = null
            try {
                val strs = str.split(";").toTypedArray()
                key = strs[0].trim { it <= ' ' }
                screct = strs[1].trim { it <= ' ' }
            } catch (e: Exception) {
            }
            Util.API_KEY = key
            Util.API_SECRET = screct
            return if (Util.API_KEY == null || Util.API_SECRET == null) false else true
        }

        fun toggleHideyBar(activity: Activity) {
            val uiOptions = activity.window.decorView.systemUiVisibility
            var newUiOptions = uiOptions
            if (Build.VERSION.SDK_INT >= 14) {
                newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            }
            if (Build.VERSION.SDK_INT >= 16) {
                newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_FULLSCREEN
            }
            if (Build.VERSION.SDK_INT >= 19) {
                newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }
            activity.window.decorView.systemUiVisibility = newUiOptions
        }

        fun getFormatterDate(time: Long): String? {
            val d = Date(time)
            val formatter = SimpleDateFormat("yyyy-MM-dd")
            return formatter.format(d)
        }

        fun getUUIDString(mContext: Context?): String? {
            val KEY_UUID = "key_uuid"
            val sharedUtil = SharedUtil(mContext!!)
            var uuid: String? = sharedUtil.getStringValueByKey(KEY_UUID)
            if (uuid != null && uuid.trim { it <= ' ' }.length != 0) return uuid
            uuid = UUID.randomUUID().toString()
            uuid = Base64.encodeToString(
                uuid.toByteArray(),
                Base64.DEFAULT
            )
            sharedUtil.saveStringValue(KEY_UUID, uuid)
            return uuid
        }

        fun decodeToBitMap(data: ByteArray?, _camera: Camera): Bitmap? {
            val size = _camera.parameters.previewSize
            try {
                val image =
                    YuvImage(data, ImageFormat.NV21, size.width, size.height, null)
                if (image != null) {
                    val stream = ByteArrayOutputStream()
                    image.compressToJpeg(
                        Rect(0, 0, size.width, size.height),
                        80,
                        stream
                    )
                    val bmp =
                        BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
                    stream.close()
                    return bmp
                }
            } catch (ex: java.lang.Exception) {
            }
            return null
        }

        fun isGoneKeyBoard(activity: Activity) {
            if (activity.currentFocus != null) {
                // 隐藏软键盘
                (activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(
                        activity.currentFocus!!
                            .windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
            }
        }

        var wakeLock: WakeLock? = null

        fun acquireWakeLock(context: Context) {
            if (wakeLock == null) {
                val powerManager = context
                    .getSystemService(Context.POWER_SERVICE) as PowerManager
                wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_DIM_WAKE_LOCK, "OpenGLDemo:wakelock"
                )
                wakeLock?.acquire()
            }
        }

        fun releaseWakeLock() {
            if (wakeLock != null && wakeLock!!.isHeld) {
                wakeLock!!.release()
                wakeLock = null
            }
        }

        /**
         * 获取 bitmap 灰度
         */

        fun getGrayscale(bitmap: Bitmap?): ByteArray? {
            if (bitmap == null) return null
            val ret = ByteArray(bitmap.width * bitmap.height)
            for (j in 0 until bitmap.height) for (i in 0 until bitmap.width) {
                val pixel = bitmap.getPixel(i, j)
                val red = pixel and 0x00FF0000 shr 16
                val green = pixel and 0x0000FF00 shr 8
                val blue = pixel and 0x000000FF
                ret[j * bitmap.width + i] = ((299 * red + (587
                        * green) + 114 * blue) / 1000).toByte()
            }
            return ret
        }

        fun convertYUV21FromRGB(bitmap: Bitmap): ByteArray? {
            var bitmap = bitmap
            bitmap = rotaingImageView(90, bitmap)!!
            val inputWidth = bitmap.width
            val inputHeight = bitmap.height
            val argb = IntArray(inputWidth * inputHeight)
            bitmap.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight)
            val yuv = ByteArray(inputWidth * inputHeight * 3 / 2)
            ConUtil.encodeYUV420SP(yuv, argb, inputWidth, inputHeight)
            bitmap.recycle()
            return yuv
        }

        fun encodeYUV420SP(
            yuv420sp: ByteArray,
            argb: IntArray,
            width: Int,
            height: Int
        ) {
            val frameSize = width * height
            var yIndex = 0
            var uvIndex = frameSize
            var a: Int
            var R: Int
            var G: Int
            var B: Int
            var Y: Int
            var U: Int
            var V: Int
            var index = 0
            for (j in 0 until height) {
                for (i in 0 until width) {
                    a = argb[index] and -0x1000000 shr 24 // a is not used obviously
                    R = argb[index] and 0xff0000 shr 16
                    G = argb[index] and 0xff00 shr 8
                    B = argb[index] and 0xff shr 0

                    // well known RGB to YUV algorithm
                    Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
                    U = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128
                    V = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128

                    // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                    //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                    //    pixel AND every other scanline.
                    yuv420sp[yIndex++] =
                        (if (Y < 0) 0 else if (Y > 255) 255 else Y).toByte()
                    if (j % 2 == 0 && index % 2 == 0) {
                        yuv420sp[uvIndex++] =
                            (if (V < 0) 0 else if (V > 255) 255 else V).toByte()
                        yuv420sp[uvIndex++] =
                            (if (U < 0) 0 else if (U > 255) 255 else U).toByte()
                    }
                    index++
                }
            }
        }

        fun getFileContent(context: Context, id: Int): ByteArray? {
            var inputStream: InputStream? = null
            val byteArrayOutputStream =
                ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var count = -1
            try {
                inputStream = context.resources.openRawResource(id)
                while (inputStream.read(buffer).also { count = it } != -1) {
                    byteArrayOutputStream.write(buffer, 0, count)
                }
                byteArrayOutputStream.close()
            } catch (e: IOException) {
                return null
            } finally {
                // closeStreamSilently(inputStream);
                inputStream = null
            }
            return byteArrayOutputStream.toByteArray()
        }

        fun showToast(context: Context?, str: String?) {
            if (context != null) {
                val toast = Toast.makeText(context, str, Toast.LENGTH_SHORT)
                // 可以控制toast显示的位置
                toast.setGravity(Gravity.TOP, 0, 30)
                toast.show()
            }
        }

        fun showLongToast(
            context: Context?,
            str: String?
        ) {
            if (context != null) {
                val toast = Toast.makeText(context, str, Toast.LENGTH_LONG)
                // 可以控制toast显示的位置
                toast.setGravity(Gravity.TOP, 0, 30)
                toast.show()
            }
        }

        fun getVersionName(context: Context): String? {
            return try {
                context.packageManager.getPackageInfo(
                    context.packageName, 0
                ).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                null
            }
        }

        /**
         * 镜像翻转
         */
        fun convert(bitmap: Bitmap, mIsFrontalCamera: Boolean): Bitmap? {
            val w = bitmap.width
            val h = bitmap.height
            val newbBitmap =
                Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888) // 创建一个新的和SRC长度宽度一样的位图
            val cv = Canvas(newbBitmap)
            val m = Matrix()
            // m.postScale(1, -1); //镜像垂直翻转
            if (mIsFrontalCamera) {
                m.postScale(-1f, 1f) // 镜像水平翻转
            }
            //		m.postRotate(-90); //旋转-90度
            val bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, w, h, m, true)
            cv.drawBitmap(
                bitmap2,
                Rect(0, 0, bitmap2.width, bitmap2.height),
                Rect(0, 0, w, h), null
            )
            return newbBitmap
        }

        fun readYUVInfo(ctx: Context?): ByteArray? {
            val path: String = getDiskCachePath(ctx!!)!!
            val pathName = "$path/yuv.img"
            val file = File(pathName)
            if (!file.exists()) return null
            var inputStream: InputStream? = null
            val byteArrayOutputStream =
                ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var count = -1
            try {
                inputStream = FileInputStream(file)
                while (inputStream.read(buffer).also { count = it } != -1) {
                    byteArrayOutputStream.write(buffer, 0, count)
                }
                byteArrayOutputStream.close()
            } catch (e: IOException) {
                return null
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            return byteArrayOutputStream.toByteArray()
        }

        fun saveYUVInfo(ctx: Context?, arr: ByteArray?) {
            if (arr == null) return
            val path: String = getDiskCachePath(ctx!!)!!
            val pathName = "$path/yuv.img"
            val file = File(pathName)
            var fileOutputStream: FileOutputStream? = null
            try {
                fileOutputStream = FileOutputStream(file)
                fileOutputStream.write(arr)
                fileOutputStream.flush()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }


        fun saveBitmap(
            mContext: Context,
            bitmaptosave: Bitmap?
        ): String? {
            if (bitmaptosave == null) return null
            val mediaStorageDir = mContext.getExternalFilesDir("megvii")
            if (!mediaStorageDir!!.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null
                }
            }
            // String bitmapFileName = System.currentTimeMillis() + ".jpg";
            val bitmapFileName = System.currentTimeMillis().toString() + ""
            var fos: FileOutputStream? = null
            return try {
                fos = FileOutputStream("$mediaStorageDir/$bitmapFileName")
                val successful = bitmaptosave.compress(
                    Bitmap.CompressFormat.JPEG, 75, fos
                )
                if (successful) mediaStorageDir.absolutePath + "/" + bitmapFileName else null
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                null
            } finally {
                try {
                    fos!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun revitionImage(path: String?, width: Int, height: Int): Bitmap? {
            if (null == path || TextUtils.isEmpty(path) || !File(path)
                    .exists()
            ) return null
            var `in`: BufferedInputStream? = null
            return try {
                // 获取到图片的旋转属性
                val degree: Int = ConUtil.readPictureDegree(path)
                `in` = BufferedInputStream(FileInputStream(File(path)))
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(`in`, null, options)
                // 计算出图片的缩放比例
                options.inSampleSize = ConUtil.calculateInSampleSize(options, width, height)
                `in`.close()
                `in` = BufferedInputStream(FileInputStream(File(path)))
                options.inJustDecodeBounds = false
                val bitmap = BitmapFactory.decodeStream(`in`, null, options)
                ConUtil.rotaingImageView(degree, bitmap)
            } catch (e: java.lang.Exception) {
                null
            } finally {
                if (null != `in`) {
                    try {
                        `in`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    `in` = null
                }
            }
        }

        fun readPictureDegree(path: String?): Int {
            var degree = 0
            try {
                val exifInterface = ExifInterface(path)
                val orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                }
            } catch (e: IOException) {
//			Logger.getLogger(PhotoHelper.class).e(e.getMessage());
            }
            return degree
        }

        fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int, reqHeight: Int
        ): Int {
            // 源图片的高度和宽度
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {
                // 计算出实际宽高和目标宽高的比率
                val heightRatio =
                    Math.round(height.toFloat() / reqHeight.toFloat())
                val widthRatio =
                    Math.round(width.toFloat() / reqWidth.toFloat())
                // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
                // 一定都会大于等于目标的宽和高。
                inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
            }
            return inSampleSize
        }

        fun rotaingImageView(angle: Int, bitmap: Bitmap?): Bitmap? {
            if (null == bitmap) {
                return null
            }
            // 旋转图片 动作
            val matrix = Matrix()
            matrix.postRotate(angle.toFloat())
            // 创建新的图片
            return Bitmap.createBitmap(
                bitmap, 0, 0,
                bitmap.width, bitmap.height, matrix, true
            )
        }

        fun getDiskCachePath(context: Context): String? {
            return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
                context.externalCacheDir!!.path
            } else {
                context.cacheDir.path
            }
        }

        fun getSDRootPath(): String? {
            return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
                Environment.getExternalStorageDirectory().path
            } else {
                null
            }
        }

        fun dip2px(context: Context, dipValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dipValue * scale + 0.5f).toInt()
        }

        fun px2dip(context: Context, pxValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (pxValue / scale + 0.5f).toInt()
        }


    }
}
