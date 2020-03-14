package com.jaryd.facepp.utils

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorEventUtil(context: Context) : SensorEventListener {
    private val mSensorManager: SensorManager
    private val mSensor: Sensor
    var orientation = 0
    override fun onAccuracyChanged(
        sensor: Sensor,
        accuracy: Int
    ) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        val G = 9.81
        val SQRT2 = 1.414213
        if (event.sensor == null) {
            return
        }
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            orientation = if (z >= G / SQRT2) { //screen is more likely lying on the table
                if (x >= G / 2) {
                    1
                } else if (x <= -G / 2) {
                    2
                } else if (y <= -G / 2) {
                    3
                } else {
                    0
                }
            } else {
                if (x >= G / SQRT2) {
                    1
                } else if (x <= -G / SQRT2) {
                    2
                } else if (y <= -G / SQRT2) {
                    3
                } else {
                    0
                }
            }
        }
    }

    init {
        mSensorManager =
            context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensor =
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) // TYPE_GRAVITY
        // 参数三，检测的精准度
        mSensorManager.registerListener(
            this,
            mSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        ) // SENSOR_DELAY_GAME
    }
}