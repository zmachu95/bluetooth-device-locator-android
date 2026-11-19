package cz.vsb.fei.bluetoothdevicelocator

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cz.vsb.fei.bluetoothdevicelocator.ui.theme.BluetoothDeviceLocatorTheme
import cz.vsb.fei.bluetoothdevicelocator.ui.MenuScreen
import cz.vsb.fei.bluetoothdevicelocator.ui.IScreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceData
import cz.vsb.fei.bluetoothdevicelocator.data.LocatorRepository
import cz.vsb.fei.bluetoothdevicelocator.ui.ShowTrackedDevicesScreen
import cz.vsb.fei.bluetoothdevicelocator.ui.SingleDeviceLocationsScreen
import cz.vsb.fei.bluetoothdevicelocator.ui.StoreNearbyDevicesScreen
import cz.vsb.fei.bluetoothdevicelocator.viewmodel.LocatorViewModel
import cz.vsb.fei.bluetoothdevicelocator.viewmodel.LocatorViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: LocatorViewModel by viewModels {
        LocatorViewModelFactory(LocatorRepository(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BluetoothDeviceLocatorTheme {
                MainAppScaffold(viewModel)
            }
        }
    }
}

/**
 * Scaffold used to render the application.
 * @param defaultScreen Default screen to render. To be used in preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: LocatorViewModel, defaultScreen: IScreen = IScreen.Menu) {
    val savedDevices by viewModel.allDevices.collectAsState()

    /**
     * Current screen to render.
     */
    var currentScreen by remember { mutableStateOf<IScreen>(defaultScreen) }

    /**
     * Temporary list of scanned devices used for testing.
     */
    val mockScannedList = remember {
        mutableStateListOf(
            DeviceData(name = "JBL Flip 6", macAddress = "00:11:22:33:44:55", rssi = -55f),
            DeviceData(name = "Unknown Beacon", macAddress = "AA:CC:EE:11:22:33", rssi = -78f),
            DeviceData(name = "Galaxy Watch", macAddress = "44:55:66:77:88:99", rssi = -92f)
        )
    }

    Scaffold(
        topBar = {
            if (currentScreen != IScreen.Menu) {
                /* render navigation bar when not in main menu */
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentScreen) {
                                IScreen.StoreNearbyDevices -> stringResource(R.string.store_nearby_devices)
                                IScreen.ShowTrackedDevices -> stringResource(R.string.show_tracked_devices)
                                is IScreen.SingleDeviceLocations -> stringResource(R.string.select_location)
                                is IScreen.FindDevice -> stringResource(R.string.find_device)
                                else -> ""
                            }
                        )
                    },
                    navigationIcon = {
                        when (currentScreen) {
                            IScreen.ShowTrackedDevices, IScreen.StoreNearbyDevices -> {
                                IconButton(
                                    onClick = { currentScreen = IScreen.Menu }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.back_to_menu)
                                    )
                                }
                            }
                            is IScreen.SingleDeviceLocations, is IScreen.FindDevice -> {
                                IconButton(
                                    onClick = { currentScreen = IScreen.ShowTrackedDevices }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.back_to_tracked_devices)
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
            when (currentScreen) {
                IScreen.Menu -> MenuScreen(
                    onNavigateToStoreDevices = { currentScreen = IScreen.StoreNearbyDevices },
                    onNavigateToTrackedDevices = { currentScreen = IScreen.ShowTrackedDevices },
                    modifier = Modifier.padding(innerPadding)
                )

                IScreen.ShowTrackedDevices -> {
                    val devicesLastLocations by viewModel.devicesLastLocations.collectAsState()

                    ShowTrackedDevicesScreen(
                        devices = savedDevices.values.toList(),
                        onSelectLocation = { deviceId ->
                            currentScreen = IScreen
                                .SingleDeviceLocations(savedDevices[deviceId])
                        },
                        onDeleteDevice = { deviceId ->
                            viewModel.removeDevice(deviceId)
                        },
                        onUseLastLocation = {deviceId ->
                            val device = savedDevices[deviceId]
                            val location = if (device != null) devicesLastLocations[device] else null

                            currentScreen = IScreen.FindDevice(device, location)
                        },
                        onDeleteAllDevices = { viewModel.removeAllDevices() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                is IScreen.SingleDeviceLocations -> {
                    val device = (currentScreen as IScreen.SingleDeviceLocations).device
                    val deviceLocations by viewModel
                        .getLocationsForDevice(device?.deviceId)
                        .collectAsState( initial = emptyMap())

                    SingleDeviceLocationsScreen(
                        device = device,
                        locations = deviceLocations.values.toList(),
                        onSelectSpecificLocation = {locationId ->
                            currentScreen = IScreen.FindDevice(device, deviceLocations[locationId])
                        },
                        onDeleteLocation = {locationId ->
                            if (device != null) {
                                viewModel.removeDeviceLocation(device.deviceId, locationId)
                            }
                        },
                        onDeleteAllLocationsButRecent = {
                            if (device != null) {
                                viewModel.removeDeviceLocationsButLast(device.deviceId)
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                IScreen.StoreNearbyDevices -> StoreNearbyDevicesScreen(
                    foundDevices = mockScannedList,
                    isScanning = false,
                    onStartScan = {},
                    onStopScan = {},
                    onSaveDevice = { deviceData ->
                        viewModel.saveDeviceWithMockLocation(deviceData)
                    },
                    modifier = Modifier.padding(innerPadding)
                )

                is IScreen.FindDevice -> {
                    val device = (currentScreen as IScreen.FindDevice).device
                    val location = (currentScreen as IScreen.FindDevice).location

                    Log.i("MainActivity", "Device name: ${device?.name}, location timestamp: ${location?.timestamp}")

                    // TODO: replace this with actual screen with Google Maps, compass and proximity alarm
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(R.drawable.find_device_mockup),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                else -> {}
        }
    }
}