package cz.vsb.fei.bluetoothdevicelocator.data

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.time.Instant

/**
 * Stored location record.
 */
@Entity(indices = [Index(value=["epochMilliseconds"], unique = true)])
data class LocationEntity (

    /**
     * The location's artificial primary key.
     */
    @PrimaryKey(autoGenerate = true)
    val locationId: Long = 0,

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
     * The location's timestamp represented as number of milliseconds from epoch.
     */
    val epochMilliseconds: Long
) {

    /**
     * Secondary constructor that ignores ID and accepts Instant object.
     */
    constructor(latitude: Double, longitude: Double, accuracy: Double, timestamp: Instant)
            : this(0, latitude, longitude, accuracy, timestamp.toEpochMilliseconds())

    /**
     * Gets the location's timestamp as Instant object.
     */
    val timestamp: Instant
        get() = Instant.fromEpochMilliseconds(epochMilliseconds)
}
