package cz.vsb.fei.bluetoothdevicelocator.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import cz.vsb.fei.bluetoothdevicelocator.R
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceEntity
import cz.vsb.fei.bluetoothdevicelocator.data.LocationData
import cz.vsb.fei.bluetoothdevicelocator.data.LocationEntity
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.datetime.offsetAt
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Original width of temperature bar image in pixels.
 */
private const val TEMPERATURE_BAR_ORIGINAL_IMAGE_WIDTH = 270.0f

/**
 * Original height of temperature bar image in pixels.
 */
private const val TEMPERATURE_BAR_ORIGINAL_IMAGE_HEIGHT = 80.0f

/**
 * Relative x offset of animated temperature bar in pixels.
 */
private const val TEMPERATURE_BAR_ANIMATED_BAR_RELATIVE_X_OFFSET =
    50.0f / TEMPERATURE_BAR_ORIGINAL_IMAGE_WIDTH

/**
 * Relative y offset of animated temperature bar in pixels.
 */
private const val TEMPERATURE_BAR_ANIMATED_BAR_RELATIVE_Y_OFFSET =
    16.0f / TEMPERATURE_BAR_ORIGINAL_IMAGE_HEIGHT

/**
 * Relative max width of animated temperature bar in pixels.
 */
private const val TEMPERATURE_BAR_ANIMATED_BAR_RELATIVE_MAX_WIDTH =
    188.0f / TEMPERATURE_BAR_ORIGINAL_IMAGE_WIDTH

/**
 * Relative height of animated temperature bar in pixels.
 */
private const val TEMPERATURE_BAR_ANIMATED_BAR_RELATIVE_HEIGHT =
    17.0f / TEMPERATURE_BAR_ORIGINAL_IMAGE_HEIGHT

/**
 * RSSI value in dBm for device that is very close to the phone.
 */
private const val RSSI_VALUE_NEAR_PHONE = -40f

/**
 * RSSI value in dBm for device that is far away from the phone.
 */
private const val RSSI_VALUE_FAR_AWAY = -100f

/**
 * Minimum fraction value for RSSI proximity thermometer.
 */
private const val RSSI_PROXIMITY_METER_MIN_FRACTION = 0.01f

/**
 * Maximum fraction value for RSSI proximity thermometer.
 */
private const val RSSI_PROXIMITY_METER_MAX_FRACTION = 1f

/**
 * Defines screen for finding a device.
 * @param device Device to find.
 * @param deviceLocation Location of the device.
 * @param deviceLocationRssi Value of RSSI for the device at the location.
 * @param currentLocation Current location of the phone.
 * @param currentRssi Currently measured RSSI for the device.
 * @param azimuthDegrees Current azimuth of the phone in degrees.
 * @param onResumeSensors Callback used when resuming sensors.
 * @param onPauseSensors Callback used when pausing sensors.
 * @param modifier Modifier to apply to the layout.
 * @param mapContent Content of the map.
 */
@Composable
fun FindDeviceScreen(
    device: DeviceEntity?,
    deviceLocation: LocationEntity?,
    deviceLocationRssi: Float,
    currentLocation: LocationData?,
    currentRssi: Float?,
    azimuthDegrees: Float,
    onResumeSensors: () -> Unit,
    onPauseSensors: () -> Unit,
    modifier: Modifier = Modifier,
    mapContent: (@Composable () -> Unit)? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // register listener which will restart / stop Bluetooth, location and sensor updates
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> onResumeSensors()
                Lifecycle.Event.ON_STOP -> onPauseSensors()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (device == null || deviceLocation == null) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize().padding(48.dp)
        ) {
            Text(
                text = stringResource(R.string.device_not_found_caption),
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )

            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(32.dp).size(128.dp)
            )

            Text(
                text = stringResource(R.string.device_or_location_not_found),
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DeviceHeaderCard(
                deviceName = device.name,
                macAddress = device.macAddress,
                locationTimestamp = deviceLocation.timestamp.toFormattedString(),
                deviceLocation = deviceLocation,
                deviceLocationRssi = deviceLocationRssi,
                currentLocation = currentLocation,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.BottomEnd
            ) {
                if (mapContent != null) {
                    mapContent()
                } else {
                    OsmDeviceLocatorMap(
                        device = device,
                        deviceLocation = deviceLocation,
                        deviceLocationRssi = deviceLocationRssi,
                        currentLocation = currentLocation
                    )
                }
            }

            SensorDashboardCard(
                currentRssi = currentRssi,
                azimuthDegrees = azimuthDegrees
            )
        }
    }
}

/**
 * Defines header card for the FindDevice screen.
 * @param deviceName Name of the device.
 * @param macAddress MAC address of the device.
 * @param locationTimestamp Timestamp of the location.
 * @param deviceLocation Location of the device.
 * @param deviceLocationRssi Value of RSSI for the device at the location.
 * @param currentLocation Current location of the phone.
 */
