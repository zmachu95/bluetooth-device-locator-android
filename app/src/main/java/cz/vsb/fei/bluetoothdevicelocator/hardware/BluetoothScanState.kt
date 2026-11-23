package cz.vsb.fei.bluetoothdevicelocator.hardware

/**
 * Represents possible states of Bluetooth scanning.
 */
enum class BluetoothScanState(

    /**
     * Flag whether scanning is active.
     */
    val isActive: Boolean
){

    /**
     * Bluetooth scanning is not active.
     */
    NOT_ACTIVE(false),

    /**
     * Bluetooth is performing a single scan only.
     */
    SINGLE_SCAN(true),

    /**
     * Bluetooth is performing a continuous scan.
     */
    CONTINUOUS_SCAN(true)

}
