package com.example.alishaikh.rc_test

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.example.alishaikh.rc_test.HardwareDrivers.RcCarController


import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface

import java.io.UnsupportedEncodingException

class MainActivity : Activity(){


    private var usbManager: UsbManager? = null
    private var connection: UsbDeviceConnection? = null
    private var serialDevice: UsbSerialDevice? = null
    private var buffer = ""

    var carController: RcCarController = RcCarController()


    private val callback = UsbSerialInterface.UsbReadCallback { data ->
        try {
            val dataUtf8 = String(data) // Need to check for UTF-8
            buffer += dataUtf8
            var index: Int
            index =  buffer.indexOf('\n')
            while (index != -1) {
                val dataStr = buffer.substring(0, index + 1).trim { it <= ' ' }
                buffer = if (buffer.length == index) "" else buffer.substring(index + 1)
                runOnUiThread { onSerialDataReceived(dataStr) }
                index =  buffer.indexOf('\n')
            }
        } catch (e: UnsupportedEncodingException) {
            Log.e(TAG, "Error receiving USB data", e)
        }
    }

    private val usbDetachedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                if (device != null && device.vendorId == USB_VENDOR_ID && device.productId == USB_PRODUCT_ID) {
                    Log.i(TAG, "USB device detached")
                    stopUsbConnection()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usbManager = getSystemService(UsbManager::class.java)

        // Detach events are sent as a system-wide broadcast
        val filter = IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(usbDetachedReceiver, filter)


    }

    override fun onResume() {
        super.onResume()
        startUsbConnection()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbDetachedReceiver)
        stopUsbConnection()
    }

    private fun startUsbConnection() {
        val connectedDevices = usbManager!!.deviceList

        if (!connectedDevices.isEmpty()) {
            for (device in connectedDevices.values) {
                if (device.vendorId == USB_VENDOR_ID && device.productId == USB_PRODUCT_ID) {
                    Log.i(TAG, "Device found: " + device.deviceName)
                    startSerialConnection(device)
                    return
                }
            }
        }
        Log.w(TAG, "Could not start USB connection - No devices found")
    }

    private fun startSerialConnection(device: UsbDevice) {
        Log.i(TAG, "Ready to open USB device connection")
        connection = usbManager!!.openDevice(device)
        serialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection)
        if (serialDevice != null) {
            if (serialDevice!!.open()) {
                serialDevice!!.setBaudRate(115200)
                serialDevice!!.setDataBits(UsbSerialInterface.DATA_BITS_8)
                serialDevice!!.setStopBits(UsbSerialInterface.STOP_BITS_1)
                serialDevice!!.setParity(UsbSerialInterface.PARITY_NONE)
                serialDevice!!.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                serialDevice!!.read(callback)
                Log.i(TAG, "Serial connection opened")
            } else {
                Log.w(TAG, "Cannot open serial connection")
            }
        } else {
            Log.w(TAG, "Could not create Usb Serial Device")
        }
    }

    private fun onSerialDataReceived(data: String) {
        // Add whatever you want here
        Log.i(TAG, "Serial data received: " + data)
    }

    private fun stopUsbConnection() {
        try {
            if (serialDevice != null) {
                serialDevice!!.close()
            }

            if (connection != null) {
                connection!!.close()
            }

        } finally {
            serialDevice = null
            connection = null
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        val commands =  carController.handleAnalogEvent(event)
        commands.forEach {
            serialDevice?.write(it.toByteArray())
        }

        return true
    }

    override fun onKeyDown(keyCode:Int, event: KeyEvent):Boolean {
        var handled = false
        if ((event.source and InputDevice.SOURCE_GAMEPAD) === InputDevice.SOURCE_GAMEPAD)
        {
            if (event.getRepeatCount() === 0)
            {
                when (keyCode) {
                    else -> if (isFireKey(keyCode))
                    {
                        handled = true
                        var command = "M2D1S200x"
                        serialDevice?.write(command.toByteArray())
//                        mDevice.writeUartData("M2D1S200x")

                    }// Update the ship object to fire lasers
                }// Handle gamepad and D-pad button presses to
                // navigate the ship
            }
            if (handled)
            {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }


    private fun isFireKey(keyCode: Int): Boolean {
        // Here we treat Button_B and DPAD_CENTER as the primary action
        // keys for the game.
        return keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_BUTTON_B
    }

    companion object {

        private val TAG = MainActivity::class.java!!.getSimpleName()
        private val USB_VENDOR_ID = 0x2341 // 9025
        private val USB_PRODUCT_ID = 0x0043


    }
}





/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
//class MainActivity : Activity() {
//
//    val manager = PeripheralManagerService()
//    val TAG = "MainActivity"
//    lateinit var mDevice : UartDevice
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        val deviceList = manager.uartDeviceList
//        if (deviceList.isEmpty()) {
//            Log.i("UartCheck", "No UART port available on this device.")
//        } else {
//            Log.i("UartCheck", "List of available devices: " + deviceList)
//        }
//
//        try {
//            mDevice = manager.openUartDevice("UART0")
//        } catch (e: IOException) {
//            Log.w(TAG, "Unable to access UART device", e)
//        }
//        configureUartFrame(mDevice)
//
//
//
//    }
//
//    override fun onKeyDown(keyCode:Int, event: KeyEvent):Boolean {
//        var handled = false
//        if ((event.getSource() and InputDevice.SOURCE_GAMEPAD) === InputDevice.SOURCE_GAMEPAD)
//        {
//            if (event.getRepeatCount() === 0)
//            {
//                when (keyCode) {
//                    else -> if (isFireKey(keyCode))
//                    {
//                        handled = true
//                        mDevice.writeUartData("M2D1S200x")
//
//                    }// Update the ship object to fire lasers
//                }// Handle gamepad and D-pad button presses to
//                // navigate the ship
//            }
//            if (handled)
//            {
//                return true
//            }
//        }
//        return super.onKeyDown(keyCode, event)
//    }
//
//
//    private fun isFireKey(keyCode: Int): Boolean {
//        // Here we treat Button_B and DPAD_CENTER as the primary action
//        // keys for the game.
//        return keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_BUTTON_B
//    }
//
//    override fun onDestroy() {
//        super.onDestroy();
//
//        if (mDevice != null) {
//            try {
//                mDevice.close();
//            } catch (e: IOException) {
//                Log.w("MainActivity", "Unable to close UART device", e);
//            }
//        }
//    }
//
//    @Throws(IOException::class)
//    fun configureUartFrame(uart: UartDevice) {
//        // Configure the UART port
//        uart.setBaudrate(115200)
//        uart.setDataSize(8)
//        uart.setParity(UartDevice.PARITY_NONE)
//        uart.setStopBits(1)
//    }
//
//    @Throws(IOException::class)
//    fun UartDevice.writeUartData(data: String) {
//        val buffer = data.toByteArray()
//        var count = this.write(buffer, buffer.size)
//        Log.d("MainActivity", "Wrote " + count + " bytes to peripheral");
//    }
//}
