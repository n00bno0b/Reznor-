package com.reznor.emulation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reznor.emulation.model.EmulatorComponent
import com.reznor.emulation.model.EmulatorComponents
import com.reznor.emulation.model.EmulatorStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmulationManagerViewModel : ViewModel() {
    
    private val _components = MutableStateFlow(EmulatorComponents.ALL_COMPONENTS)
    val components: StateFlow<List<EmulatorComponent>> = _components.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun installComponent(componentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            updateComponentStatus(componentId, EmulatorStatus.DOWNLOADING)
            
            // Simulate download/installation process
            delay(2000) // Simulated installation time
            
            updateComponentStatus(componentId, EmulatorStatus.INSTALLED)
            _isLoading.value = false
        }
    }
    
    fun uninstallComponent(componentId: String) {
        updateComponentStatus(componentId, EmulatorStatus.NOT_INSTALLED)
    }
    
    fun launchComponent(componentId: String) {
        // In a real implementation, this would launch the specific emulator
        // For now, we'll just simulate the action
        viewModelScope.launch {
            // Simulate launching
            delay(500)
            // Could open specific emulator interface or file picker
        }
    }
    
    private fun updateComponentStatus(componentId: String, status: EmulatorStatus) {
        val currentComponents = _components.value
        val updatedComponents = currentComponents.map { component ->
            if (component.id == componentId) {
                component.copy(status = status)
            } else {
                component
            }
        }
        _components.value = updatedComponents
    }
    
    fun getInstalledComponents(): List<EmulatorComponent> {
        return _components.value.filter { it.status == EmulatorStatus.INSTALLED }
    }
    
    fun getAvailableComponents(): List<EmulatorComponent> {
        return _components.value.filter { it.status == EmulatorStatus.NOT_INSTALLED }
    }
}