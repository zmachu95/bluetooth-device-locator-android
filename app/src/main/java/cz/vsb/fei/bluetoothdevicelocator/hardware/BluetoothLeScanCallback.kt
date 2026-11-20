package cz.vsb.fei.bluetoothdevicelocator.hardware

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.content.Context
import cz.vsb.fei.bluetoothdevicelocator.R
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Callback object for Bluetooth Low Energy scans.
 */
class BluetoothLeScanCallback(

    /**
     * Flow with nearby devices.
     */
    private val devicesFlow: MutableStateFlow<Map<String, DeviceData>>,

    /**
     * Callback which checks whether Bluetooth connect permission is granted.
     */
    private val hasBluetoothConnectPermission: () -> Boolean,

    /**
     * Callback to invoke upon scan stop.
     */
    private val onScanStopped: () -> Unit,

    private val context: Context

) : android.bluetooth.le.ScanCallback() {

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        if (result != null) storeDevice(result)
    }

    override fun onBatchScanResults(results: List<ScanResult?>?) {
        if (results == null) return
        for (result in results) {
            if (result != null) storeDevice(result)
        }
    }

    override fun onScanFailed(errorCode: Int) = onScanStopped()

    /**
     * Stores device data into the flow.
     * @param result Scan result with device data.
     */
    private fun storeDevice(result: ScanResult) {
        // extract data about device
        val deviceMacAddress: String = result.device.address
        val rssi: Float = result.rssi.toFloat()
        val deviceName: String? = getDeviceName(result)

        // insert data into the flow
        devicesFlow.update { currentDevicesMap ->
            updateDevicesFlow(currentDevicesMap, deviceMacAddress, rssi, deviceName)
        }
    }

    /**
     * Tries to get device's name from supplied scan result.
     * @param result Scan result to get the device name from.
     * @return If found, the device's name; otherwise returns null.
     */
    @SuppressLint("MissingPermission")
    private fun getDeviceName(result: ScanResult): String? {
        val deviceName: String? = if (hasBluetoothConnectPermission())
            result.device.name?.takeIf(String::isNotBlank) else null
        val scanRecordName: String? = result.scanRecord?.deviceName?.takeIf(String::isNotBlank)
        return deviceName ?: scanRecordName
    }

    /**
     * Updates the flow with the new device data.
     * @param currentDevicesMap Current devices map obtained from the flow.
     * @param deviceMacAddress Device's MAC address.
     * @param rssi Received signal strength indication in (dBm).
     * @param deviceName Device's name.
     * @return Updated devices map.
     */
    private fun updateDevicesFlow(currentDevicesMap: Map<String, DeviceData>,
                                  deviceMacAddress: String,
                                  rssi: Float,
                                  deviceName: String?): Map<String, DeviceData> {
        // favor already stored name over "unknown device"
        val storedDeviceName: String? = currentDevicesMap[deviceMacAddress]?.name
        val finalDeviceName: String = deviceName
            ?: storedDeviceName
            ?: context.resources.getString(R.string.unknown_device_name)

        val deviceData = DeviceData(finalDeviceName, deviceMacAddress, rssi)
        return currentDevicesMap + (deviceMacAddress to deviceData)
    }

}