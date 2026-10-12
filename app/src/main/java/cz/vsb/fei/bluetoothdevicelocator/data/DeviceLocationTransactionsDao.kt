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
     * @param rssi Received signal strength indication in (dBm).
     */
    @Transaction
    suspend fun insertDeviceWithLocation(db: LocatorDatabase, device: DeviceEntity, location: LocationEntity, rssi: Float) {
        val deviceDao = db.deviceDao()
        val locationDao = db.locationDao()

        var newDeviceId = deviceDao.insertDevice(device)
        if (newDeviceId == -1L) {
            newDeviceId = deviceDao.getDeviceIdByMacAddress(device.macAddress)!!
        }

        var newLocationId = locationDao.insertLocation(location)
        if (newLocationId == -1L) {
            newLocationId = locationDao.getLocationAtTimestamp(location.epochMilliseconds)!!.locationId
        }

        db.deviceLocationCrossRefDao().insertDeviceLocationCrossRef(DeviceLocationCrossRef(newDeviceId, newLocationId, rssi))
    }
}
