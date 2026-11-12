package cz.vsb.fei.bluetoothdevicelocator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.outlined.FindReplace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.vsb.fei.bluetoothdevicelocator.R

/**
 * Defines main menu screen.
 * @param onNavigateToStoreDevices Callback to navigate to StoreNearbyDevices screen.
 * @param onNavigateToTrackedDevices Callback to navigate to ShowTrackedDevices screen.
 * @param modifier Modifier to apply to the layout.
 */
@Composable
fun MenuScreen(
    onNavigateToStoreDevices: () -> Unit,
    onNavigateToTrackedDevices: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.app_motto),
            fontStyle = FontStyle.Italic,
            style = MaterialTheme.typography.bodyLarge,
        )

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
            /* TODO: replace this with better image? */
            Icon(
                imageVector = Icons.Outlined.FindReplace,
                contentDescription = "Find and Replace icon",
                modifier = Modifier.size(400.dp),
                tint = MaterialTheme.colorScheme.secondary
            )

            Icon(
                imageVector = Icons.Filled.BluetoothConnected,
                contentDescription = "Bluetooth icon",
                modifier = Modifier.size(128.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = onNavigateToStoreDevices,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(
                text = stringResource(R.string.store_nearby_devices),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(56.dp))

        Button(
            onClick = onNavigateToTrackedDevices,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(
                text = stringResource(R.string.find_device),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Provides preview of Menu screen.
 */
@Preview(showBackground = true)
@Composable
fun MenuScreenPreview() {
    MaterialTheme {
        MenuScreen({}, {})
    }
}