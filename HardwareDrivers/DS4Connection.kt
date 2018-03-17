package com.example.alishaikh.rc_test.HardwareDrivers

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.TextUtils
import android.util.Log
import com.google.android.things.bluetooth.*


import com.google.android.things.bluetooth.BluetoothProfile.AVRCP_CONTROLLER
import com.google.android.things.bluetooth.BluetoothProfile.A2DP_SINK
import com.google.android.things.bluetooth.ConnectionParams
import com.google.android.things.bluetooth.BluetoothConnectionCallback
import com.google.android.things.bluetooth.BluetoothProfile.A2DP_SINK




/**
 * Created by alishaikh on 7/26/17.
 */
class DS4Connection constructor(val context: Context){

    val TAG = this::class.simpleName
//    private var context = context
//private var deviceAddress = "00:1A:E9:50:CE:CB"//"F0:DF:0E:BA:BA:2C"
        //"04:03:D6:43:A7:94"

    private var deviceAddress = "00:01:6C:87:67:B2"


     var connectionManager: BluetoothConnectionManager
    private var bluetoothAdapter :BluetoothAdapter

    init {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        connectionManager = BluetoothConnectionManager.getInstance()

        val profileManager = BluetoothProfileManager.getInstance()
        profileManager.enableProfiles(listOf(4))
        connectionManager.registerConnectionCallback(object : BluetoothConnectionCallback {
            override fun onConnectionRequested(bluetoothDevice: BluetoothDevice?, connectionParams: ConnectionParams?) {
                // Handle incoming connection request
                handleConnectionRequest(bluetoothDevice!!, connectionParams!!)
            }

            override fun onConnectionRequestCancelled(bluetoothDevice: BluetoothDevice?, requestType: Int) {
                // Request cancelled
            }

            override fun onConnected(bluetoothDevice: BluetoothDevice?, profile: Int) {
                // Connection completed successfully
            }

            override fun onDisconnected(bluetoothDevice: BluetoothDevice?, profile: Int) {
                // Remote device disconnected
            }
        })

        if (!isEnabled()) {
            val enabled = bluetoothAdapter.enable()
            Log.d(TAG,"Bluetooth isn't enabled, enabling: $enabled")
        }


    }

    fun isEnabled(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    fun getPairedDevices(): Set<BluetoothDevice> {
        return bluetoothAdapter.bondedDevices
    }

    /**
     * Checks whether the device is already paired.
     */
    fun isPaired(): BluetoothDevice? {
        var res: BluetoothDevice? = null
        val pairedDevices = getPairedDevices()
        for (pairedDevice in pairedDevices) {
            if (isKnownDevice(pairedDevice.address)) res = pairedDevice
        }

        return res
    }



    fun startDiscovery(): Boolean {
        registerReceiver()
        return bluetoothAdapter.startDiscovery()
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            // Discovery has found a device.
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Get the BluetoothDevice object and its info from the Intent.
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val bondState = device.bondState
                    val foundName = device.name
                    val foundAddress = device.address // MAC address
                    Log.d(TAG, "Discovery has found a device: $bondState $foundName $foundAddress")
                    if (foundName != null && isKnownDevice(foundAddress)) {
                        var result = createBond(device)
                        Log.d(TAG, "Bond attempt status: $result")

                    } else {
                        Log.d(TAG, "Unknown device, skipping bond attempt.")
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                    when (state) {
                        BluetoothDevice.BOND_NONE ->
                            Log.d(TAG, "The remote device is not bonded.")

                        BluetoothDevice.BOND_BONDING ->
                            Log.d(TAG, "Bonding is in progress with the remote device.")

                        BluetoothDevice.BOND_BONDED ->
                            Log.d(TAG, "The remote device is bonded.")

                        else ->
                            Log.d(TAG, "Unknown remote device bonding state.")

                    }
                }
            }
        }
    }

    private fun registerReceiver() {
        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)

        context.registerReceiver(receiver, filter)


    }

    fun cancelDiscovery() {
        bluetoothAdapter.cancelDiscovery()
        context.unregisterReceiver(receiver)
    }

    private fun isKnownDevice(foundAddress: String): Boolean {
        return (!TextUtils.isEmpty(deviceAddress) && deviceAddress == foundAddress)
    }

    /**
     * Pair with the specific device.
     */
    fun createBond(device: BluetoothDevice): Boolean {

//        device.setPin("0000".toByteArray())
        val result = device.createBond()
        Log.d(TAG,"Creating bond with:  ${device.name} ${device.address} $result")
        return result
    }


    fun connect(device:BluetoothDevice): Boolean {
        var res = false

        connectToA2dp(device)


        return res
    }

    private fun connectToA2dp(bluetoothDevice: BluetoothDevice) {
        connectionManager.connect(bluetoothDevice, 4)
    }



    private fun handleConnectionRequest(bluetoothDevice: BluetoothDevice, connectionParams: ConnectionParams) {
        // Determine whether to accept the connection request
        var accept = false
        if (connectionParams.requestType == ConnectionParams.REQUEST_TYPE_PROFILE_CONNECTION) {
            accept = true
        }

        // Pass that result on to the BluetoothConnectionManager
        connectionManager.confirmOrDenyConnection(bluetoothDevice, connectionParams, accept)
    }


}