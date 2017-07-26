package com.example.alishaikh.rc_test.HardwareDrivers


import android.util.Log
import java.io.IOException

/**
 * Created by alishaikh on 7/10/17.
 */

class AdafruitMotorShieldV1Driver(mArduino: ArduinoUno) : ArduinoShieldInterface(mArduino) {
    init {

    }
    enum class AFMotor (val motorNum: Int)
    {DC_MOTOR_1(1), DC_MOTOR_2(2), DC_MOTOR_3(3), DC_MOTOR_4(4)}

    enum class AFMotorDirection (val direction: Int)
    {STOP(0), FORWARD(1), BACKWARD(2)}


    fun setDcMotor(motorNum: AFMotor, direction: AFMotorDirection, speed: Int) {
        var command = String.format("M%dD%dS%d", motorNum, direction, speed)

        try {
            sendCommand(command)
        } catch (e: IOException) {
            try {
                close()
            } catch (e1: Exception) {
                e1.printStackTrace()
            }

        }

    }

    //TODO: Add Stepper comnand
    @Throws(UnsupportedOperationException::class)
    fun setStepperMotor() {
    }

    @Throws(IOException::class)
    private fun sendCommand(command: String) {
        // TODO: MUST REWORK - Terrible code
        val commandPlusX = command + "x" // x added currently to get around state machine code on Ad, must be reworked
        arduinoUno.sendCommand(commandPlusX)
    }

    @Throws(Exception::class)
    override fun close() {
        arduinoUno.close()
    }

}

