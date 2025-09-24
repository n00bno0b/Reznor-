package com.reznor.emulation

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import java.io.File

// Data classes for ROM information
@Serializable
data class RomInfo(
    val uri: String,
    val displayName: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String? = null,
    val system: String? = null,
    val region: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastPlayed: Long? = null,
    val playTime: Long = 0, // in seconds
    val isFavorite: Boolean = false
)

// Extension property for DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rom_library")

class RomManager(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    // DataStore keys
    private val ROMS_KEY = stringPreferencesKey("roms_json")

    // Get all ROMs from storage
    val roms: Flow<List<RomInfo>> = context.dataStore.data
        .map { preferences ->
            val romsJson = preferences[ROMS_KEY] ?: "[]"
            try {
                json.decodeFromString<List<RomInfo>>(romsJson)
            } catch (e: Exception) {
                emptyList()
            }
        }

    // Add a ROM to the library
    suspend fun addRom(uri: Uri): RomInfo? {
        return try {
            val romInfo = extractRomInfo(uri) ?: return null

            // Check if ROM already exists
            val preferences = context.dataStore.data.first()
            val currentRoms = preferences[ROMS_KEY]?.let {
                json.decodeFromString<List<RomInfo>>(it)
            } ?: emptyList()

            if (currentRoms.any { it.uri == romInfo.uri }) {
                return null // Already exists
            }

            val updatedRoms = currentRoms + romInfo
            val romsJson = json.encodeToString(updatedRoms)

            context.dataStore.edit { preferences ->
                preferences[ROMS_KEY] = romsJson
            }

            romInfo
        } catch (e: Exception) {
            null
        }
    }

    // Remove a ROM from the library
    suspend fun removeRom(uri: String) {
        val preferences = context.dataStore.data.first()
        val currentRoms = preferences[ROMS_KEY]?.let {
            json.decodeFromString<List<RomInfo>>(it)
        } ?: emptyList()

        val updatedRoms = currentRoms.filter { it.uri != uri }
        val romsJson = json.encodeToString(updatedRoms)

        context.dataStore.edit { preferences ->
            preferences[ROMS_KEY] = romsJson
        }
    }

    // Update ROM metadata (favorite, last played, etc.)
    suspend fun updateRom(uri: String, updates: RomInfo.() -> RomInfo) {
        val preferences = context.dataStore.data.first()
        val currentRoms = preferences[ROMS_KEY]?.let {
            json.decodeFromString<List<RomInfo>>(it)
        } ?: emptyList()

        val updatedRoms = currentRoms.map { rom ->
            if (rom.uri == uri) updates(rom) else rom
        }
        val romsJson = json.encodeToString(updatedRoms)

        context.dataStore.edit { preferences ->
            preferences[ROMS_KEY] = romsJson
        }
    }

    // Extract ROM information from URI
    private fun extractRomInfo(uri: Uri): RomInfo? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) ?: "Unknown"
                    val size = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))

                    // Determine system based on file extension
                    val system = determineSystemFromFilename(displayName)

                    RomInfo(
                        uri = uri.toString(),
                        displayName = displayName,
                        fileName = displayName,
                        fileSize = size,
                        system = system,
                        region = determineRegionFromFilename(displayName)
                    )
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Determine game system from filename
    private fun determineSystemFromFilename(filename: String): String {
        val lowerFilename = filename.lowercase()

        return when {
            // Nintendo
            lowerFilename.endsWith(".nes") -> "Nintendo Entertainment System"
            lowerFilename.endsWith(".snes") || lowerFilename.endsWith(".smc") -> "Super Nintendo"
            lowerFilename.endsWith(".n64") || lowerFilename.endsWith(".z64") -> "Nintendo 64"
            lowerFilename.endsWith(".gb") -> "Game Boy"
            lowerFilename.endsWith(".gbc") -> "Game Boy Color"
            lowerFilename.endsWith(".gba") -> "Game Boy Advance"
            lowerFilename.endsWith(".nds") -> "Nintendo DS"
            lowerFilename.endsWith(".3ds") -> "Nintendo 3DS"
            lowerFilename.endsWith(".sfc") -> "Super Famicom"

            // Sega
            lowerFilename.endsWith(".gen") || lowerFilename.endsWith(".md") -> "Sega Genesis"
            lowerFilename.endsWith(".sms") -> "Sega Master System"
            lowerFilename.endsWith(".gg") -> "Sega Game Gear"
            lowerFilename.endsWith(".32x") -> "Sega 32X"
            lowerFilename.endsWith(".smd") -> "Sega Mega Drive"

            // Sony
            lowerFilename.endsWith(".psx") || lowerFilename.endsWith(".ps1") -> "PlayStation"
            lowerFilename.endsWith(".pbp") -> "PSP"
            lowerFilename.endsWith(".iso") -> "PlayStation 2"

            // Arcade
            lowerFilename.endsWith(".zip") -> "Arcade"

            // Other
            lowerFilename.endsWith(".rom") -> "ROM File"
            lowerFilename.endsWith(".bin") -> "Binary ROM"

            else -> "Unknown System"
        }
    }

    // Determine region from filename
    private fun determineRegionFromFilename(filename: String): String? {
        val lowerFilename = filename.lowercase()

        return when {
            lowerFilename.contains("(japan)") || lowerFilename.contains("(j)") -> "Japan"
            lowerFilename.contains("(usa)") || lowerFilename.contains("(u)") -> "USA"
            lowerFilename.contains("(europe)") || lowerFilename.contains("(e)") -> "Europe"
            lowerFilename.contains("(world)") || lowerFilename.contains("(w)") -> "World"
            lowerFilename.contains("(asia)") -> "Asia"
            lowerFilename.contains("(australia)") -> "Australia"
            else -> null
        }
    }

    // Get ROM file as byte array (for core loading)
    fun getRomBytes(uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            null
        }
    }

    // Get ROM file size
    fun getRomSize(uri: Uri): Long? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
}