package cz.vsb.fei.bluetoothdevicelocator.hardware

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import cz.vsb.fei.bluetoothdevicelocator.R
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Fallback value for RSSI.
 */
private const val RSSI_FALLBACK_VALUE: Short = Short.MIN_VALUE

/**
 * Broadcast receiver for classic Bluetooth scanning.
 */
class BluetoothClassicReceiver(

    /**
     * Flow with nearby devices.
     */
    private val devicesFlow: MutableStateFlow<Map<String, DeviceData>>,

    /**
     * Callback which checks whether Bluetooth connect permission is granted.
     */
    private val hasBluetoothConnectPermission: () -> Boolean,

    /**
     * Callback which is called when discovery is finished.
     */
    private val onDiscoveryFinished: () -> Unit,

    /**
     * Callback which is called when Bluetooth is turned off.
     */
    private val onTurnedOff: () -> Unit,

    /**
     * Callback which is called when Bluetooth is turned on.
     */
    private val onTurnedOn: () -> Unit,

    private val context: Context

) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothDevice.ACTION_FOUND -> storeDevice(intent)
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> onDiscoveryFinished()
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                if (state == BluetoothAdapter.STATE_OFF
                    || state == BluetoothAdapter.STATE_TURNING_OFF) {
                    onTurnedOff()
                } else if (state == BluetoothAdapter.STATE_ON) {
                    onTurnedOn()
                }
            }
        }
    }

    /**
     * Stores device data into the flow.
     * @param intent Intent with device data.
     */
    @SuppressLint("MissingPermission")
    private fun storeDevice(intent: Intent) {
        val device: BluetoothDevice = getBluetoothDeviceFromIntent(intent) ?: return

        // extract data about device
        val deviceMacAddress: String = device.address
        val rssi: Short = intent.getShortExtra(
            BluetoothDevice.EXTRA_RSSI, RSSI_FALLBACK_VALUE)
        val deviceName: String? = if (hasBluetoothConnectPermission())
            device.name?.takeIf(String::isNotBlank) else null

        // insert data into the flow
        devicesFlow.update { currentDevicesMap ->
            updateDevicesFlow(currentDevicesMap, deviceMacAddress, rssi, deviceName)
        }
    }

    /**
     * Gets Bluetooth device from supplied intent.
     * @param intent Intent to get device from.
     * @return If found, the Bluetooth device; otherwise returns null.
     */
    private fun getBluetoothDeviceFromIntent(intent: Intent): BluetoothDevice? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }
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
                                  rssi: Short,
                                  deviceName: String?): Map<String, DeviceData> {
        // favor already stored name over "unknown device"
        val storedDeviceName: String? = currentDevicesMap[deviceMacAddress]?.name
        val finalDeviceName: String = deviceName
            ?: storedDeviceName
            ?: context.resources.getString(R.string.unknown_device_name)

        // favor already stored rssi over fallback value
        val storedRssi = currentDevicesMap[deviceMacAddress]?.rssi
        val finalRssi = if (storedRssi == null || rssi != RSSI_FALLBACK_VALUE)
            rssi.toFloat() else storedRssi

        val deviceData = DeviceData(finalDeviceName, deviceMacAddress, finalRssi)
        return currentDevicesMap + (deviceMacAddress to deviceData)
    }
}