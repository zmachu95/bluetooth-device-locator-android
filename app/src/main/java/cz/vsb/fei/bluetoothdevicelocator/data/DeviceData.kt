package cz.vsb.fei.bluetoothdevicelocator.data

/**
 * Data class representing single device data.
 */
data class DeviceData(

    /**
     * Device's name.
     */
    val name: String,

    /**
     * Device's MAC address.
     */
    val macAddress: String,

    /**
     * Received signal strength indication in (dBm).
     */
    val rssi: Float
)
