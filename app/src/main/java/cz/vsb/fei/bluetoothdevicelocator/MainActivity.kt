package cz.vsb.fei.bluetoothdevicelocator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceEntity
import cz.vsb.fei.bluetoothdevicelocator.data.LocationEntity
import cz.vsb.fei.bluetoothdevicelocator.ui.ShowTrackedDevicesScreen
import cz.vsb.fei.bluetoothdevicelocator.ui.SingleDeviceLocationsScreen
import cz.vsb.fei.bluetoothdevicelocator.ui.StoreNearbyDevicesScreen
import kotlin.time.Clock

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BluetoothDeviceLocatorTheme {
                MainAppScaffold()
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
fun MainAppScaffold(defaultScreen: IScreen = IScreen.Menu) {

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

    /**
     * Temporary list of saved devices used for testing.
     */
    val mockSavedDevices = remember {
        mutableStateListOf(
            DeviceEntity(deviceId = 1, name = "Moje sluchátka", macAddress = "AA:BB:CC:11:22:33"),
            DeviceEntity(deviceId = 2, name = "Škoda Fabia", macAddress = "44:55:66:DD:EE:FF")
        )
    }

    /**
     * Temporary list of locations used for testing.
     */
    val mockLocations = remember {
        mutableStateListOf(
            LocationEntity(
                locationId = 101,
                latitude = 49.8306,
                longitude = 18.1624,
                accuracy = 4.2,
                epochMilliseconds = Clock.System.now().toEpochMilliseconds()),
            LocationEntity(locationId = 102,
                latitude = 49.8312,
                longitude = 18.1610,
                accuracy = 12.5,
                epochMilliseconds = Clock.System.now().toEpochMilliseconds() - 60000)
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
                                IScreen.FindDevice -> stringResource(R.string.find_device)
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
                            is IScreen.SingleDeviceLocations, IScreen.FindDevice -> {
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

                IScreen.ShowTrackedDevices -> ShowTrackedDevicesScreen(
                    devices = mockSavedDevices,
                    onSelectLocation = { deviceId ->
                        currentScreen = IScreen.SingleDeviceLocations(
                            mockSavedDevices.find {
                                device -> device.deviceId == deviceId
                            }
                        )
                    },
                    onDeleteDevice = { deviceId ->
                        mockSavedDevices.removeIf { device -> device.deviceId == deviceId }
                    },
                    onUseLastLocation = { currentScreen = IScreen.FindDevice },
                    onDeleteAllDevices = { mockSavedDevices.clear() },
                    modifier = Modifier.padding(innerPadding)
                )

                is IScreen.SingleDeviceLocations -> SingleDeviceLocationsScreen(
                    device = (currentScreen as IScreen.SingleDeviceLocations).device,
                    locations = mockLocations,
                    onSelectSpecificLocation = {deviceId, locationId ->
                        currentScreen = IScreen.FindDevice
                    },
                    onDeleteLocation = {locationId ->
                        mockLocations.removeIf { location -> location.locationId == locationId }
                    },
                    onDeleteAllLocationsButRecent = {deviceId ->
                        mockLocations.removeIf { location ->
                            location.epochMilliseconds != mockLocations.maxOf { l ->
                                l.epochMilliseconds
                            }
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                )

                IScreen.StoreNearbyDevices -> StoreNearbyDevicesScreen(
                    foundDevices = mockScannedList,
                    isScanning = false,
                    onStartScan = {},
                    onStopScan = {},
                    onSaveDevice = {
                        mockSavedDevices.add(
                            DeviceEntity(
                                deviceId = (mockSavedDevices.size + 1).toLong(),
                                name = it.name, macAddress = it.macAddress
                            )
                        )
                    },
                    modifier = Modifier.padding(innerPadding)
                )

                IScreen.FindDevice -> {
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