package cz.vsb.fei.bluetoothdevicelocator.data

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Instant

/**
 * Earth's radius in meters.
 */
private const val EARTH_RADIUS: Float = 6371e3f

/**
 * Expected RSSI value in dBm at 1 meter distance.
 */
private const val EXPECTED_RSSI_AT_1M: Float = -59f

/**
 * RSSI distance calculation coefficient.
 */
private const val RSSI_DISTANCE_COEFFICIENT: Double = 2.0

/**
 * Stored location record.
 */
@Entity(indices = [Index(value=["epochMilliseconds"], unique = true)])
data class LocationEntity (

    /**
     * The location's artificial primary key.
     */
    @PrimaryKey(autoGenerate = true)
    val locationId: Long = 0,

    /**
     * The location's latitude.
     */
    val latitude: Double,

    /**
     * The location's longitude.
     */
    val longitude: Double,

    /**
     * The location's accuracy.
     */
    val accuracy: Double,

    /**
     * The location's timestamp represented as number of milliseconds from epoch.
     */
    val epochMilliseconds: Long
) {

    /**
     * Secondary constructor that ignores ID and accepts Instant object.
     */
    constructor(latitude: Double, longitude: Double, accuracy: Double, timestamp: Instant)
            : this(0, latitude, longitude, accuracy, timestamp.toEpochMilliseconds())

    /**
     * Gets the location's timestamp as Instant object.
     */
    val timestamp: Instant
        get() = Instant.fromEpochMilliseconds(epochMilliseconds)

    /**
     * Calculates distance to another location represented by latitude and longitude values.
     * @param otherLatitude Latitude of the other location.
     * @param otherLongitude Longitude of the other location.
     * @return Distance between locations in meters.
     */
    fun calculateDistanceTo(otherLatitude: Double, otherLongitude: Double): Double {
        // see Haversine formula: https://en.wikipedia.org/wiki/Haversine_formula
        val lat1rad: Double = degToRad(this.latitude)
        val lat2rad: Double = degToRad(otherLatitude)
        val long1Rad: Double = degToRad(this.longitude)
        val long2Rad: Double = degToRad(otherLongitude)

        val deltaLat: Double = lat2rad - lat1rad
        val deltaLong: Double = long2Rad - long1Rad

        val a: Double = sin(deltaLat/2.0) * sin(deltaLat/2.0) +
                cos(lat1rad) * cos(lat2rad) *
                sin(deltaLong/2.0) * sin(deltaLong/2.0)
        val c: Double = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))

        return EARTH_RADIUS * c
    }

    /**
     * Calculates the net location accuracy, combining GPS and RSSI accuracies.
     * @param otherAccuracy Accuracy of the other GPS location.
     * @param rssi RSSI value at the other location in dBm.
     * @return Net location accuracy in meters.
     */
    fun calculateNetAccuracy(otherAccuracy: Double, rssi: Float): Double {
        val accuracyImperfection = this.accuracy + otherAccuracy
        val rssiDistance = calculateDistanceFromRssi(rssi)

        return accuracyImperfection + rssiDistance
    }

    /**
     * Calculates approximate distance from RSSI value.
     * @param rssi RSSI value in dBm.
     * @return Approximate distance in meters.
     */
    private fun calculateDistanceFromRssi(rssi: Float): Double {
        val numerator: Double = (EXPECTED_RSSI_AT_1M - rssi).toDouble()
        val denominator: Double = 10.0 * RSSI_DISTANCE_COEFFICIENT

        return 10.0.pow(numerator / denominator)
    }

    /**
     * Converts degrees to radians.
     * @param deg Degrees to convert.
     * @return Radians value.
     */
    private fun degToRad(deg: Double): Double = deg * Math.PI / 180.0

}
