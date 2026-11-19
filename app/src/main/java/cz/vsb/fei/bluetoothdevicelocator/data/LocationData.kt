package cz.vsb.fei.bluetoothdevicelocator.data

import kotlin.time.Instant

/**
 * Data class representing single location data.
 */
data class LocationData(

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
    val timeStamp: Instant
)