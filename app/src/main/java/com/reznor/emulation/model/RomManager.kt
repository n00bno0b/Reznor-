package com.reznor.emulation.model

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rom_library")

@Serializable
data class RomMetadata(
    val fileName: String,
    val filePath: String,
    val system: String,
    val title: String,
    val size: Long,
    val lastPlayed: Long? = null,
    val playTime: Long = 0L,
    val favorite: Boolean = false
)

class RomManager(private val context: Context) {

    private val json = Json { prettyPrint = true }

    companion object {
        private val ROM_LIBRARY_KEY = stringPreferencesKey("rom_library")
    }

    // System detection based on file extensions
    private val systemExtensions = mapOf(
        "nes" to "NES",
        "snes" to "SNES",
        "sfc" to "SNES",
        "n64" to "Nintendo 64",
        "z64" to "Nintendo 64",
        "v64" to "Nintendo 64",
        "gb" to "Game Boy",
        "gbc" to "Game Boy Color",
        "gba" to "Game Boy Advance",
        "nds" to "Nintendo DS",
        "3ds" to "Nintendo 3DS",
        "cia" to "Nintendo 3DS",
        "gen" to "Sega Genesis",
        "md" to "Sega Genesis",
        "sms" to "Sega Master System",
        "gg" to "Sega Game Gear",
        "pce" to "PC Engine",
        "tg16" to "TurboGrafx-16",
        "psx" to "PlayStation",
        "ps1" to "PlayStation",
        "bin" to "PlayStation",
        "cue" to "PlayStation",
        "iso" to "PlayStation",
        "wii" to "Nintendo Wii",
        "wbfs" to "Nintendo Wii",
        "rvz" to "Nintendo Wii",
        "gcm" to "Nintendo GameCube",
        "iso" to "Nintendo GameCube",
        "wad" to "Nintendo Wii",
        "dol" to "Nintendo Wii/GameCube"
    )

    fun getRomLibrary(): Flow<List<RomMetadata>> {
        return context.dataStore.data.map { preferences ->
            val romLibraryJson = preferences[ROM_LIBRARY_KEY] ?: "[]"
            try {
                json.decodeFromString<List<RomMetadata>>(romLibraryJson)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun addRom(uri: Uri): Boolean {
        return try {
            val fileName = getFileNameFromUri(uri) ?: return false
            val filePath = uri.toString()
            val system = detectSystem(fileName) ?: return false
            val size = getFileSize(uri) ?: 0L
            val title = extractTitleFromFileName(fileName)

            val newRom = RomMetadata(
                fileName = fileName,
                filePath = filePath,
                system = system,
                title = title,
                size = size
            )

            context.dataStore.edit { preferences ->
                val romLibraryJson = preferences[ROM_LIBRARY_KEY] ?: "[]"
                val currentLibrary = try {
                    json.decodeFromString<List<RomMetadata>>(romLibraryJson)
                } catch (e: Exception) {
                    emptyList()
                }.toMutableList()

                // Check if ROM already exists
                if (currentLibrary.any { it.filePath == filePath }) {
                    return@edit // Already exists
                }

                currentLibrary.add(newRom)
                val updatedJson = json.encodeToString(currentLibrary)
                preferences[ROM_LIBRARY_KEY] = updatedJson
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removeRom(filePath: String): Boolean {
        return try {
            context.dataStore.edit { preferences ->
                val romLibraryJson = preferences[ROM_LIBRARY_KEY] ?: "[]"
                val currentLibrary = try {
                    json.decodeFromString<List<RomMetadata>>(romLibraryJson)
                } catch (e: Exception) {
                    emptyList()
                }.toMutableList()

                val updatedLibrary = currentLibrary.filter { it.filePath != filePath }

                if (updatedLibrary.size == currentLibrary.size) {
                    return@edit // ROM not found
                }

                val updatedJson = json.encodeToString(updatedLibrary)
                preferences[ROM_LIBRARY_KEY] = updatedJson
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateRomMetadata(filePath: String, updates: RomMetadata.() -> RomMetadata): Boolean {
        return try {
            context.dataStore.edit { preferences ->
                val romLibraryJson = preferences[ROM_LIBRARY_KEY] ?: "[]"
                val currentLibrary = try {
                    json.decodeFromString<List<RomMetadata>>(romLibraryJson)
                } catch (e: Exception) {
                    emptyList()
                }.toMutableList()

                val index = currentLibrary.indexOfFirst { it.filePath == filePath }

                if (index == -1) return@edit

                currentLibrary[index] = updates(currentLibrary[index])

                val updatedJson = json.encodeToString(currentLibrary)
                preferences[ROM_LIBRARY_KEY] = updatedJson
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun markAsPlayed(filePath: String) {
        updateRomMetadata(filePath) { copy(lastPlayed = System.currentTimeMillis()) }
    }

    suspend fun toggleFavorite(filePath: String) {
        updateRomMetadata(filePath) { copy(favorite = !favorite) }
    }

    suspend fun addPlayTime(filePath: String, additionalTime: Long) {
        updateRomMetadata(filePath) { copy(playTime = playTime + additionalTime) }
    }

    private fun detectSystem(fileName: String): String? {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return systemExtensions[extension]
    }

    private fun extractTitleFromFileName(fileName: String): String {
        // Remove extension and common tags
        return fileName
            .substringBeforeLast('.')
            .replace(Regex("\\s*\\([^)]*\\)\\s*"), "") // Remove parentheses
            .replace(Regex("\\s*\\[[^]]*\\]\\s*"), "") // Remove brackets
            .replace(Regex("\\s*\\{[^}]*\\}\\s*"), "") // Remove braces
            .replace(Regex("\\s*-\\s*"), " ") // Replace dashes with spaces
            .trim()
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex("_display_name")
                    if (nameIndex != -1) {
                        it.getString(nameIndex)
                    } else null
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getFileSize(uri: Uri): Long? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndex("_size")
                    if (sizeIndex != -1) {
                        it.getLong(sizeIndex)
                    } else null
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getRomsBySystem(system: String): Flow<List<RomMetadata>> {
        return getRomLibrary().map { library ->
            library.filter { it.system == system }
        }
    }

    fun getFavoriteRoms(): Flow<List<RomMetadata>> {
        return getRomLibrary().map { library ->
            library.filter { it.favorite }
        }
    }

    fun getRecentlyPlayedRoms(): Flow<List<RomMetadata>> {
        return getRomLibrary().map { library ->
            library
                .filter { it.lastPlayed != null }
                .sortedByDescending { it.lastPlayed }
        }
    }

    suspend fun clearLibrary() {
        context.dataStore.edit { preferences: androidx.datastore.preferences.core.MutablePreferences ->
            preferences.remove(ROM_LIBRARY_KEY)
        }
    }

    suspend fun getRomCount(): Int {
        val preferences = context.dataStore.data.first()
        val romLibraryJson = preferences[ROM_LIBRARY_KEY] ?: "[]"
        return try {
            json.decodeFromString<List<RomMetadata>>(romLibraryJson).size
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getTotalPlayTime(): Long {
        val preferences = context.dataStore.data.first()
        val romLibraryJson = preferences[ROM_LIBRARY_KEY] ?: "[]"
        return try {
            json.decodeFromString<List<RomMetadata>>(romLibraryJson).sumOf { it.playTime }
        } catch (e: Exception) {
            0L
        }
    }
}