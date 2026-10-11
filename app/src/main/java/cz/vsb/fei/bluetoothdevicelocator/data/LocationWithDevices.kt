package cz.vsb.fei.bluetoothdevicelocator.data

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation

/**
 * Enables queries of all devices for a single location.
 */
data class LocationWithDevices (
    /**
     * The location.
     */
    @Embedded val location: LocationEntity,
    @Relation(
        parentColumns = ["locationId"],
        entityColumns = ["deviceId"],
        associateBy = Junction(DeviceLocationCrossRef::class)
    )

    /**
     * List of devices associated with this location.
     */
    val devices: List<DeviceEntity>
)