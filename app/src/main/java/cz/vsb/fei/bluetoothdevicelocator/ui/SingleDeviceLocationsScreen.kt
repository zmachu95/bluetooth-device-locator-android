package cz.vsb.fei.bluetoothdevicelocator.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
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
import kotlin.time.Clock
import cz.vsb.fei.bluetoothdevicelocator.R

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
    onSelectSpecificLocation: (locationId: Long) -> Unit,
    onDeleteLocation: (locationId: Long) -> Unit,
    onDeleteAllLocationsButRecent: () -> Unit,
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
            ConfirmDeleteDialog(
                onConfirm = {
                    onDeleteAllLocationsButRecent()
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false }
            )
        }

        if (showCannotDeleteDialog) {
            CannotDeleteDialog(
                onDismiss = { showCannotDeleteDialog = false }
            )
        }
    }

    if (device == null) {
        ErrorScreenContent(
            errorMessage = stringResource(R.string.device_not_found_text),
            modifier = modifier
        )
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
                text = if (locations.size == 1) {
                    stringResource(R.string.select_device_location_prompt_singular)
                } else {
                    stringResource(R.string.select_device_location_prompt_plural)
                }.format(locations.size),
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
                    DeviceCard(
                        onCardClick = { onSelectSpecificLocation(location.locationId) },
                        locationTimestampText = location.timestamp.toFormattedString(),
                        deleteLocationCallback =  {
                            if (locations.size > 1) {
                                onDeleteLocation(location.locationId)
                            } else {
                                showCannotDeleteDialog = true
                            }
                        }
                    )
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
 * Defines warning dialog for trying to delete the last location.
 * @param onDismiss Callback when dismissing the dialog.
 */
@Composable
private fun CannotDeleteDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                onClick = onDismiss,
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

/**
 * Defines warning dialog for deleting all locations but last.
 * @param onConfirm Callback when confirming deletion.
 * @param onDismiss Callback when dismissing the dialog.
 */
@Composable
private fun ConfirmDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                onClick = onConfirm,
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
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.cancel_button_caption)
                )
            }
        }
    )
}

/**
 * Defines a single device location card.
 * @param onCardClick Callback to use when clicking the card.
 * @param locationTimestampText Timestamp of the location.
 * @param deleteLocationCallback Callback to use when deleting the location.
 */
@Composable
private fun DeviceCard(
    onCardClick: () -> Unit,
    locationTimestampText: String,
    deleteLocationCallback: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onCardClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = locationTimestampText,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )

            IconButton(
                onClick = deleteLocationCallback
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_device_location_icon_description),
                    tint = MaterialTheme.colorScheme.error
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
            onSelectSpecificLocation = {locationId -> },
            onDeleteLocation = {},
            onDeleteAllLocationsButRecent = {}
        )
    }
}