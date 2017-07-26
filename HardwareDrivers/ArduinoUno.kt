package com.example.alishaikh.rc_test.HardwareDrivers

/**
 * Created by alishaikh on 7/15/17.
 */


import android.app.Activity
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent

import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface

import java.io.UnsupportedEncodingException

class ArduinoUno : AutoCloseable{
    override fun close() {
        stopUsbConnection()

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private var usbManager: UsbManager? = null
    private var connection: UsbDeviceConnection? = null
    private var serialDevice: UsbSerialDevice? = null
    private var buffer = ""

    private val callback = UsbSerialInterface.UsbReadCallback { data ->
        try {
            val dataUtf8 = String(data) // Need to check for UTF-8
            buffer += dataUtf8
            var index: Int
            index =  buffer.indexOf('\n')
            while (index != -1) {
                val dataStr = buffer.substring(0, index + 1).trim { it <= ' ' }
                buffer = if (buffer.length == index) "" else buffer.substring(index + 1)
//                runOnUiThread { onSerialDataReceived(dataStr) }
                onSerialDataReceived(dataStr)
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

     fun onCreate() {
//        super.onCreate(savedInstanceState)
//        usbManager = getSystemService(UsbManager::class.java)

        // Detach events are sent as a system-wide broadcast
//        val filter = IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED)
//        registerReceiver(usbDetachedReceiver, filter)

         startUsbConnection()

    }

     fun onRebind(intent: Intent?) {
//        super.onRebind(intent)
        startUsbConnection()
    }

     fun onDestroy() {
//        super.onDestroy()
//        unregisterReceiver(usbDetachedReceiver)
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




    companion object {

        private val TAG = ArduinoUno::class.java!!.getSimpleName()
        private val USB_VENDOR_ID = 0x2341 // 9025
        private val USB_PRODUCT_ID = 0x0043

        private fun isFireKey(keyCode: Int): Boolean {
            // Here we treat Button_A and DPAD_CENTER as the primary action
            // keys for the game.
            return keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_BUTTON_A
        }
    }

    fun sendCommand(command: String) {

        serialDevice?.write(command.toByteArray())

    }
}