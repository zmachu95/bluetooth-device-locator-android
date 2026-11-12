package cz.vsb.fei.bluetoothdevicelocator.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceEntity
import cz.vsb.fei.bluetoothdevicelocator.data.LocationEntity
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.datetime.offsetAt
import cz.vsb.fei.bluetoothdevicelocator.R

/**
 * Formats instant to DD-MM-YYYY HH:mm string.
 */
private fun Instant.toFormattedString(): String {
    val customFormat = DateTimeComponents.Format {
        day(); char('-'); monthNumber(); char('-'); year()
        char(' ')
        hour(); char(':'); minute()
    }

    val systemZone = TimeZone.currentSystemDefault()
    val offset = systemZone.offsetAt(this)

    return this.format(customFormat, offset)
}

/**
 * Defines screen for showing saved locations of a single device.
 * @param device Device to show locations for.
 * @param locations List of saved locations for the device.
 * @param onSelectSpecificLocation Callback to use when selecting a specific location.
 * @param onDeleteLocation Callback to use when deleting a location.
 * @param onDeleteAllLocationsButRecent Callback to use when deleting all locations but the most recent.
 * @param modifier Modifier to apply to the layout.
 */
@Composable
fun SingleDeviceLocationsScreen(
    device: DeviceEntity?,
    locations: List<LocationEntity>,
    onSelectSpecificLocation: (deviceId: Long, locationId: Long) -> Unit,
    onDeleteLocation: (locationId: Long) -> Unit,
    onDeleteAllLocationsButRecent: (deviceId: Long) -> Unit,
    modifier: Modifier = Modifier
) {

    /**
     * Flag whether to show warning dialog about deleting all locations but the most recent.
     */
    var showDeleteDialog by remember { mutableStateOf(false) }

    /**
     * Flag whether to show warning dialog about trying to delete the last location.
     */
    var showCannotDeleteDialog by remember { mutableStateOf(false) }

    if (device != null) {
        /* show dialogs only when device is not null */
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.delete_location_history_caption)
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.delete_location_history_text)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteAllLocationsButRecent(device.deviceId)
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.delete_all_button_caption)
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text(
                            text = stringResource(R.string.cancel_button_caption)
                        )
                    }
                }
            )
        }

        if (showCannotDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showCannotDeleteDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.cannot_delete_last_location_caption)
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.cannot_delete_last_location_text)
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Block,
                        contentDescription = null
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { showCannotDeleteDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.continue_button_caption)
                        )
                    }
                }
            )
        }
    }

    if (device == null) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(48.dp)
        ) {
            Text(
                text = stringResource(R.string.device_not_found_caption),
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )

            Icon (
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(32.dp).size(128.dp)
            )

            Text(
                text = stringResource(R.string.device_not_found_text),
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.mac_address_label)
                    .format(device.macAddress),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.select_device_location_prompt)
                    .format(locations.size),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(locations) { location ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSpecificLocation(device.deviceId, location.locationId) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = location.timestamp.toFormattedString(),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge
                            )

                            IconButton(onClick = {
                                if (locations.size > 1) {
                                    onDeleteLocation(location.locationId)
                                } else {
                                    showCannotDeleteDialog = true
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete_device_location_icon_description),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                enabled = locations.size > 1,
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(
                    text = stringResource(R.string.delete_all_but_last_location_button_caption),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (locations.size > 1) MaterialTheme.colorScheme.error else
                        MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * Provides preview of screen with locations for single device.
 */
@Preview(showBackground = true)
@Composable
fun SingleDeviceLocationsPreview() {

    /**
     * Temporary list of locations used for testing.
     */
    val mockLocations = listOf(
        LocationEntity(locationId = 1, latitude = 1.0, longitude = 1.0, accuracy = 5.0, epochMilliseconds = Clock.System.now().toEpochMilliseconds() ),
        LocationEntity(locationId = 2, latitude = 1.0, longitude = 1.0, accuracy = 5.0, epochMilliseconds = Clock.System.now().toEpochMilliseconds() ),
        LocationEntity(locationId = 3, latitude = 1.0, longitude = 1.0, accuracy = 5.0, epochMilliseconds = Clock.System.now().toEpochMilliseconds() ),
        LocationEntity(locationId = 4, latitude = 1.0, longitude = 1.0, accuracy = 5.0, epochMilliseconds = Clock.System.now().toEpochMilliseconds() ),
        LocationEntity(locationId = 5, latitude = 1.0, longitude = 1.0, accuracy = 5.0, epochMilliseconds = Clock.System.now().toEpochMilliseconds() ),
    )

    /**
     * Temporary list with single location used for testing.
     */
    val mockLocationSingle = listOf(
        LocationEntity(locationId = 1, latitude = 1.0, longitude = 1.0, accuracy = 5.0, epochMilliseconds = Clock.System.now().toEpochMilliseconds() ),
    )

    /**
     * Temporary device used for testing.
     */
    val mockDevice = DeviceEntity(deviceId = 1, name = "Moje sluchátka", macAddress = "AA:BB:CC:11:22:33")

    MaterialTheme {
        SingleDeviceLocationsScreen(
            device = mockDevice,
            locations = mockLocationSingle,
            onSelectSpecificLocation = {deviceId, locationId -> },
            onDeleteLocation = {},
            onDeleteAllLocationsButRecent = {}
        )
    }
}