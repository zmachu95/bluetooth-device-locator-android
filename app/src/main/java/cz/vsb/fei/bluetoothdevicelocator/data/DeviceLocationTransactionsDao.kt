package cz.vsb.fei.bluetoothdevicelocator.data

import androidx.room3.Dao
import androidx.room3.Transaction

/**
 * Data Access Object containing transaction related to Device and Location entities.
 */
@Dao
interface DeviceLocationTransactionsDao {

    /**
     * Inserts a device and its location into the database.
     * @param db Database instance.
     * @param device Device to insert.
     * @param location Location to insert.
     */
    @Transaction
    suspend fun insertDevice(db: LocatorDatabase,
                             device: DeviceData,
                             location: LocationData) {
        val deviceDao = db.deviceDao()
        val locationDao = db.locationDao()

        val newDevice = DeviceEntity(name = device.name, macAddress = device.macAddress)
        var newDeviceId = deviceDao.insertDevice(newDevice)
        if (newDeviceId == -1L) {
            newDeviceId = deviceDao.getDeviceIdByMacAddress(newDevice.macAddress)!!
        }

        val newLocation = LocationEntity(
            location.latitude, location.longitude, location.accuracy, location.timeStamp)
        var newLocationId = locationDao.insertLocation(newLocation)
        if (newLocationId == -1L) {
            newLocationId = locationDao.getLocationAtTimestamp(
                newLocation.epochMilliseconds)!!.locationId
        }

        db.deviceLocationCrossRefDao().insertDeviceLocationCrossRef(
            DeviceLocationCrossRef(newDeviceId, newLocationId, device.rssi))
    }

    /**
     * Removes a device and its locations from the database.
     * @param db Database instance.
     * @param deviceId ID of device to remove.
     */
    @Transaction
    suspend fun removeDevice(db: LocatorDatabase, deviceId: Long) {
        db.deviceLocationCrossRefDao().removeDeviceLocations(deviceId)
        db.locationDao().removeOrphanedLocations()
        db.deviceDao().removeDevice(deviceId)
    }

    /**
     * Removes all devices and their locations from the database.
     * @param db Database instance.
     */
    @Transaction
    suspend fun removeAllDevices(db: LocatorDatabase) {
        db.deviceLocationCrossRefDao().removeAll()
        db.locationDao().removeAll()
        db.deviceDao().removeAll()
    }

    /**
     * Removes a specific device location from the database.
     * @param db Database instance.
     * @param deviceId Device's ID.
     * @param locationId ID of location to remove.
     */
    @Transaction
    suspend fun removeDeviceLocation(db: LocatorDatabase,
                                     deviceId: Long,
                                     locationId: Long) {
        db.deviceLocationCrossRefDao().removeDeviceLocationCrossRef(deviceId, locationId)
        db.locationDao().removeOrphanedLocations()
    }

    /**
     * Removes all locations except the last one for a specific device.
     * @param db Database instance.
     * @param deviceId Device's ID.
     */
    @Transaction
    suspend fun removeDeviceLocationsButLast(db: LocatorDatabase, deviceId: Long) {
        val deviceLocations = db.deviceDao().getDeviceLocations(deviceId)
        if (deviceLocations.isEmpty()) {
            return
        }

        val lastLocation = deviceLocations.fold(
            initial = deviceLocations.first(),
            operation = { currentLast, location ->
                if (location.epochMilliseconds > currentLast.epochMilliseconds) location
                else currentLast })

        db.deviceLocationCrossRefDao().removeDeviceLocations(
            deviceId, deviceLocations
                .filter { location -> location.locationId != lastLocation.locationId }
                .map { location -> location.locationId }
        )
        db.locationDao().removeOrphanedLocations()
    }
}
