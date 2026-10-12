package cz.vsb.fei.bluetoothdevicelocator.data

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

/**
 * Cross-reference table for Device and Location entities.
 */
@Entity(primaryKeys = ["deviceId", "locationId"],
    indices = [Index(value = ["deviceId", "locationId"])],
    foreignKeys = [ForeignKey(entity = DeviceEntity::class, parentColumns = ["deviceId"],
        childColumns = ["deviceId"], onDelete = ForeignKey.CASCADE),
    ForeignKey(entity = LocationEntity::class, parentColumns = ["locationId"],
        childColumns = ["locationId"], onDelete = ForeignKey.CASCADE)])
data class DeviceLocationCrossRef(

    /**
     * Device's primary key.
     */
    val deviceId: Long,

    /**
     * Location's primary key.
     */
    val locationId: Long,

    /**
     * Received signal strength indication in (dBm).
     */
    val rssi: Float
)