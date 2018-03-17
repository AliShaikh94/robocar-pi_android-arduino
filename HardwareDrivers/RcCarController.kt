package com.example.alishaikh.rc_test.HardwareDrivers

import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent

/**
 * Created by alishaikh on 7/19/17.
 */
class RcCarController {
    var mOldLeftThumbstickX = 0.0f
    var mOldLeftThumbstickY = 0.0f
    var mOldRightThumbstickX = 0.0f
    var mOldRightThumbstickY = 0.0f

    var mOldBrake = 0.0f
    var mOldGas = 0.0f

    val dpad = Dpad()

    fun handleAnalogEvent(event: MotionEvent): List<String> {

        var commands = mutableListOf<String>()
        val eventSource = event.source
        var handled = false
//
//        if(Dpad.isDpadDevice(event)) {
//            var command: String
//            val press = dpad.getDirectionPressed(event)
//            when (press) {
//                Dpad.LEFT ->  command = setDcMotor(
//                    AFMotor.DC_MOTOR_4,
//                    AFMotorDirection.BACKWARD ,
//                    (255f).toInt())
//                Dpad.RIGHT ->  command = setDcMotor(
//                        AFMotor.DC_MOTOR_4,
//                        AFMotorDirection.FORWARD ,
//                        (255f).toInt())
//                else -> command = setDcMotor(
//                        AFMotor.DC_MOTOR_4,
//                        AFMotorDirection.STOP ,
//                        (0f).toInt())
//            }
//            Log.d("DPAD Event", " Command: " + command)
//            commands.add(command)
//        }
//
//        else
            if (eventSource and InputDevice.SOURCE_GAMEPAD === InputDevice.SOURCE_GAMEPAD || eventSource and InputDevice.SOURCE_JOYSTICK === InputDevice.SOURCE_JOYSTICK) {




            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                var newAXIS_LX = event.getAxisValue(MotionEvent.AXIS_X)
                val absNewAXIS_LX = Math.abs(newAXIS_LX)
//                if(absNewAXIS_LX < 0.05)
//                    newAXIS_LX = 0f
                if (newAXIS_LX.compareTo(mOldLeftThumbstickX) !=0) {
                    mOldLeftThumbstickX = newAXIS_LX
                    handled = true
                    Log.d("Analog Event", "LX: " + newAXIS_LX.toString())
                    val command: String = if(absNewAXIS_LX  < 0.05f) {
                        setDcMotor(
                                AFMotor.DC_MOTOR_4,
                                AFMotorDirection.STOP ,
                                (0f).toInt())
                    } else setDcMotor(
                            AFMotor.DC_MOTOR_4,
                            if (newAXIS_LX < 0) AFMotorDirection.BACKWARD else AFMotorDirection.FORWARD,
                            (255f).toInt())
                    commands.add(command)

                    if(absNewAXIS_LX.toInt() == 0)
                        commands.add(command)
                    Log.d("Serial Event", "Serial Analog Command: " + command)

                }

                val newAXIS_LY = event.getAxisValue(MotionEvent.AXIS_Y)
//                if (newAXIS_LY.compareTo(mOldLeftThumbstickY) !=0) {
//                    mOldLeftThumbstickY = newAXIS_LY
//                    handled = true
//                    Log.d("Analog Event", "LY: " + newAXIS_LY.toString())
//                }
//
                val newAXIS_RX = event.getAxisValue(MotionEvent.AXIS_Z)
//                if (newAXIS_RX.compareTo(mOldRightThumbstickX) !=0) {
//                    mOldRightThumbstickX = newAXIS_RX
//                    handled = true
//                    Log.d("Analog Event", "RX: " + newAXIS_RX.toString())
//                }
//
                val newAXIS_RY = event.getAxisValue(MotionEvent.AXIS_RZ)
//                if (newAXIS_RY.compareTo(mOldRightThumbstickY) !=0) {
//                    mOldRightThumbstickY = newAXIS_RY
//                    handled = true
//                    Log.d("Analog Event", "RY: " + newAXIS_RY.toString())
//                }


                val newAXIS_BRAKE = event.getAxisValue(MotionEvent.AXIS_BRAKE)
//                val newAXIS_BRAKE = (event.getAxisValue(MotionEvent.AXIS_Z) +1)/2
                if (newAXIS_BRAKE.compareTo(mOldBrake) !=0) {
                    mOldBrake = newAXIS_BRAKE
                    handled = true
                    Log.d("Analog Event", "Brake: " + newAXIS_BRAKE.toString())
                    val command = setDcMotor(AFMotor.DC_MOTOR_2, AFMotorDirection.BACKWARD, (newAXIS_BRAKE*255f).toInt())
                    commands.add(command)
                    if(mOldBrake.toInt() == 0)
                        commands.add(command)
                    Log.d("Serial Event", "Serial Analog Command: " + command)
                }


                val newAXIS_GAS = event.getAxisValue(MotionEvent.AXIS_GAS)
//                val newAXIS_GAS = (event.getAxisValue(MotionEvent.AXIS_RZ) + 1)/2
                if (newAXIS_GAS.compareTo(mOldGas) !=0) {
                    mOldGas = newAXIS_GAS

                    handled = true
                    Log.d("Analog Event", "Gas: " + newAXIS_GAS.toString())
                    val command = setDcMotor(AFMotor.DC_MOTOR_2, AFMotorDirection.FORWARD, (newAXIS_GAS*255f).toInt())
                    commands.add(command)
                    if(mOldGas.toInt() == 0)
                        commands.add(command)

                    Log.d("Serial Event", "Serial Analog Command: " + command)
                }



            }
        }

        return commands
    }

    fun handleButtonPress(keyCode: Int) {
        when(keyCode) {

        }

    }

    enum class AFMotor (val motorNum: Int)
    {DC_MOTOR_1(1), DC_MOTOR_2(2), DC_MOTOR_3(3), DC_MOTOR_4(4)}

    enum class AFMotorDirection (val direction: Int)
    {STOP(0), FORWARD(1), BACKWARD(2)}


    fun setDcMotor(motorNum: AFMotor, direction: AFMotorDirection, speed: Int): String {
//        val command = String.format("M%dD%dS%d", motorNum, direction, speed)
        val command = "M${motorNum.motorNum}D${direction.direction}S$speed"
        return command
    }

//    fun handleAxisShift(axis)
}