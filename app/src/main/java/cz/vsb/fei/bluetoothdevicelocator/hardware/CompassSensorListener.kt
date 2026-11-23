package cz.vsb.fei.bluetoothdevicelocator.hardware

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.atan2

/**
 * Listener for compass sensor events.
 */
class CompassSensorListener(

    /**
     * Callback used when azimuth angle changes. Argument is in radians.
     */
    private val updateAzimuthAngleCallback : (Float) -> Unit

) : SensorEventListener {
    companion object {

        /**
         * Checks whether given sensor type is a compass sensor.
         * @param sensorType Sensor type to check.
         * @return True if sensor type is compass sensor; otherwise false.
         */
        private fun isCompassSensor(sensorType: Int?): Boolean =
            sensorType == Sensor.TYPE_ROTATION_VECTOR
                    || sensorType == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR

        /**
         * Reference y-axis vector for rotation.
         */
        val yAxisReference: Array<Float> = arrayOf(0.0f, 1.0f, 0.0f)
    }

    /**
     * Array for holding quaternion values.
     */
    private val quaternionValues = FloatArray(4)

    /**
     * Array for holding rotated y-axis vector.
     */
    private val rotatedYAxis = FloatArray(3)

    override fun onSensorChanged(event: SensorEvent?) {
        val sensor = event?.sensor
        if (sensor == null || !isCompassSensor(sensor.type)) return

        SensorManager.getQuaternionFromVector(quaternionValues, event.values)

        // copied from androidx.xr.runtime.math.Quaternion.times(Vector3)
        val qw = quaternionValues[0]
        val qx = quaternionValues[1]
        val qy = quaternionValues[2]
        val qz = quaternionValues[3]
        val vx = yAxisReference[0]
        val vy = yAxisReference[1]
        val vz = yAxisReference[2]

        val rx = qy * vz - qz * vy + qw * vx
        val ry = qz * vx - qx * vz + qw * vy
        val rz = qx * vy - qy * vx + qw * vz
        val sx = qy * rz - qz * ry
        val sy = qz * rx - qx * rz
        rotatedYAxis[0] = 2 * sx + vx
        rotatedYAxis[1] = 2 * sy + vy

        /* rotate the y-axis vector by -90 deg so that atan function
           calculates correct azimuth value  */
        val azimuthAngle = atan2(rotatedYAxis[0], rotatedYAxis[1])
        updateAzimuthAngleCallback(azimuthAngle)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

}