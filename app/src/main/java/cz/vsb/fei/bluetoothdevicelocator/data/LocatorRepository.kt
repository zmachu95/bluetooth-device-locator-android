package cz.vsb.fei.bluetoothdevicelocator.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Bluetooth device locator database.
 */
class LocatorRepository(context: Context) {

    /**
     * Database instance.
     */
    private val db = LocatorDatabase.getDatabase(context)

    /**
     * Inserts a device and its location into the database.
     * @param device Device to insert.
     * @param location Location to insert.
     */
    suspend fun insertDeviceWithLocation(device: DeviceData, location: LocationData) {
        db.deviceLocationTransactionsDao().insertDevice(db, device, location)
    }

    /**
     * Removes a device and its locations from the database.
     * @param deviceId ID of device to remove.
     */
    suspend fun removeDevice(deviceId: Long) {
        db.deviceLocationTransactionsDao().removeDevice(db, deviceId)
    }

    /**
     * Removes all devices and their locations from the database.
     */
    suspend fun removeAllDevices() {
        db.deviceLocationTransactionsDao().removeAllDevices(db)
    }

    /**
     * Removes a specific device location from the database.
     * @param deviceId Device's ID.
     * @param locationId ID of location to remove.
     */
    suspend fun removeDeviceLocation(deviceId: Long, locationId: Long) {
        db.deviceLocationTransactionsDao().removeDeviceLocation(db, deviceId, locationId)
    }

    /**
     * Removes all locations except the last one for a specific device.
     * @param deviceId Device's ID.
     */
    suspend fun removeDeviceLocationsButLast(deviceId: Long) {
        db.deviceLocationTransactionsDao().removeDeviceLocationsButLast(db, deviceId)
    }

    /**
     * Gets all devices from the database.
     * @return Flow of all devices.
     */
    fun getAllDevices() : Flow<List<DeviceEntity>> {
        return db.deviceDao().getAllDevices()
    }

    /**
     * Gets the last location for all devices in the database.
     * @return Map containing devices and their last location.
     */
    fun getDevicesLastLocation() : Flow<Map<DeviceEntity, LocationEntity?>> {
        return db.deviceDao().getDevicesLastLocation()
    }

    /**
     * Gets all stored locations for given device.
     * @param deviceId Device's ID.
     * @return List of locations for given device.
     */
    fun getDeviceLocations(deviceId: Long) : Flow<List<LocationEntity>> {
        return db.deviceDao().getDeviceLocationsFlow(deviceId)
    }

    /**
     * Gets signal data for all devices and their locations.
     * @return Flow with list of signal data for all devices and their locations.
     */
    fun getDevicesLocationsSignalData(): Flow<List<DeviceLocationCrossRef>> {
        return db.deviceLocationCrossRefDao().getCrossRefData()
    }

}