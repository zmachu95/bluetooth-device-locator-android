package cz.vsb.fei.bluetoothdevicelocator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cz.vsb.fei.bluetoothdevicelocator.data.LocatorRepository
import cz.vsb.fei.bluetoothdevicelocator.hardware.BluetoothGateway
import cz.vsb.fei.bluetoothdevicelocator.hardware.CompassGateway
import cz.vsb.fei.bluetoothdevicelocator.hardware.LocationGateway

/**
 * Factory for LocatorViewModel.
 */
class LocatorViewModelFactory(
    private val repository: LocatorRepository,
    private val bluetoothGateway: BluetoothGateway,
    private val locationGateway: LocationGateway,
    private val compassGateway: CompassGateway
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocatorViewModelImpl(repository, bluetoothGateway, locationGateway, compassGateway) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}