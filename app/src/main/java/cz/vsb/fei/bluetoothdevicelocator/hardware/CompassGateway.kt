package cz.vsb.fei.bluetoothdevicelocator.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Enables communication with device's orientation sensors.
 */
class CompassGateway(context: Context) {

    /**
     * Backing field for azimuth property.
     */
    private val _azimuth = MutableStateFlow(0f)

    /**
     * Backing field for isCompassActive property.
     */
    private val _isCompassActive = MutableStateFlow(false)

    /**
     * "Hot" flow with current phone azimuth in degrees
     * (0° = North, 90° = East, 180° = South, -90° = West).
     */
    val azimuth: StateFlow<Float> = _azimuth

    /**
     * "Hot" flow with flag whether compass updates are active.
     */
    val isCompassActive: StateFlow<Boolean> = _isCompassActive

    /**
     * Listener for compass sensor events.
     */
    private val sensorListener = CompassSensorListener{ azimuth ->
        _azimuth.update { (azimuth * 180.0 / Math.PI).toFloat() }
    }

    /**
     * Running device's sensor manager.
     */
    private val sensorManager: SensorManager? =
        context.getSystemService(SensorManager::class.java)

    /**
     * Running device's rotation vector sensor.
     * If not available, geomagnetic rotation vector sensor is used instead.
     */
    private val rotationSensor: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)

    /**
     * Checks whether the device has a rotation vector sensor.
     */
    fun isCompassAvailable(): Boolean = rotationSensor != null

    /**
     * Starts listening to compass sensor updates.
     */
    fun startCompassUpdates() {
        if (_isCompassActive.value || rotationSensor == null) return

        sensorManager?.registerListener(
            sensorListener,
            rotationSensor,
            SensorManager.SENSOR_DELAY_UI
        )
        _isCompassActive.update { true }
    }

    /**
     * Stops listening to compass sensor updates.
     */
    fun stopCompassUpdates() {
        if (!_isCompassActive.value) return

        sensorManager?.unregisterListener(sensorListener)
        _isCompassActive.update { false }
    }

}
