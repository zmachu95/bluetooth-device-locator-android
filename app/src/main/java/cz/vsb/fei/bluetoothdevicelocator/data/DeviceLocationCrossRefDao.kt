package cz.vsb.fei.bluetoothdevicelocator.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy

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
}
