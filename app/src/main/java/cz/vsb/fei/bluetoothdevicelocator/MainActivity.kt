package cz.vsb.fei.bluetoothdevicelocator

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import cz.vsb.fei.bluetoothdevicelocator.data.DeviceEntity
import cz.vsb.fei.bluetoothdevicelocator.data.LocationEntity
import cz.vsb.fei.bluetoothdevicelocator.ui.theme.BluetoothDeviceLocatorTheme

import cz.vsb.fei.bluetoothdevicelocator.data.LocatorRepository
import cz.vsb.fei.bluetoothdevicelocator.hardware.BluetoothGateway
import cz.vsb.fei.bluetoothdevicelocator.hardware.CompassGateway
import cz.vsb.fei.bluetoothdevicelocator.hardware.LocationGateway
import cz.vsb.fei.bluetoothdevicelocator.ui.IScreen
import cz.vsb.fei.bluetoothdevicelocator.ui.MainAppScaffold
import cz.vsb.fei.bluetoothdevicelocator.viewmodel.LocatorViewModel
import cz.vsb.fei.bluetoothdevicelocator.viewmodel.LocatorViewModelFactory

class MainActivity : ComponentActivity() {

    /**
     * Cached array of permissions needed for the Bluetooth Locator application.
     */
    private val applicationPermissions =
        (BluetoothGateway.getBluetoothPermissions() + LocationGateway.LOCATION_PERMISSIONS)
            .plus(Manifest.permission.INTERNET)

    /**
     * Launcher for requesting necessary permissions to perform single Bluetooth scan for nearby devices.
     */
    private val bluetoothSingleScanPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val context = applicationContext
        val hasPermissions = hasNecessaryPermissions(permissionsMap)

        if (hasPermissions && hasHardwareReady()) {
            viewModel.startBluetoothScan(false)
        } else if (!hasPermissions) {
            Toast.makeText(
                context,
                context.resources.getString(R.string.no_bluetooth_search_without_permissions),
                Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Launcher for requesting necessary permissions to perform finding a specific device
     */
    private val findDevicePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val context = applicationContext
        val hasPermissions = hasNecessaryPermissions(permissionsMap)

        if (hasPermissions) {
            viewModel.startBluetoothScan(true)
            viewModel.startCompassUpdates()
            viewModel.startLocationUpdates()
            viewModel.changeCurrentScreen(viewModel.cachedFindDeviceScreen)

            hasHardwareReady()
        } else {
            Toast.makeText(
                context,
                context.resources.getString(R.string.no_bluetooth_search_without_permissions),
                Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Locator view model.
     */
    private val viewModel: LocatorViewModel by viewModels {
        LocatorViewModelFactory(
            repository = LocatorRepository(applicationContext),
            bluetoothGateway = BluetoothGateway(applicationContext),
            locationGateway = LocationGateway(applicationContext),
            compassGateway = CompassGateway(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BluetoothDeviceLocatorTheme {
                MainAppScaffold(
                    viewModel,
                    onStartBluetoothScan = this::startSingleBluetoothScan,
                    onDeviceStoredSuccessfully = this::showDeviceSavedToast,
                    onLocationUnavailable = this::showLocationUnavailableToast,
                    initializeFindDeviceScreen = this::startFindingDevice
                )
            }
        }
    }

    /**
     * Checks whether Bluetooth and location services hardware is ready for use.
     * @return Whether Bluetooth and location services hardware is ready for use.
     */
    private fun hasHardwareReady(): Boolean {
        if (viewModel.isBluetoothEnabled() && viewModel.areLocationServicesEnabled()) {
            return true
        }

        val context = applicationContext
        val toastText: String = when (true) {
            (!viewModel.isBluetoothEnabled() && !viewModel.areLocationServicesEnabled()) ->
                context.getString(R.string.enable_bluetooth_and_location_services_prompt)
            !viewModel.isBluetoothEnabled() ->
                context.resources.getString(R.string.enable_bluetooth_prompt)
            !viewModel.areLocationServicesEnabled() ->
                context.resources.getString(R.string.enable_location_services_prompt)
            else ->
                ""
        }
        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
        return false
    }

    /**
     * Checks whether all necessary permissions are granted.
     * @param permissionsMap Map of permissions and their granted status.
     * @return Whether all necessary permissions are granted.
     */
    private fun hasNecessaryPermissions(permissionsMap: Map<String, Boolean>): Boolean =
        BluetoothGateway.getBluetoothPermissions().map { permissionsMap[it] ?: false }.all{it}
                && LocationGateway.LOCATION_PERMISSIONS.map { permissionsMap[it] ?: false }.any{it}

    /**
     * Checks whether all necessary permissions are granted.
     * @return Whether all necessary permissions are granted.
     */
    private fun hasNecessaryPermissions(): Boolean =
        hasNecessaryPermissions(applicationPermissions.associateWith {
            (applicationContext.checkSelfPermission(it)
                    == PackageManager.PERMISSION_GRANTED)
        })

    /**
     * Shows toast when device is successfully saved.
     */
    private fun showDeviceSavedToast() = Toast.makeText(
        applicationContext,
        applicationContext.getString(R.string.device_saved),
        Toast.LENGTH_SHORT).show()

    /**
     * Shows toast when device's location is unavailable.
     */
    private fun showLocationUnavailableToast() = Toast.makeText(
        applicationContext,
        applicationContext.getString(R.string.device_location_unavailable),
        Toast.LENGTH_SHORT).show()

    /**
     * Starts single Bluetooth scan with permissions check.
     */
    private fun startSingleBluetoothScan() {
        val hasPermissions = hasNecessaryPermissions()
        if (hasPermissions && hasHardwareReady()) {
            viewModel.startBluetoothScan(false)
        } else if (!hasPermissions) {
            bluetoothSingleScanPermissionLauncher.launch(applicationPermissions)
        }
    }

    /**
     * Starts finding a specific device with permissions check.
     * @param device Device to find.
     * @param location Location of the device.
     * @param deviceLocationRssi Value of RSSI for the device at the location.
     */
    private fun startFindingDevice(device: DeviceEntity?,
                                   location: LocationEntity?,
                                   deviceLocationRssi: Float?) {
        viewModel.cachedFindDeviceScreen = IScreen.FindDevice(device, location, deviceLocationRssi)
        val hasPermissions = hasNecessaryPermissions()
        if (hasPermissions) {
            viewModel.startBluetoothScan(true)
            viewModel.startCompassUpdates()
            viewModel.startLocationUpdates()
            viewModel.changeCurrentScreen(viewModel.cachedFindDeviceScreen)

            hasHardwareReady()
        } else {
            findDevicePermissionLauncher.launch(applicationPermissions)
        }

    }

}
