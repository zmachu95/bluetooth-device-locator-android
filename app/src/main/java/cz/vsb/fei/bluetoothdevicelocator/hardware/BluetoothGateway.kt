package cz.vsb.fei.bluetoothdevicelocator.hardware

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Enables communication with Bluetooth.
 */
class BluetoothGateway(private val context: Context) {
    companion object {
        /**
         * Checks whether the running device has Android 12 or higher.
         * @return True if the device has Android 12 or higher; otherwise false.
         */
        fun hasDeviceAtleastAndroid12(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        /**
         * Gets array of permissions required for correct Bluetooth functionality.
         */
        fun getBluetoothPermissions(): Array<String> {
            return if (hasDeviceAtleastAndroid12()) {
                arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    /**
     * Backing field for foundDevices property.
     */
    private val _foundDevices = MutableStateFlow<Map<String, DeviceData>>(emptyMap())

    /**
     * Backing field for scanState property.
     */
    private val _scanState = MutableStateFlow(BluetoothScanState.NOT_ACTIVE)

    /**
     * Running device's Bluetooth manager.
     */
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(BluetoothManager::class.java)

    /**
     * Running device's Bluetooth adapter.
     */
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    /**
     * Receiver object for classic Bluetooth scanning.
     */
    private val bluetoothClassicReceiver = BluetoothClassicReceiver(
        devicesFlow = _foundDevices,
        hasBluetoothConnectPermission = this::hasBluetoothConnectPermission,
        onDiscoveryFinished = this::onDiscoveryFinished,
        onTurnedOff = this::onBluetoothTurnedOff,
        onTurnedOn = this::onBluetoothTurnedOn,
        context = context
    )

    /**
     * Callback object for Bluetooth LE scanning.
     */
    private val bluetoothLeScannerCallback: BluetoothLeScanCallback =
        BluetoothLeScanCallback(
            devicesFlow =  _foundDevices,
            hasBluetoothConnectPermission = this::hasBluetoothConnectPermission,
            onScanStopped = {
                Log.e(BluetoothGateway::class.simpleName, "LE scan failed!")
                stopScan()
            },
            context = context
        )

    /**
     * "Hot" flow with nearby devices.
     */
    val foundDevices: StateFlow<Map<String, DeviceData>> = _foundDevices

    /**
     * Flag whether receiver object for classic Bluetooth scanning is currently registered.
     */
    private var isClassicReceiverRegistered: Boolean = false

    /**
     * "Hot" flow with Bluetooth scanning state.
     */
    val scanState: StateFlow<BluetoothScanState> = _scanState

    /**
     * Checks whether Bluetooth is enabled.
     * @return True if Bluetooth is enabled; otherwise false.
     */
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    /**
     * Starts Bluetooth scanning for nearby devices.
     */
    fun startScan(isContinuous: Boolean) {
        if (_scanState.value.isActive || !hasBluetoothScanPermission()) return
        if (!isContinuous && !isBluetoothEnabled()) return

        _foundDevices.update { emptyMap() }
        _scanState.update { if (isContinuous)
            BluetoothScanState.CONTINUOUS_SCAN else BluetoothScanState.SINGLE_SCAN }
        startBluetoothClassicScanning(true)
        startBluetoothLeScanning()
    }

    /**
     * Stops Bluetooth scanning.
     */
    fun stopScan() {
        stopBluetoothClassicScanning(true)
        stopBluetoothLeScanning()
        unregisterBluetoothClassicReceiver()

        if (_scanState.value == BluetoothScanState.CONTINUOUS_SCAN) {
            /* clean the devices list if using continuous scanning to prevent showing
               the devices in Nearby Devices screen */
            _foundDevices.update { emptyMap() }
        }
        _scanState.update { BluetoothScanState.NOT_ACTIVE }
    }

    /**
     * Checks whether the running device has Bluetooth connect permission.
     * @return True if the device has Bluetooth connect permission; otherwise false.
     */
    fun hasBluetoothConnectPermission() : Boolean {
        return if (hasDeviceAtleastAndroid12()) {
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(Manifest.permission.BLUETOOTH) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Checks whether the running device has Bluetooth scan permission.
     * @return True if the device has Bluetooth scan permission; otherwise false.
     */
    fun hasBluetoothScanPermission() : Boolean {
        return if (hasDeviceAtleastAndroid12()) {
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Callback called when classic Bluetooth discovery finishes.
     */
    private fun onDiscoveryFinished() {
        if (_scanState.value == BluetoothScanState.CONTINUOUS_SCAN) {
            /* clear the map to ensure no far-away or disabled devices are present there */
            _foundDevices.update { emptyMap() }
            startBluetoothClassicScanning(false)
        } else {
            stopScan()
        }
    }

    /**
     * Callback called when Bluetooth is turned off.
     */
    private fun onBluetoothTurnedOff() {
        if (_scanState.value == BluetoothScanState.CONTINUOUS_SCAN) {
            /* clear the map to ensure no wrong data is present */
            _foundDevices.update { emptyMap() }
            stopBluetoothClassicScanning(false)
            stopBluetoothLeScanning()
        } else {
            stopScan()
        }
    }

    /**
     * Callback called when Bluetooth is turned on.
     */
    private fun onBluetoothTurnedOn() {
        if (_scanState.value == BluetoothScanState.CONTINUOUS_SCAN) {
            startBluetoothClassicScanning(false)
            startBluetoothLeScanning()
        }
    }

    /**
     * Registers receiver object for classic Bluetooth scanning.
     */
    private fun registerBluetoothClassicReceiver() {
        if (!isClassicReceiverRegistered) {
            Log.i(
                BluetoothGateway::class.simpleName,
                "Registered Bluetooth classic receiver.")
            val filter = android.content.IntentFilter().apply {
                addAction(android.bluetooth.BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            }
            ContextCompat.registerReceiver(
                context,
                bluetoothClassicReceiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
            isClassicReceiverRegistered = true
        }
    }

    /**
     * Starts classic Bluetooth scanning.
     * @param registerReceiver Flag whether to register the receiver object.
     */
    @SuppressLint("MissingPermission")
    private fun startBluetoothClassicScanning(registerReceiver: Boolean) {
        if (bluetoothAdapter == null || !hasBluetoothScanPermission()) return

        if (registerReceiver) {
            unregisterBluetoothClassicReceiver()
            registerBluetoothClassicReceiver()
        }
        if (isBluetoothEnabled()) {
            try {
                bluetoothAdapter.startDiscovery()
                Log.i(
                    BluetoothGateway::class.simpleName,
                    "Started Bluetooth classic scan...")
            } catch (_: Exception) {
                Log.e(
                    BluetoothGateway::class.simpleName,
                    "Failed to start Bluetooth classic scan!")
            }
        }
    }

    /**
     * Starts Bluetooth LE scanning.
     */
    @SuppressLint("MissingPermission")
    private fun startBluetoothLeScanning() {
        if (!isBluetoothEnabled() || !hasBluetoothScanPermission()) return
        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner ?: return
        val scanSettings: ScanSettings = ScanSettings.Builder()
            .setScanMode(SCAN_MODE_LOW_LATENCY).build()

        try {
            bluetoothLeScanner.startScan(null, scanSettings, bluetoothLeScannerCallback)
            Log.i(
                BluetoothGateway::class.simpleName,
                "Started Bluetooth LE scan...")
        } catch (_: Exception) {
            Log.e(
                BluetoothGateway::class.simpleName,
                "Failed to start Bluetooth LE scan!")
        }
    }

    /**
     * Stops classic Bluetooth scanning.
     * @param unregisterReceiver Flag whether to unregister the receiver object.
     */
    @SuppressLint("MissingPermission")
    private fun stopBluetoothClassicScanning(unregisterReceiver: Boolean) {
        if (bluetoothAdapter == null || !hasBluetoothScanPermission()) return

        try {
            if (unregisterReceiver) unregisterBluetoothClassicReceiver()
            bluetoothAdapter.cancelDiscovery()
            Log.i(
                BluetoothGateway::class.simpleName,
                "Stopped Bluetooth classic scan...")
        } catch (_: Exception) {
            Log.w(
                BluetoothGateway::class.simpleName,
                "Failed to stop Bluetooth classic scan!")
        }
    }

    /**
     * Stops Bluetooth LE scanning.
     */
    @SuppressLint("MissingPermission")
    private fun stopBluetoothLeScanning() {
        if (!hasBluetoothScanPermission()) return
        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner ?: return

        try {
            bluetoothLeScanner.stopScan(bluetoothLeScannerCallback)
            Log.i(
                BluetoothGateway::class.simpleName,
                "Stopped Bluetooth LE scan...")
        } catch (_: Exception) {
            Log.w(
                BluetoothGateway::class.simpleName,
                "Failed to stop Bluetooth LE scan!")
        }
    }

    /**
     * Unregisters receiver object for classic Bluetooth scanning.
     */
    private fun unregisterBluetoothClassicReceiver() {
        if (isClassicReceiverRegistered) {
            Log.i(
                BluetoothGateway::class.simpleName,
                "Unregistered Bluetooth classic receiver.")
            context.unregisterReceiver(bluetoothClassicReceiver)
            isClassicReceiverRegistered = false
        }
    }

}
