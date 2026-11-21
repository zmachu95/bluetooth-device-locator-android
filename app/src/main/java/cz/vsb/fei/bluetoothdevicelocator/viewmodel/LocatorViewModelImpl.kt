package cz.vsb.fei.bluetoothdevicelocator.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceData
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceEntity
import cz.vsb.fei.bluetoothdevicelocator.data.LocationData
import cz.vsb.fei.bluetoothdevicelocator.data.LocationEntity
import cz.vsb.fei.bluetoothdevicelocator.data.LocatorRepository
import cz.vsb.fei.bluetoothdevicelocator.hardware.BluetoothGateway
import cz.vsb.fei.bluetoothdevicelocator.hardware.LocationGateway
import cz.vsb.fei.bluetoothdevicelocator.ui.IScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 *  Provides implementation of Bluetooth device locator ViewModel.
 */
class LocatorViewModelImpl(
    private val repository: LocatorRepository,
    private val bluetoothGateway: BluetoothGateway,
    private val locationGateway: LocationGateway
) : LocatorViewModel() {

    private var _currentScreen = MutableStateFlow<IScreen>(IScreen.Menu)

    override val allDevices: StateFlow<Map<Long, DeviceEntity>> = repository.getAllDevices()
        .map { devices-> devices.associateBy { device -> device.deviceId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    override val currentScreen: StateFlow<IScreen> = _currentScreen

    override val devicesLastLocations: StateFlow<Map<DeviceEntity, LocationEntity?>> =
        repository.getDevicesLastLocation().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    override val nearbyDevices: StateFlow<List<DeviceData>> = bluetoothGateway.foundDevices
        .map { devices-> devices.values.sortedWith(this::nearbyDevicesComparator).toList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    override fun isBluetoothEnabled(): Boolean {
        return bluetoothGateway.isBluetoothEnabled()
    }

    override val isBluetoothScanning: StateFlow<Boolean> = bluetoothGateway.isScanning

    override fun areLocationServicesEnabled(): Boolean = locationGateway.areLocationServicesEnabled()

    override fun changeCurrentScreen(screen: IScreen)  {
        val currentScreen = _currentScreen.value

        // stop hardware services when leaving specific pages
        if (currentScreen != screen && currentScreen == IScreen.StoreNearbyDevices) {
            /* stop Bluetooth scanning when leaving StoreNearbyDevices screen */
            stopBluetoothScan()
        }

        _currentScreen.update { screen }
    }

    override fun getLocationsForDevice(deviceId: Long?): Flow<Map<Long, LocationEntity>> {
        return if (deviceId != null) repository.getDeviceLocations(deviceId)
            .map { locations-> locations.associateBy { location -> location.locationId } } else
            flowOf(emptyMap())
    }

    override fun navigateBack() {
        when (_currentScreen.value) {
            IScreen.StoreNearbyDevices -> {
                stopBluetoothScan()
                _currentScreen.value = IScreen.Menu
            }
            IScreen.ShowTrackedDevices -> {
                _currentScreen.value = IScreen.Menu
            }
            is IScreen.SingleDeviceLocations, is IScreen.FindDevice -> {
                if (_currentScreen.value is IScreen.FindDevice) {
                    locationGateway.stopLocationUpdates()
                }

                _currentScreen.value = IScreen.ShowTrackedDevices
            }
            else -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()

        // stop Bluetooth scanning and location updates
        bluetoothGateway.stopScan()
        locationGateway.stopLocationUpdates()
    }

    override fun removeAllDevices() {
        viewModelScope.launch {
            repository.removeAllDevices()
        }
    }

    override fun removeDevice(deviceId: Long) {
        viewModelScope.launch {
            repository.removeDevice(deviceId)
        }
    }

    override fun removeDeviceLocation(deviceId: Long, locationId: Long) {
        viewModelScope.launch {
            repository.removeDeviceLocation(deviceId, locationId)
        }
    }

    override fun removeDeviceLocationsButLast(deviceId: Long) {
        viewModelScope.launch {
            repository.removeDeviceLocationsButLast(deviceId)
        }
    }

    override fun saveDeviceWithLocation(
        deviceData: DeviceData,
        onSuccess: () -> Unit,
        onUnavailableLocation: () -> Unit
    ) {
        viewModelScope.launch {
            val deviceLocation: LocationData? = locationGateway.getCurrentLocation()
            if (deviceLocation != null) {
                Log.i("LocatorViewModel",
                    "Stored location: latitude: ${deviceLocation.latitude}, " +
                            "longitude: ${deviceLocation.longitude}, " +
                            "accuracy: ${deviceLocation.accuracy}")
                repository.insertDeviceWithLocation(
                    device = deviceData,
                    location = deviceLocation
                )
                onSuccess()
            } else {
                onUnavailableLocation()
            }
        }
    }

    override fun startBluetoothScan() {
        bluetoothGateway.startScan()
    }

    override fun stopBluetoothScan() {
        bluetoothGateway.stopScan()
    }

    /**
     * Compares two devices based on their RSSI and name.
     * @param first First device to compare.
     * @param second Second device to compare.
     */
    private fun nearbyDevicesComparator(first: DeviceData, second: DeviceData): Int {
        return if (first.rssi != second.rssi)
            second.rssi.compareTo(first.rssi) else first.name.compareTo(second.name)
    }
}
