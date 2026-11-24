package cz.vsb.fei.bluetoothdevicelocator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceData
import cz.vsb.fei.bluetoothdevicelocator.R

/**
 * Defines screen for storing nearby devices.
 * @param foundDevices List of found devices.
 * @param isScanning Flag whether scanning is in progress.
 * @param onStartScan Callback to use when starting scan.
 * @param onStopScan Callback to use when stopping scan.
 * @param onSaveDevice Callback to use when saving a device.
 * @param modifier Modifier to apply to the layout.
 */
@Composable
fun StoreNearbyDevicesScreen(
    foundDevices: List<DeviceData>,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onSaveDevice: (device: DeviceData) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        ScreenHeader(
            isScanning = isScanning,
            foundDevicesCount = foundDevices.size
        )

        Spacer(Modifier.height(16.dp))

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.height(16.dp))

        if (foundDevices.isEmpty() && !isScanning) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_devices_found_yet_label),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(foundDevices) { device ->
                    DeviceCard(
                        deviceName = device.name,
                        deviceMacAddress = device.macAddress,
                        deviceRssi = device.rssi,
                        isSaveButtonEnabled = !isScanning,
                        onSaveDevice = { onSaveDevice(device) }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { if (isScanning) onStopScan() else onStartScan() },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isScanning) MaterialTheme.colorScheme.errorContainer else
                    MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (isScanning) MaterialTheme.colorScheme.onErrorContainer else
                    MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(
                imageVector = if (isScanning) Icons.Default.Close else Icons.Default.Refresh,
                contentDescription = if (isScanning)
                    stringResource(R.string.stop_scan_button_caption) else
                    stringResource(R.string.start_scan_button_caption)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = if (isScanning)
                    stringResource(R.string.stop_scan_button_caption) else
                    stringResource(R.string.start_scan_button_caption)
            )
        }
    }
}

/**
 * Defines a header for the StoreNearbyDevices screen.
 * @param isScanning Flag whether scanning is in progress.
 * @param foundDevicesCount Number of found devices.
 */
@Composable
private fun ScreenHeader(
    isScanning: Boolean,
    foundDevicesCount: Int
) {
    Text(
        text = stringResource(R.string.scan_for_bt_devices_label),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(16.dp))

    if (isScanning) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.scan_in_progress_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    } else if (foundDevicesCount > 0) {
        Text(
            text = stringResource(R.string.found_devices_label)
                .format(foundDevicesCount, if (foundDevicesCount > 1)
                    stringResource(R.string.found_devices_label_plural) else
                    stringResource(R.string.found_devices_label_singular)
                ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Defines a single device card.
 * @param deviceName Name of the device.
 * @param deviceMacAddress MAC address of the device.
 * @param deviceRssi Value of RSSI for the device.
 * @param isSaveButtonEnabled Flag whether save button is enabled.
 * @param onSaveDevice Callback to use when saving a device.
 */
@Composable
private fun DeviceCard(
    deviceName: String,
    deviceMacAddress: String,
    deviceRssi: Float,
    isSaveButtonEnabled: Boolean,
    onSaveDevice: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deviceName.ifBlank {
                        stringResource(R.string.unknown_device_name)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.mac_address_label)
                        .format(deviceMacAddress),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(4.dp))

                val signalColor = when {
                    deviceRssi > -60 -> MaterialTheme.colorScheme.primary // strong signal
                    deviceRssi > -80 -> MaterialTheme.colorScheme.secondary // moderate signal
                    else -> MaterialTheme.colorScheme.error // weak signal
                }
                Text(
                    text = stringResource(R.string.signal_text)
                        .format(deviceRssi),
                    style = MaterialTheme.typography.bodySmall,
                    color = signalColor
                )
            }

            Button(
                enabled = isSaveButtonEnabled,
                onClick = onSaveDevice,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.save_position_button_caption),
                )
            }
        }
    }
}

/**
 * Provides preview of screen with storing nearby Bluetooth devices.
 */
@Preview(showBackground = true)
@Composable
fun StoreNearbyDevicesScreenPreview() {

    val mockScanned = listOf(
        DeviceData(name = "JBL Flip 6", macAddress = "00:11:22:33:44:55", rssi = -55f),
        DeviceData(name = "", macAddress = "AA:CC:EE:11:22:33", rssi = -72f),
        DeviceData(name = "Galaxy Watch", macAddress = "44:55:66:77:88:99", rssi = -91f)
    )

    val mockEmpty = emptyList<DeviceData>()

    MaterialTheme {
        StoreNearbyDevicesScreen(
            foundDevices = mockScanned,
            isScanning = true,
            onStartScan = {},
            onStopScan = {},
            onSaveDevice = {}
        )
    }
}