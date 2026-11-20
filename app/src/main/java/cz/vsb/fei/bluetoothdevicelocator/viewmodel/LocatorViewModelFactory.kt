package cz.vsb.fei.bluetoothdevicelocator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cz.vsb.fei.bluetoothdevicelocator.data.LocatorRepository
import cz.vsb.fei.bluetoothdevicelocator.hardware.BluetoothGateway

/**
 * Factory for LocatorViewModel.
 */
class LocatorViewModelFactory(private val repository: LocatorRepository,
                              private val bluetoothGateway: BluetoothGateway) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocatorViewModel(repository, bluetoothGateway) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}