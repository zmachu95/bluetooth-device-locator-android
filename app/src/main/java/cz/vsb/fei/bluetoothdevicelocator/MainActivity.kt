package cz.vsb.fei.bluetoothdevicelocator

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import cz.vsb.fei.bluetoothdevicelocator.ui.theme.BluetoothDeviceLocatorTheme

import cz.vsb.fei.bluetoothdevicelocator.data.LocatorRepository
import cz.vsb.fei.bluetoothdevicelocator.hardware.BluetoothGateway
import cz.vsb.fei.bluetoothdevicelocator.hardware.LocationGateway
import cz.vsb.fei.bluetoothdevicelocator.ui.MainAppScaffold
import cz.vsb.fei.bluetoothdevicelocator.viewmodel.LocatorViewModel
import cz.vsb.fei.bluetoothdevicelocator.viewmodel.LocatorViewModelFactory

class MainActivity : ComponentActivity() {

    /**
     * Cached array of permissions needed for the Bluetooth Locator application.
     */
    private val applicationPermissions =
        BluetoothGateway.getBluetoothPermissions() + LocationGateway.LOCATION_PERMISSIONS

    /**
     * Launcher for requesting necessary permissions.
     */
    private val blueToothScanPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val context = applicationContext
        val hasPermissions = hasNecessaryPermissions(permissionsMap)

        if (hasPermissions && hasHardwareReady()) {
            viewModel.startBluetoothScan()
        } else if (!hasPermissions) {
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
            locationGateway = LocationGateway(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BluetoothDeviceLocatorTheme {
                MainAppScaffold(
                    viewModel,
                    onStartBluetoothScan = this::startBluetoothScan,
                    onDeviceStoredSuccessfully = this::showDeviceSavedToast,
                    onLocationUnavailable = this::showLocationUnavailableToast
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
        hasNecessaryPermissions(applicationPermissions.map{
            it to (applicationContext.checkSelfPermission(it)
                    == PackageManager.PERMISSION_GRANTED)
        }.toMap())

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
     * Starts Bluetooth scanning with permissions check.
     */
    private fun startBluetoothScan() {
        val hasPermissions = hasNecessaryPermissions()
        if (hasPermissions && hasHardwareReady()) {
            viewModel.startBluetoothScan()
        } else if (!hasPermissions) {
            blueToothScanPermissionLauncher.launch(applicationPermissions)
        }
    }

}
