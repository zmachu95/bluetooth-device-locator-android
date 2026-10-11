package cz.vsb.fei.bluetoothdevicelocator.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.time.TimeMark

/**
 * Stored location record.
 */
@Entity
data class LocationEntity (

    /**
     * The location's artificial primary key.
     */
    @PrimaryKey(autoGenerate = true)
    val locationId: Long,

    /**
     * The location's latitude.
     */
    val latitude: Double,

    /**
     * The location's longitude.
     */
    val longitude: Double,

    /**
     * The location's accuracy.
     */
    val accuracy: Double,

    /**
     * The location's timestamp.
     */
    val timestamp: TimeMark
)