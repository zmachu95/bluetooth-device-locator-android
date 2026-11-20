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
     * Backing field for isScanning property.
     */
    private val _isScanning = MutableStateFlow<Boolean>(false)

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
        onScanStopped = this::stopScan,
        context = context
    )

    /**
     * Callback object for Bluetooth LE scanning.
     */
    private val bluetoothLeScannerCallback: BluetoothLeScanCallback =
        BluetoothLeScanCallback(
            devicesFlow =  _foundDevices,
            hasBluetoothConnectPermission = this::hasBluetoothConnectPermission,
            onScanStopped = this::stopScan,
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
     * Flag whether Bluetooth scanning is currently in progress.
     */
    val isScanning: StateFlow<Boolean> = _isScanning

    /**
     * Checks whether Bluetooth is enabled.
     * @return True if Bluetooth is enabled; otherwise false.
     */
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    /**
     * Starts Bluetooth scanning for nearby devices.
     */
    @SuppressLint("MissingPermission")
    fun startScan() {
        if (_isScanning.value || !isBluetoothEnabled() || !hasBluetoothScanPermission()) return
        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner ?: return

        // erase previously found devices
        _foundDevices.update { emptyMap() }
        _isScanning.update { true }

        // start classic Bluetooth scanning
        if (isClassicReceiverRegistered) unregisterBluetoothClassicReceiver()
        registerBluetoothClassicReceiver()
        bluetoothAdapter.startDiscovery()

        // start Bluetooth LE scanning
        val scanSettings: ScanSettings = ScanSettings.Builder()
            .setScanMode(SCAN_MODE_LOW_LATENCY).build()
        bluetoothLeScanner.startScan(null, scanSettings, bluetoothLeScannerCallback)
    }

    /**
     * Stops Bluetooth scanning.
     */
    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (hasBluetoothScanPermission() && bluetoothAdapter != null) {
            // cancel classic Bluetooth scanning
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }

            // cancel Bluetooth LE scanning
            bluetoothAdapter.bluetoothLeScanner?.stopScan(bluetoothLeScannerCallback)
        }

        unregisterBluetoothClassicReceiver()
        _isScanning.update { false }
    }

    /**
     * Checks whether the running device has Bluetooth connect permission.
     * @return True if the device has Bluetooth connect permission; otherwise false.
     */
    fun hasBluetoothConnectPermission() : Boolean {
        return if (hasDeviceAtleastAndroid12()) {
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Checks whether the running device has Bluetooth scan permission.
     * @return True if the device has Bluetooth scan permission; otherwise false.
     */
    fun hasBluetoothScanPermission() : Boolean {
        return if (hasDeviceAtleastAndroid12()) {
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Registers receiver object for classic Bluetooth scanning.
     */
    private fun registerBluetoothClassicReceiver() {
        if (!isClassicReceiverRegistered) {
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
     * Unregisters receiver object for classic Bluetooth scanning.
     */
    private fun unregisterBluetoothClassicReceiver() {
        if (isClassicReceiverRegistered) {
            context.unregisterReceiver(bluetoothClassicReceiver)
            isClassicReceiverRegistered = false
        }
    }

}