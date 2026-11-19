package cz.vsb.fei.bluetoothdevicelocator.ui

import cz.vsb.fei.bluetoothdevicelocator.data.DeviceEntity
import cz.vsb.fei.bluetoothdevicelocator.data.LocationEntity

/**
 * Sealed interface grouping all application screens.
 */
sealed interface IScreen {

    /**
     * Main menu screen.
     */
    object Menu : IScreen

    /**
     * Screen for storing nearby Bluetooth devices.
     */
    object StoreNearbyDevices : IScreen

    /**
     * Screen for showing tracked Bluetooth devices.
     */
    object ShowTrackedDevices : IScreen

    /**
     * Screen with saved locations of a concrete device.
     */
    data class SingleDeviceLocations(

        /**
         * Device to show locations for.
         */
        val device: DeviceEntity?
    ) : IScreen

    /**
     * Screen for finding a device.
     */
    data class FindDevice(

        /**
         * Device to find.
         */
        val device: DeviceEntity?,

        /**
         * Device's saved location.
         */
        val location: LocationEntity?,
    ) : IScreen
}