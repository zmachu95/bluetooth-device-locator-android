package cz.vsb.fei.bluetoothdevicelocator.hardware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build

/**
 * Broadcast receiver for location services.
 */
class LocationBroadcastReceiver(

    /**
     * Callback to be used when location services are turned off.
     */
    private val onTurnedOff: () -> Unit,

    /**
     * Callback to be used when location services are turned on.
     */
    private val onTurnedOn: () -> Unit,

) : BroadcastReceiver() {
    companion object {

        /**
         * Checks whether location services are enabled.
         * @param context Context to use.
         * @return True if location services are enabled; otherwise false.
         */
        fun areLocationServicesEnabled(context: Context): Boolean {
            val locationManager = context.getSystemService(LocationManager::class.java)

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                /* this allows to get the exact answer */
                locationManager?.isLocationEnabled == true
            } else {
                locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
                    || locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == LocationManager.MODE_CHANGED_ACTION) {
            if (areLocationServicesEnabled(context)) {
                onTurnedOn()
            } else {
                onTurnedOff()
            }
        }
    }
}
