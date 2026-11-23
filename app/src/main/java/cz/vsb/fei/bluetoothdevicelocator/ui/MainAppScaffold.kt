package cz.vsb.fei.bluetoothdevicelocator.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import cz.vsb.fei.bluetoothdevicelocator.R
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceData
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceEntity
import cz.vsb.fei.bluetoothdevicelocator.data.LocationData
import cz.vsb.fei.bluetoothdevicelocator.data.LocationEntity
import cz.vsb.fei.bluetoothdevicelocator.viewmodel.LocatorViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random
import kotlin.time.Clock

/**
 * Scaffold used to render the Bluetooth Locator application.
 * @param viewModel Locator view model.
 * @param onStartBluetoothScan Callback used when starting a Bluetooth scan.
 * @param onDeviceStoredSuccessfully Callback used when a device is successfully stored.
 * @param onLocationUnavailable Callback used when device's location is unavailable.
 * @param initializeFindDeviceScreen Callback used to initialize FindDevice screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: LocatorViewModel,
    onStartBluetoothScan: () -> Unit,
    onDeviceStoredSuccessfully: () -> Unit,
    onLocationUnavailable: () -> Unit,
    initializeFindDeviceScreen: (DeviceEntity?, LocationEntity?, Float?) -> Unit
) {

    /**
     * Map of currently saved devices as a state.
     */
    val savedDevices by viewModel.allDevices.collectAsState()

    /**
     * List of active nearby devices from a single scan as a state.
     */
    val nearbyDevicesSingleScan by viewModel.nearbyDevicesSingleScan.collectAsState()

    /**
     * Flag whether Bluetooth scanning is now in progress as a state.
     */
    val isBluetoothScanning by viewModel.isBluetoothPerformingSingleScan.collectAsState()

    /**
     * Current screen to render.
     */
    val currentScreen by viewModel.currentScreen.collectAsState()

    /**
     * RSSI values mapped to device-location pairs as a state.
     */
    val devicesLocationsSignalData by viewModel.devicesLocationsSignalData.collectAsState()

    BackHandler(
        enabled = currentScreen != IScreen.Menu,
        onBack = viewModel::navigateBack
    )

    Scaffold(
        topBar = {
            if (currentScreen != IScreen.Menu) {
                /* render navigation bar when not in main menu */
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentScreen) {
                                IScreen.StoreNearbyDevices ->
                                    stringResource(R.string.store_nearby_devices)
                                IScreen.ShowTrackedDevices ->
                                    stringResource(R.string.show_tracked_devices)
                                is IScreen.SingleDeviceLocations ->
                                    stringResource(R.string.select_location)
                                is IScreen.FindDevice ->
                                    stringResource(R.string.find_device)
                                else -> ""
                            }
                        )
                    },
                    navigationIcon = {
                        val contentDescription: String? = when(currentScreen) {
                            IScreen.ShowTrackedDevices, IScreen.StoreNearbyDevices ->
                                stringResource(R.string.back_to_menu)
                            is IScreen.SingleDeviceLocations, is IScreen.FindDevice ->
                                stringResource(R.string.back_to_tracked_devices)
                            else -> null
                        }

                        if (contentDescription != null) {
                            IconButton(
                                onClick = viewModel::navigateBack
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = contentDescription
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            IScreen.Menu -> MenuScreen(
                onNavigateToStoreDevices = {
                    viewModel.changeCurrentScreen(IScreen.StoreNearbyDevices)
                },
                onNavigateToTrackedDevices = {
                    viewModel.changeCurrentScreen(IScreen.ShowTrackedDevices)
                },
                modifier = Modifier.padding(innerPadding)
            )

            IScreen.ShowTrackedDevices -> {
                val devicesLastLocations by viewModel.devicesLastLocations.collectAsState()

                ShowTrackedDevicesScreen(
                    devices = savedDevices.values.toList(),
                    onSelectLocation = { deviceId ->
                        viewModel.changeCurrentScreen(
                            IScreen.SingleDeviceLocations(savedDevices[deviceId])
                        )
                    },
                    onDeleteDevice = { deviceId ->
                        viewModel.removeDevice(deviceId)
                    },
                    onUseLastLocation = { deviceId ->
                        val device = savedDevices[deviceId]
                        val location = if (device != null) devicesLastLocations[device] else null
                        val deviceLocationPair = Pair(
                            device?.deviceId ?: -1, location?.locationId ?: -1
                        )
                        val deviceLocationRssi = devicesLocationsSignalData[deviceLocationPair]

                        initializeFindDeviceScreen(device,location, deviceLocationRssi)
                    },
                    onDeleteAllDevices = { viewModel.removeAllDevices() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is IScreen.SingleDeviceLocations -> {
                val device = (currentScreen as IScreen.SingleDeviceLocations).device
                val deviceLocations by viewModel
                    .getLocationsForDevice(device?.deviceId)
                    .collectAsState(initial = emptyMap())

                SingleDeviceLocationsScreen(
                    device = device,
                    locations = deviceLocations.values.toList(),
                    onSelectSpecificLocation = { locationId ->
                        val location = deviceLocations[locationId]
                        val deviceLocationPair = Pair(device?.deviceId ?: -1, locationId)
                        val deviceLocationRssi = devicesLocationsSignalData[deviceLocationPair]

                        initializeFindDeviceScreen(device, location, deviceLocationRssi)
                    },
                    onDeleteLocation = { locationId ->
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

            IScreen.StoreNearbyDevices -> {
                StoreNearbyDevicesScreen(
                    foundDevices = nearbyDevicesSingleScan,
                    isScanning = isBluetoothScanning,
                    onStartScan = onStartBluetoothScan,
                    onStopScan = viewModel::stopBluetoothScan,
                    onSaveDevice = { deviceData ->
                        viewModel.saveDeviceWithLocation(
                            deviceData = deviceData,
                            onSuccess = onDeviceStoredSuccessfully,
                            onUnavailableLocation = onLocationUnavailable
                        )
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is IScreen.FindDevice -> {
                val nearbyDevicesContinuousScan by viewModel.nearbyDevicesContinuousScan
                    .collectAsState()

                val azimuthAngle by viewModel.azimuthAngle.collectAsState()
                val device = (currentScreen as IScreen.FindDevice).device
                val location = (currentScreen as IScreen.FindDevice).location
                val deviceLocationRssi = (currentScreen as IScreen.FindDevice).deviceLocationRssi
                    ?: 0f
                val currentLocation by viewModel.currentLocation.collectAsState()
                val currentDeviceRssi = nearbyDevicesContinuousScan[device?.macAddress]?.rssi

                FindDeviceScreen(
                    device = device,
                    deviceLocation = location,
                    deviceLocationRssi = deviceLocationRssi,
                    currentLocation = currentLocation,
                    currentRssi = currentDeviceRssi,
                    azimuthDegrees = azimuthAngle,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            else -> {}
        }
    }
}

/**
 * Provides preview of MainAppScaffold.
 */
@Preview(showBackground = true)
@Composable
fun MainAppScaffoldPreview() {
    val mockViewModel = object: LocatorViewModel() {
        val devices = arrayOf (
            DeviceEntity(1, "Device A", "AA:BB:CC:DD:EE:FF"),
            DeviceEntity(2, "Device B", "AA:BB:11:DD:33:FF"),
            DeviceEntity(3, "Device C", "AA:BB:22:DD:44:FF")
        )

        val locations = arrayOf(
            LocationEntity(1L, 49.8306, 18.1624, 21.5, Clock.System.now().toEpochMilliseconds()),
            LocationEntity(2L, 49.8307, 18.1625, 50.0, Clock.System.now().toEpochMilliseconds() - 50000)
        )

        var _currentScreen = MutableStateFlow<IScreen>(IScreen.Menu)

        override val allDevices = MutableStateFlow(devices.associateBy { it.deviceId }.toMap())
        override val azimuthAngle = MutableStateFlow(0f)
        override var cachedFindDeviceScreen: IScreen.FindDevice = IScreen.FindDevice(null, null, null)
        override val currentLocation = MutableStateFlow(LocationData(49.8306, 18.1624, 21.5, Clock.System.now()))
        override val currentScreen = _currentScreen
        override val devicesLastLocations = MutableStateFlow(devices.associateWith { locations.first() })
        override val devicesLocationsSignalData = MutableStateFlow<Map<Pair<Long, Long>, Float>>(emptyMap())
        override val isBluetoothPerformingSingleScan = MutableStateFlow(false)
        override val nearbyDevicesSingleScan = MutableStateFlow(devices.map { DeviceData(it.name, it.macAddress,
            Random.nextDouble(-100.0, -20.0).toFloat()) }.toList())
        override val nearbyDevicesContinuousScan = MutableStateFlow<Map<String, DeviceData>>(emptyMap())
        override fun areLocationServicesEnabled(): Boolean = true
        override fun startBluetoothScan(isContinuous: Boolean) {}
        override fun startCompassUpdates() {}
        override fun startLocationUpdates() {}
        override fun stopBluetoothScan() {}
        override fun isBluetoothEnabled(): Boolean = true
        override fun changeCurrentScreen(screen: IScreen) {_currentScreen.update { screen }}
        override fun getLocationsForDevice(deviceId: Long?): StateFlow<Map<Long, LocationEntity>> {
            return MutableStateFlow(locations.associateBy { it.locationId }.toMap())
        }
        override fun saveDeviceWithLocation(
            deviceData: DeviceData,
            onSuccess: () -> Unit,
            onUnavailableLocation: () -> Unit
        ) {}
        override fun navigateBack() {
            when (_currentScreen.value) {
                IScreen.StoreNearbyDevices, IScreen.ShowTrackedDevices -> {
                    _currentScreen.value = IScreen.Menu
                }
                is IScreen.SingleDeviceLocations, is IScreen.FindDevice -> {
                    _currentScreen.value = IScreen.ShowTrackedDevices
                }
                else -> {}
            }
        }
        override fun removeDevice(deviceId: Long) {}
        override fun removeAllDevices() {}
        override fun removeDeviceLocation(deviceId: Long, locationId: Long) {}
        override fun removeDeviceLocationsButLast(deviceId: Long) {}
    }

    MaterialTheme {
        MainAppScaffold(
            mockViewModel,
            {},
            {},
            {},
            {_, _, _ -> }
        )
    }
}