package cz.vsb.fei.bluetoothdevicelocator.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for DeviceEntity.
 */
@Dao
interface DeviceDao {

    /**
     * Tries to insert a device into the database.
     * @param device Device to insert.
     * @return Positive ID of inserted row if successful, otherwise returns -1.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDevice(device: DeviceEntity): Long

    /**
     * Tries to remove a device from the database.
     * @param device Device to remove.
     * @return 1 if successful, otherwise returns 0.
     */
    @Delete
    suspend fun removeDevice(device: DeviceEntity): Int

    /**
     * Tries to update a device in the database.
     * @param device Device to update.
     * @return 1 if successful, otherwise returns 0.
     */
    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateDevice(device: DeviceEntity): Int

    /**
     * Gets a device by its ID.
     * @param id Device's ID.
     * @return Device with given ID if found, otherwise returns null.
     */
    @Query("SELECT * FROM DeviceEntity WHERE deviceId = :id")
    suspend fun getDeviceById(id: Long): DeviceEntity?

    /**
     * Gets all devices from the database.
     * @return Flow of all devices.
     */
    @Query("SELECT * FROM DeviceEntity")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    /**
     * Gets all stored locations for given device.
     * @param deviceId Device's ID.
     * @return List of locations for given device.
     */
    @Query("SELECT LocationEntity.* FROM DeviceLocationCrossRef " +
            "JOIN LocationEntity ON DeviceLocationCrossRef.locationId = LocationEntity.locationId " +
            "WHERE deviceId = :deviceId")
    suspend fun getDeviceLocations(deviceId: Long): List<LocationEntity>

    /**
     * Gets the last location for given device.
     * @param deviceId Device's ID.
     * @return Last location for given device.
     */
    @Query("SELECT LocationEntity.* FROM DeviceLocationCrossRef " +
            "JOIN LocationEntity ON DeviceLocationCrossRef.locationId = LocationEntity.locationId " +
            "WHERE deviceId = :deviceId " +
            "ORDER BY epochMilliseconds DESC LIMIT 1")
    suspend fun getDeviceLastLocation(deviceId: Long): LocationEntity?

    /**
     * Gets all devices that match given name.
     * @param name Device's name.
     * @return List of devices that match given name.
     */
    @Query("SELECT * FROM DeviceEntity WHERE name LIKE :name")
    suspend fun getDevicesByName(name: String): List<DeviceEntity>

    /**
     * Gets device that matches the given MAC address.
     * @param macAddress Device's MAC address.
     * @return Device with given MAC address if found, otherwise returns null.
     */
    @Query("SELECT * FROM DeviceEntity WHERE macAddress = :macAddress")
    suspend fun getDeviceByMacAddress(macAddress: String): DeviceEntity?

    /**
     * Gets device ID that matches the given MAC address.
     * @param macAddress Device's MAC address.
     * @return Device ID with given MAC address if found, otherwise returns null.
     */
    @Query("SELECT deviceId FROM DeviceEntity WHERE macAddress = :macAddress")
    suspend fun getDeviceIdByMacAddress(macAddress: String): Long?
}
