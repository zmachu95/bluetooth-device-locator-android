package cz.vsb.fei.bluetoothdevicelocator.viewmodel

import androidx.lifecycle.ViewModel
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceData
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceEntity
import cz.vsb.fei.bluetoothdevicelocator.data.LocationEntity
import cz.vsb.fei.bluetoothdevicelocator.ui.IScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for Bluetooth device locator.
 */
abstract class LocatorViewModel : ViewModel() {

    /**
     * Gets "hot" flow of all devices in the database.
     */
    abstract val allDevices: StateFlow<Map<Long, DeviceEntity>>

    /**
     * Gets "hot" flow with current screen.
     */
    abstract val currentScreen: StateFlow<IScreen>

    /**
     * Gets "hot" flow of the last location for all devices in the database.
     */
    abstract val devicesLastLocations: StateFlow<Map<DeviceEntity, LocationEntity?>>

    /**
     * Gets "hot" flow of Bluetooth scanning status.
     */
    abstract val isBluetoothScanning: StateFlow<Boolean>

    /**
     * Gets "hot" flow of nearby active devices.
     */
    abstract val nearbyDevices: StateFlow<List<DeviceData>>

    /**
     * Performs hardware check whether location services are enabled.
     */
    abstract fun areLocationServicesEnabled(): Boolean

    /**
     * Changes the current screen.
     * @param screen New screen to render.
     */
    abstract fun changeCurrentScreen(screen: IScreen)

    /**
     * Gets "cold" flow of all locations for a specific device.
     * @param deviceId Device's ID.
     * @return Flow of all locations for a specific device.
     */
    abstract fun getLocationsForDevice(deviceId: Long?): Flow<Map<Long, LocationEntity>>

    /**
     * Performs hardware check whether Bluetooth is enabled.
     */
    abstract fun isBluetoothEnabled(): Boolean

    /**
     * Navigates back to the previous screen.
     */
    abstract fun navigateBack()

    /**
     * Removes all devices and their locations from the database.
     */
    abstract fun removeAllDevices()

    /**
     * Removes a device and its locations from the database.
     * @param deviceId ID of device to remove.
     */
    abstract fun removeDevice(deviceId: Long)

    /**
     * Removes a specific device location from the database.
     * @param deviceId Device's ID.
     * @param locationId ID of location to remove.
     */
    abstract fun removeDeviceLocation(deviceId: Long, locationId: Long)

    /**
     * Removes all locations except the last one for a specific device.
     * @param deviceId Device's ID.
     */
    abstract fun removeDeviceLocationsButLast(deviceId: Long)

    /**
     * Inserts a device with current location into the database.
     * @param deviceData Data of device to insert.
     * @param onSuccess Callback called when the device is saved successfully.
     * @param onUnavailableLocation Callback called when the device's location is unavailable.
     */
    abstract fun saveDeviceWithLocation(
        deviceData: DeviceData,
        onSuccess: () -> Unit,
        onUnavailableLocation: () -> Unit
    )

    /**
     * Starts Bluetooth scanning.
     */
    abstract fun startBluetoothScan()

    /**
     * Stops Bluetooth scanning.
     */
    abstract fun stopBluetoothScan()

}
