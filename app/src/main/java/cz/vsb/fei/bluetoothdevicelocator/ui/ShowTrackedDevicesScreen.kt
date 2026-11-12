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
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
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
                    onClick = {
                        onDeleteAllDevices()
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
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text =  stringResource(R.string.cancel_button_caption)
                    )
                }
            }
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
                text = stringResource(R.string.select_device_prompt).format(devices.size),
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
                    /* Card design by Google Gemini */
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            onUseLastLocation(device.deviceId)
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = device.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Spacer(Modifier.height(2.dp))

                                    Text(
                                        text = stringResource(R.string.mac_address_label)
                                            .format(device.macAddress),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteDevice(device.deviceId) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.delete_device_icon_description),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )

                            Spacer(Modifier.height(8.dp))

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
                                    onClick = { onSelectLocation(device.deviceId) },
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
                    }
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