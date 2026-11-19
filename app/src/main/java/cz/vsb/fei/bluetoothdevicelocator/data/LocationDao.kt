package cz.vsb.fei.bluetoothdevicelocator.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/**
 * Data Access Object for LocationEntity.
 */
@Dao
interface LocationDao {

    /**
     * Tries to insert a location into the database.
     * @param location Location to insert.
     * @return Positive ID of inserted row if successful, otherwise returns -1.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocation(location: LocationEntity): Long

    /**
     * Tries to remove a location from the database.
     * @param location Location to remove.
     * @return 1 if successful, otherwise returns 0.
     */
    @Delete
    suspend fun removeLocation(location: LocationEntity): Int

    /**
     * Deletes all orphaned locations from the database, i.e. locations without a device.
     * @return Number of deleted orphaned locations.
     */
    @Query("DELETE FROM LocationEntity " +
            "WHERE LocationEntity.locationId NOT IN ( " +
            "    SELECT DeviceLocationCrossRef.locationId FROM DeviceLocationCrossRef " +
            ")")
    suspend fun removeOrphanedLocations() : Int

    /**
     * Deletes all locations from the database.
     * @return Number of deleted locations.
     */
    @Query("DELETE FROM LocationEntity")
    suspend fun removeAll() : Int

    /**
     * Tries to get location by its timestamp.
     * @param millisecondsFromEpoch Timestamp represented as number of milliseconds from epoch.
     * @return Location with given timestamp if found, otherwise returns null.
     */
    @Query("SELECT * FROM LocationEntity WHERE epochMilliseconds = :millisecondsFromEpoch")
    suspend fun getLocationAtTimestamp(millisecondsFromEpoch: Long): LocationEntity?

    /**
     * Gets all locations between two timestamps.
     * @param millisecondsFromEpochStart Start timestamp represented as number of milliseconds from epoch.
     * @param millisecondsFromEpochEnd End timestamp represented as number of milliseconds from epoch.
     * @return List of locations between two timestamps.
     */
    @Query("SELECT * FROM LocationEntity " +
            "WHERE epochMilliseconds BETWEEN :millisecondsFromEpochStart AND :millisecondsFromEpochEnd")
    fun getLocationsBetweenTwoTimestamps(millisecondsFromEpochStart: Long,
                                                 millisecondsFromEpochEnd: Long): Flow<List<LocationEntity>>

    /**
     * Gets all locations between two timestamps.
     * @param start Start timestamp.
     * @param end End timestamp.
     * @return List of locations between two timestamps.
     */
    fun getLocationsBetweenTwoTimestamps(start: Instant, end: Instant): Flow<List<LocationEntity>> {
        return getLocationsBetweenTwoTimestamps(start.toEpochMilliseconds(),
            end.toEpochMilliseconds())
    }
}
