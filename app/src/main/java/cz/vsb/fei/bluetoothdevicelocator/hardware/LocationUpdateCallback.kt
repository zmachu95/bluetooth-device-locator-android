package cz.vsb.fei.bluetoothdevicelocator.hardware

import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import cz.vsb.fei.bluetoothdevicelocator.data.LocationData
import kotlin.time.Clock

/**
 * Callback object for location updates.
 */
class LocationUpdateCallback(

    /**
     * Callback to perform upon location update.
     */
    private val onLocationUpdate: (LocationData) -> Unit

) : LocationCallback() {

    override fun onLocationResult(p0: LocationResult) {
        val currentLocation: Location = p0.lastLocation ?: return

        onLocationUpdate(
            LocationData(
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude,
                accuracy = currentLocation.accuracy.toDouble(),
                timeStamp = Clock.System.now()
            )
        )
    }
}