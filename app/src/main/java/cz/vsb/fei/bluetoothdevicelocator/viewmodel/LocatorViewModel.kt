package cz.vsb.fei.bluetoothdevicelocator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceData
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceEntity
import cz.vsb.fei.bluetoothdevicelocator.data.LocationData
import cz.vsb.fei.bluetoothdevicelocator.data.LocationEntity
import cz.vsb.fei.bluetoothdevicelocator.data.LocatorRepository
import cz.vsb.fei.bluetoothdevicelocator.hardware.BluetoothGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.lang.AutoCloseable
import kotlin.time.Clock

/**
 * ViewModel for Bluetooth device locator.
 */
class LocatorViewModel(
    private val repository: LocatorRepository,
    private val bluetoothGateway: BluetoothGateway
) : ViewModel() {

    /**
     * Gets "hot" flow of all devices in the database.
     */
    val allDevices: StateFlow<Map<Long, DeviceEntity>> = repository.getAllDevices()
        .map { devices-> devices.associateBy { device -> device.deviceId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    /**
     * Gets "hot" flow of the last location for all devices in the database.
     */
    val devicesLastLocations: StateFlow<Map<DeviceEntity, LocationEntity?>> =
        repository.getDevicesLastLocation().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    /**
     * Gets "hot" flow of nearby active devices.
     */
    val nearbyDevices: StateFlow<List<DeviceData>> = bluetoothGateway.foundDevices
        .map { devices-> devices.values.sortedWith(this::nearbyDevicesComparator).toList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Gets "hot" flow of Bluetooth scanning status.
     */
    val isBluetoothScanning: StateFlow<Boolean> = bluetoothGateway.isScanning

    /**
     * Starts Bluetooth scanning.
     */
    fun startBluetoothScan() {
        bluetoothGateway.startScan()
    }

    /**
     * Stops Bluetooth scanning.
     */
    fun stopBluetoothScan() {
        bluetoothGateway.stopScan()
    }

    /**
     * Performs hardware check whether Bluetooth is enabled.
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothGateway.isBluetoothEnabled()
    }

    override fun onCleared() {
        super.onCleared()

        // stop Bluetooth scanning
        bluetoothGateway.stopScan()
    }

    /**
     * Gets "cold" flow of all locations for a specific device.
     * @param deviceId Device's ID.
     * @return Flow of all locations for a specific device.
     */
    fun getLocationsForDevice(deviceId: Long?): Flow<Map<Long, LocationEntity>> {
        return if (deviceId != null) repository.getDeviceLocations(deviceId)
            .map { locations-> locations.associateBy { location -> location.locationId } } else
            flowOf(emptyMap())
    }

    /**
     * Inserts a device and a mock location into the database.
     * @param deviceData Data o device to insert.
     */
    fun saveDeviceWithMockLocation(deviceData: DeviceData) {
        viewModelScope.launch {
            val mockLocation = LocationData(
                latitude = 49.8306,
                longitude = 18.1624,
                accuracy = 5.0,
                timeStamp = Clock.System.now()
            )
            repository.insertDeviceWithLocation(
                device = deviceData,
                location = mockLocation
            )
        }
    }

    /**
     * Removes a device and its locations from the database.
     * @param deviceId ID of device to remove.
     */
    fun removeDevice(deviceId: Long) {
        viewModelScope.launch {
            repository.removeDevice(deviceId)
        }
    }

    /**
     * Removes all devices and their locations from the database.
     */
    fun removeAllDevices() {
        viewModelScope.launch {
            repository.removeAllDevices()
        }
    }

    /**
     * Removes a specific device location from the database.
     * @param deviceId Device's ID.
     * @param locationId ID of location to remove.
     */
    fun removeDeviceLocation(deviceId: Long, locationId: Long) {
        viewModelScope.launch {
            repository.removeDeviceLocation(deviceId, locationId)
        }
    }

    /**
     * Removes all locations except the last one for a specific device.
     * @param deviceId Device's ID.
     */
    fun removeDeviceLocationsButLast(deviceId: Long) {
        viewModelScope.launch {
            repository.removeDeviceLocationsButLast(deviceId)
        }
    }

    /**
     * Compares two devices based on their RSSI and name.
     * @param first First device to compare.
     * @param second Second device to compare.
     */
    private fun nearbyDevicesComparator(first: DeviceData, second: DeviceData): Int {
        return if (first.rssi != second.rssi)
            second.rssi.compareTo(first.rssi) else first.name.compareTo(second.name)
    }
}
