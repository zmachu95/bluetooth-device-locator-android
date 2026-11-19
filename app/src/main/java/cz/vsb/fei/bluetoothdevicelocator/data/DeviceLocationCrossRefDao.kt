package cz.vsb.fei.bluetoothdevicelocator.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query

/**
 * Data Access Object for DeviceLocationCrossRef.
 */
@Dao
interface DeviceLocationCrossRefDao {

    /**
     * Tries to insert a cross-reference into the database.
     * @param crossRef Cross-reference to insert.
     * @return Positive ID of inserted row if successful, otherwise returns -1.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDeviceLocationCrossRef(crossRef: DeviceLocationCrossRef): Long

    /**
     * Tries to remove a cross-reference from the database.
     * @param crossRef Cross-reference to remove.
     * @return 1 if successful, otherwise returns 0.
     */
    @Delete
    suspend fun removeDeviceLocationCrossRef(crossRef: DeviceLocationCrossRef): Int

    /**
     * Tries to remove a cross-reference from the database.
     * @param deviceId Device's ID.
     * @param locationId Location's ID.
     * @return 1 if successful, otherwise returns 0.
     */
    @Query("DELETE FROM DeviceLocationCrossRef WHERE deviceId = :deviceId AND locationId = :locationId")
    suspend fun removeDeviceLocationCrossRef(deviceId: Long, locationId: Long) : Int

    /**
     * Removes all location cross-references for the supplied device.
     * @param deviceId Device's ID.
     * @return Number of deleted cross-references.
     */
    @Query("DELETE FROM DeviceLocationCrossRef WHERE deviceId = :deviceId")
    suspend fun removeDeviceLocations(deviceId: Long) : Int

    /**
     * Removes specific location cross-references for the supplied device.
     * @param deviceId Device's ID.
     * @param locationIds List of location IDs to remove.
     * @return Number of deleted cross-references.
     */
    @Query("DELETE FROM DeviceLocationCrossRef WHERE deviceId = :deviceId AND locationId IN (:locationIds)")
    suspend fun removeDeviceLocations(deviceId: Long, locationIds: List<Long>) : Int

    /**
     * Deletes all cross-references from the database.
     * @return Number of deleted cross-references.
     */
    @Query("DELETE FROM DeviceLocationCrossRef")
    suspend fun removeAll() : Int

    /**
     * Gets signal strength (RSSI) for a device at given location.
     * @param deviceId Device's ID.
     * @param locationId Location's ID.
     * @return Signal strength (RSSI) if found, otherwise returns null.
     */
    @Query("SELECT rssi FROM DeviceLocationCrossRef WHERE deviceId = :deviceId AND locationId = :locationId")
    suspend fun getSignalStrength(deviceId: Long, locationId: Long): Float?

}
