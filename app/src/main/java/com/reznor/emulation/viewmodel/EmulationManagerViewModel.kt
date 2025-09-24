package com.reznor.emulation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.reznor.emulation.model.EmulatorComponent
import com.reznor.emulation.model.EmulatorComponents
import com.reznor.emulation.model.EmulatorStatus
import com.reznor.emulation.model.RomManager
import com.reznor.emulation.model.RomMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class EmulationManagerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val romManager = RomManager(application)
    
    private val _components = MutableStateFlow(EmulatorComponents.ALL_COMPONENTS.map { checkInstalled(it) })
    val components: StateFlow<List<EmulatorComponent>> = _components.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // ROM Library
    val romLibrary = romManager.getRomLibrary()
    val favoriteRoms = romManager.getFavoriteRoms()
    val recentlyPlayedRoms = romManager.getRecentlyPlayedRoms()
    
    private fun checkInstalled(component: EmulatorComponent): EmulatorComponent {
        val packageName = component.packageName ?: return component
        return try {
            getApplication<Application>().packageManager.getPackageInfo(packageName, 0)
            component.copy(status = EmulatorStatus.INSTALLED)
        } catch (e: Exception) {
            component
        }
    }
    
    fun installComponent(componentId: String) {
        val component = _components.value.find { it.id == componentId } ?: return
        val downloadUrl = component.downloadUrl ?: return // No URL, skip

        viewModelScope.launch {
            _isLoading.value = true
            updateComponentStatus(componentId, EmulatorStatus.DOWNLOADING)

            try {
                val apkFile = downloadApk(downloadUrl, componentId)
                if (apkFile != null) {
                    // Install the APK
                    installApk(apkFile)
                    updateComponentStatus(componentId, EmulatorStatus.INSTALLED)
                } else {
                    updateComponentStatus(componentId, EmulatorStatus.ERROR)
                }
            } catch (e: Exception) {
                updateComponentStatus(componentId, EmulatorStatus.ERROR)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun downloadApk(url: String, componentId: String): File? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext null
            }

            val inputStream = connection.inputStream
            val cacheDir = getApplication<Application>().cacheDir
            val apkFile = File(cacheDir, "$componentId.apk")

            FileOutputStream(apkFile).use { output ->
                inputStream.copyTo(output)
            }

            inputStream.close()
            connection.disconnect()

            apkFile
        } catch (e: Exception) {
            null
        }
    }

    private fun installApk(apkFile: File) {
        // Use FileProvider for URI
        val uri = androidx.core.content.FileProvider.getUriForFile(
            getApplication(),
            "${getApplication<Application>().packageName}.fileprovider",
            apkFile
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        getApplication<Application>().startActivity(intent)
    }
    
    fun uninstallComponent(componentId: String) {
        updateComponentStatus(componentId, EmulatorStatus.NOT_INSTALLED)
    }
    
    fun launchComponent(componentId: String) {
        val component = _components.value.find { it.id == componentId } ?: return
        val packageName = component.packageName ?: return

        try {
            val intent = getApplication<Application>().packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                getApplication<Application>().startActivity(intent)
            }
        } catch (e: Exception) {
            // Handle error
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
    
    // ROM Management Methods
    fun addRom(uri: android.net.Uri) {
        viewModelScope.launch {
            romManager.addRom(uri)
        }
    }
    
    fun removeRom(filePath: String) {
        viewModelScope.launch {
            romManager.removeRom(filePath)
        }
    }
    
    fun markRomAsPlayed(filePath: String) {
        viewModelScope.launch {
            romManager.markAsPlayed(filePath)
        }
    }
    
    fun toggleRomFavorite(filePath: String) {
        viewModelScope.launch {
            romManager.toggleFavorite(filePath)
        }
    }
    
    fun addRomPlayTime(filePath: String, additionalTime: Long) {
        viewModelScope.launch {
            romManager.addPlayTime(filePath, additionalTime)
        }
    }
    
    fun getRomsBySystem(system: String) = romManager.getRomsBySystem(system)
    
    suspend fun getRomCount(): Int = romManager.getRomCount()
    
    suspend fun getTotalPlayTime(): Long = romManager.getTotalPlayTime()
}