package cz.vsb.fei.bluetoothdevicelocator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cz.vsb.fei.bluetoothdevicelocator.data.LocatorDatabase
import cz.vsb.fei.bluetoothdevicelocator.data.LocatorRepository

/**
 * Factory for LocatorViewModel.
 */
class LocatorViewModelFactory(private val repository: LocatorRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocatorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}