package cz.vsb.fei.bluetoothdevicelocator.data

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation

/**
 * Enables queries of all locations for a single device.
 */
data class DeviceWithLocations (
    /**
     * The device.
     */
    @Embedded val device: DeviceEntity,
    @Relation(
        parentColumns = ["deviceId"],
        entityColumns = ["locationId"],
        associateBy = Junction(DeviceLocationCrossRef::class)
    )

    /**
     * List of locations associated with this device.
     */
    val locations: List<LocationEntity>
)
