package cz.vsb.fei.bluetoothdevicelocator.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceEntity
import cz.vsb.fei.bluetoothdevicelocator.R

/**
 * Defines screen for showing tracked devices.
 * @param devices List of tracked devices.
 * @param onUseLastLocation Callback to use when finding a device via its last location.
 * @param onSelectLocation Callback to use when selecting a specific device location.
 * @param onDeleteDevice Callback to use when deleting a device.
 * @param onDeleteAllDevices Callback to use when deleting all devices.
 * @param modifier Modifier to apply to the layout.
 */
@Composable
fun ShowTrackedDevicesScreen(
    devices: List<DeviceEntity>,
    onUseLastLocation: (deviceId: Long) -> Unit,
    onSelectLocation: (deviceId: Long) -> Unit,
    onDeleteDevice: (deviceId: Long) -> Unit,
    onDeleteAllDevices: () -> Unit,
    modifier: Modifier = Modifier
) {

    /**
     * Flag whether to show warning dialog about deleting all devices.
     */
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        ConfirmDeleteAllDevicesDialog(
            onConfirm = {
                onDeleteAllDevices()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        if (devices.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = stringResource(R.string.no_devices_to_show),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Text(
                text = if (devices.size == 1) {
                    stringResource(R.string.select_device_prompt_singular)
                } else {
                    stringResource(R.string.select_device_prompt_plural)
                }.format(devices.size),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(devices) { device ->
                    DeviceCard(
                        deviceName = device.name,
                        deviceMacAddress = device.macAddress,
                        deleteDeviceCallback = { onDeleteDevice(device.deviceId) },
                        onCardClick = { onUseLastLocation(device.deviceId) },
                        selectLocationCallback = { onSelectLocation(device.deviceId) }
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(
                    text = stringResource(R.string.delete_all_devices_button_caption),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Defines warning dialog for deleting all devices.
 * @param onConfirm Callback when confirming deletion.
 * @param onDismiss Callback when dismissing the dialog.
 */
@Composable
private fun ConfirmDeleteAllDevicesDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.delete_all_devices_caption)
            )
        },
        text = {
            Text(
                text = stringResource(R.string.delete_all_devices_text)
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
                    text =  stringResource(R.string.cancel_button_caption)
                )
            }
        }
    )
}

/**
 * Defines a single device card. Design by Google Gemini.
 * @param deviceName Name of the device.
 * @param deviceMacAddress MAC address of the device.
 * @param deleteDeviceCallback Callback to use when deleting the device.
 * @param onCardClick Callback to use when clicking the card.
 * @param selectLocationCallback Callback to use when selecting a specific device location.
 */
@Composable
private fun DeviceCard(
    deviceName: String,
    deviceMacAddress: String,
    deleteDeviceCallback: () -> Unit,
    onCardClick: () -> Unit,
    selectLocationCallback: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onCardClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            DeviceCardTopRow(
                deviceName = deviceName,
                deviceMacAddress = deviceMacAddress,
                deleteDeviceCallback = deleteDeviceCallback
            )

            Spacer(Modifier.height(12.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Spacer(Modifier.height(8.dp))

            DeviceCardBottomRow(
                selectLocationCallback = selectLocationCallback
            )
        }
    }
}

/**
 * Defines bottom row of the device card.
 * @param selectLocationCallback Callback to use when selecting a specific device location.
 */
@Composable
private fun DeviceCardBottomRow(
    selectLocationCallback: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.tracked_devices_card_prompt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedButton(
            onClick = selectLocationCallback,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(16.dp))

            Spacer(Modifier.width(4.dp))

            Text(
                text = stringResource(R.string.show_history_button_caption),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Defines top row of the device card.
 * @param deviceName Name of the device.
 * @param deviceMacAddress MAC address of the device.
 * @param deleteDeviceCallback Callback to use when deleting the device.
 */
@Composable
private fun DeviceCardTopRow(
    deviceName: String,
    deviceMacAddress: String,
    deleteDeviceCallback: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = deviceName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = stringResource(R.string.mac_address_label).format(deviceMacAddress),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        IconButton(
            onClick = deleteDeviceCallback
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete_device_icon_description),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Provides preview of tracked devices screen.
 */
@Preview(showBackground = true)
@Composable
fun ShowTrackedDevicesScreenPreview() {

    /**
     * Temporary list of devices used for testing.
     */
    val mockDevicesFull = listOf(
        DeviceEntity(deviceId = 1, name = "Moje sluchátka", macAddress = "AA:BB:CC:11:22:33"),
        DeviceEntity(deviceId = 2, name = "Klíčenka", macAddress = "44:55:66:DD:EE:FF"),
        DeviceEntity(deviceId = 3, name = "Xiaomi Watch", macAddress = "44:55:66:DD:EE:FF"),
        DeviceEntity(deviceId = 4, name = "Samsung Galaxy", macAddress = "44:55:66:DD:EE:FF"),
        DeviceEntity(deviceId = 5, name = "Desktop AE15DE", macAddress = "44:55:66:DD:EE:FF"),
    )

    /**
     * Temporary empty list of devices used for testing.
     */
    val mockDevicesEmpty: List<DeviceEntity> = emptyList()

    MaterialTheme {
        ShowTrackedDevicesScreen(
            devices = mockDevicesFull,
            onUseLastLocation = {},
            onSelectLocation = {},
            onDeleteDevice = {},
            onDeleteAllDevices =  {})
    }
}