package com.example.alishaikh.rc_test.HardwareDrivers

/**
 * Created by alishaikh on 7/15/17.
 */
open class ArduinoShieldInterface(val arduinoUno: ArduinoUno) : AutoCloseable{
//    var mArduino : ArduinoUno

    init {
//        mArduino = arduinoUno
        arduinoUno.onCreate()

    }

    override fun close() {
        arduinoUno.onDestroy()
    }
}