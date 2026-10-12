package cz.vsb.fei.bluetoothdevicelocator.data

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Stored device record with its key properties.
 */
@Entity(indices = [Index(value=["macAddress"], unique = true)])
data class DeviceEntity(

    /**
     * The device's artificial primary key.
     */
    @PrimaryKey(autoGenerate = true)
    val deviceId: Long = 0,

    /**
     * The device's name.
     */
    val name: String,

    /**
     * The device's MAC address.
     */
    val macAddress: String
) {

    /**
     * Secondary constructor that ignores ID.
     */
    constructor(name: String, macAddress: String) : this(0, name, macAddress)
}