@Composable
private fun DeviceHeaderCard(
    deviceName: String,
    macAddress: String,
    locationTimestamp: String,
    deviceLocation: LocationEntity,
    deviceLocationRssi: Float,
    currentLocation: LocationData?,
) {
    val gpsDistance: Double? = if (currentLocation != null) deviceLocation.calculateDistanceTo(
        currentLocation.latitude, currentLocation.longitude) else null
    val netAccuracy: Double? = if (currentLocation != null) deviceLocation.calculateNetAccuracy(
        currentLocation.accuracy, deviceLocationRssi) else null

    Column(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.finding_device_name).format(deviceName),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.mac_address_label).format(macAddress),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon (
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = stringResource(R.string.location_timestamp_icon),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = locationTimestamp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Text(
            text = if (gpsDistance != null && netAccuracy != null) {
                stringResource(R.string.gps_distance_acquired)
                    .format(gpsDistance.roundToInt(), netAccuracy.roundToInt())
            } else {
                stringResource(R.string.gps_distance_calculating)
            }
        )
    }
}

/**
 * Currently a placeholder for Google Maps.
 */
@Composable
private fun MapPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Zde bude Google Mapa",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Defines card with sensor readings.
 * @param currentRssi Currently measured RSSI for the device.
 * @param azimuthDegrees Current azimuth of the phone in degrees.
 */
@Composable
private fun SensorDashboardCard(
    currentRssi: Float?,
    azimuthDegrees: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CompassWidget(azimuthDegrees = azimuthDegrees)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (currentRssi != null) {
                    stringResource(R.string.signal_strength_active_label)
                        .format(currentRssi.toInt())
                } else {
                    stringResource(R.string.signal_strength_inactive_label)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 10.dp)
            )

            RssiProximityThermometer(currentRssi = currentRssi)
        }
    }
}

/**
 * Defines appearance and behavior for a compass widget.
 * @param azimuthDegrees Current azimuth of the phone in degrees.
 */
@Composable
private fun CompassWidget(azimuthDegrees: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.compass),
                contentDescription = stringResource(R.string.compass_body_image),
                modifier = Modifier.fillMaxSize()
            )

            Image(
                painter = painterResource(id = R.drawable.compass_arrow),
                contentDescription = stringResource(R.string.compass_arrow_image),
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(azimuthDegrees)
            )
        }
    }
}

/**
 * Defines appearance and behavior for a RSSI proximity thermometer.
 * @param currentRssi Currently measured RSSI for the device.
 */
@Composable
private fun RssiProximityThermometer(currentRssi: Float?) {
    val targetFraction = if (currentRssi != null) {
        val offsetValue = currentRssi - RSSI_VALUE_FAR_AWAY
        val valuesRange = abs(RSSI_VALUE_NEAR_PHONE - RSSI_VALUE_FAR_AWAY)

        (offsetValue / valuesRange)
            .coerceIn(RSSI_PROXIMITY_METER_MIN_FRACTION, RSSI_PROXIMITY_METER_MAX_FRACTION)
    } else {
        RSSI_PROXIMITY_METER_MIN_FRACTION
    }

    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        label = "thermometerFill"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.TopStart
    ) {
        Box(
            modifier = Modifier
                .width((TEMPERATURE_BAR_ANIMATED_BAR_RELATIVE_MAX_WIDTH * animatedFraction * maxWidth.value).dp)
                .height((TEMPERATURE_BAR_ANIMATED_BAR_RELATIVE_HEIGHT * maxHeight.value).dp)
                .offset(
                    x = (TEMPERATURE_BAR_ANIMATED_BAR_RELATIVE_X_OFFSET * maxWidth.value).dp,
                    y = (TEMPERATURE_BAR_ANIMATED_BAR_RELATIVE_Y_OFFSET * maxHeight.value).dp)
                .background(colorResource(R.color.compass_north_fill))
        )

        Image(
            painter = painterResource(id = R.drawable.signal_bar),
            contentDescription = stringResource(R.string.proximity_thermometer_image),
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Provides preview of FindDevice screen.
 */
@Preview(showBackground = true)
@Composable
private fun FindDeviceScreenPreview() {
    val mockDevice = DeviceEntity(
        1,
        "Sony WH-1000XM5",
        "AC:80:0A:12:34:56"
    )

    val mockLocation = LocationEntity(
        locationId = 1,
        latitude = 48.1486,
        longitude = 17.1077,
        accuracy = 20.0,
        epochMilliseconds = Clock.System.now().toEpochMilliseconds()
    )

    val mockCurrentLocation = LocationData(
        latitude = 47.1,
        longitude = 16.0,
        accuracy = 10.0,
        timeStamp = Clock.System.now()
    )

    MaterialTheme {
        FindDeviceScreen(
            device = mockDevice,
            deviceLocation = mockLocation,
            deviceLocationRssi = -50.0f,
            currentLocation = mockCurrentLocation,
            currentRssi = -70f,
            azimuthDegrees = 90f,
            onResumeSensors = {},
            onPauseSensors = {},
            mapContent = {MapPlaceholder()}
        )
    }
}

/**
 * Formats instant to DD-MM-YYYY HH:mm string.
 */
private fun Instant.toFormattedString(): String {
    val customFormat = DateTimeComponents.Format {
        day(); chars(". "); monthNumber(); chars(". "); year()
        char(' ')
        hour(); char(':'); minute()
    }

    val systemZone = TimeZone.currentSystemDefault()
    val offset = systemZone.offsetAt(this)

    return this.format(customFormat, offset)
}