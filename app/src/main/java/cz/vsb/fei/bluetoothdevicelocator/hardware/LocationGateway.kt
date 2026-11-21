package cz.vsb.fei.bluetoothdevicelocator.hardware

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import cz.vsb.fei.bluetoothdevicelocator.data.LocationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Clock
import kotlinx.coroutines.tasks.await
import com.google.android.gms.location.LocationRequest
import kotlinx.coroutines.flow.update

/**
 * Enables communication with location services.
 */
class LocationGateway(private val context: Context) {
    companion object {

        /**
         * Array of permissions required for correct location functionality.
         */
        val LOCATION_PERMISSIONS: Array<String> =
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        /**
         * Interval for location updates in milliseconds.
         */
        private const val LOCATION_UPDATE_INTERVAL_MILLIS: Long = 5000L

        /**
         * Minimum distance between location updates in meters.
         */
        private const val LOCATION_UPDATE_MIN_DISTANCE_DIFF: Float = 1f
    }

    /**
     * Backing field for areLocationUpdatesActive property.
     */
    private var _areLocationUpdatesActive = MutableStateFlow<Boolean>(false)

    /**
     * Backing field for currentLocation property.
     */
    private var _currentLocation: MutableStateFlow<LocationData?> = MutableStateFlow(null)

    /**
     * "Hot" flow with flag whether location updates are active.
     */
    val areLocationUpdatesActive: StateFlow<Boolean> = _areLocationUpdatesActive

    /**
     * "Hot" flow with current location.
     */
    val currentLocation: StateFlow<LocationData?> = _currentLocation

    /**
     * Running device's location client.
     */
    private val locationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Callback object for location updates.
     */
    private val locationUpdateCallback = LocationUpdateCallback{ locationData ->
        _currentLocation.update { locationData }
    }

    /**
     * Checks whether location services are enabled.
     * @return True if location services are enabled; otherwise false.
     */
    fun areLocationServicesEnabled(): Boolean {
        val locationManager = context.getSystemService(LocationManager::class.java)
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Checks whether the running device has necessary location permissions.
     * @return True if the device has necessary location permissions; otherwise false.
     */
    fun hasDeviceLocationPermission(): Boolean =
        hasDeviceCoarseLocationPermission() || hasDeviceFineLocationPermission()

    /**
     * Tries to get device's current location.
     * @return Device's current location or null if unavailable.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationData? {
        if (hasDeviceLocationPermission() && areLocationServicesEnabled()) {
            try {
                val location: Location = locationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY, null).await()
                return LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy.toDouble(),
                    timeStamp = Clock.System.now()
                )
            } catch (_: Exception) {
                return null
            }
        } else {
            return null
        }
    }

    /**
     * Starts location updates.
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (_areLocationUpdatesActive.value) return
        if (!hasDeviceLocationPermission() || !areLocationServicesEnabled()) return

        val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                LOCATION_UPDATE_INTERVAL_MILLIS)
            .setMinUpdateDistanceMeters(LOCATION_UPDATE_MIN_DISTANCE_DIFF).build()

        locationClient.requestLocationUpdates(
            locationRequest,
            locationUpdateCallback,
            Looper.getMainLooper()
        )
        _areLocationUpdatesActive.update { true }
    }

    /**
     * Stops location updates.
     */
    fun stopLocationUpdates() {
        if (!_areLocationUpdatesActive.value) return

        locationClient.removeLocationUpdates(locationUpdateCallback)
        _areLocationUpdatesActive.update { false }
    }

    /**
     * Checks whether the running device has coarse location permission.
     */
    private fun hasDeviceCoarseLocationPermission(): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    /**
     * Checks whether the running device has fine location permission.
     */
    private fun hasDeviceFineLocationPermission(): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

}