package com.reznor.emulation.libretro

import android.content.Context
import android.net.Uri
import java.io.File

data class RomInfo(
    val uri: Uri,
    val displayName: String,
    val size: Long,
    val coreType: CoreType
)

enum class CoreType(val displayName: String, val extensions: List<String>) {
    NES("Nintendo Entertainment System", listOf("nes", "fds")),
    SNES("Super Nintendo", listOf("smc", "sfc", "fig")),
    GENESIS("Sega Genesis", listOf("md", "bin", "gen")),
    PS1("PlayStation 1", listOf("cue", "bin", "iso", "img")),
    UNKNOWN("Unknown", emptyList())
}

class RomManager(private val context: Context) {

    fun getRomInfo(uri: Uri): RomInfo? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayName = it.getString(it.getColumnIndexOrThrow("_display_name")) ?: "Unknown"
                    val size = it.getLong(it.getColumnIndexOrThrow("_size"))
                    val coreType = determineCoreType(displayName)
                    RomInfo(uri, displayName, size, coreType)
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun determineCoreType(filename: String): CoreType {
        val extension = filename.substringAfterLast('.', "").lowercase()

        return when (extension) {
            in CoreType.NES.extensions -> CoreType.NES
            in CoreType.SNES.extensions -> CoreType.SNES
            in CoreType.GENESIS.extensions -> CoreType.GENESIS
            in CoreType.PS1.extensions -> CoreType.PS1
            else -> CoreType.UNKNOWN
        }
    }

    fun getRecommendedCore(coreType: CoreType): CoreManager.CoreInfo? {
        return when (coreType) {
            CoreType.NES -> CoreManager.CoreInfo(
                name = "Nestopia",
                system = "NES",
                downloadUrl = "https://buildbot.libretro.com/nightly/android/latest/arm64-v8a/nestopia_libretro_android.so.zip",
                filename = "nestopia_libretro_android.so"
            )
            CoreType.SNES -> CoreManager.CoreInfo(
                name = "Snes9x",
                system = "SNES",
                downloadUrl = "https://buildbot.libretro.com/nightly/android/latest/arm64-v8a/snes9x_libretro_android.so.zip",
                filename = "snes9x_libretro_android.so"
            )
            CoreType.GENESIS -> CoreManager.CoreInfo(
                name = "Genesis Plus GX",
                system = "Genesis",
                downloadUrl = "https://buildbot.libretro.com/nightly/android/latest/arm64-v8a/genesis_plus_gx_libretro_android.so.zip",
                filename = "genesis_plus_gx_libretro_android.so"
            )
            else -> null
        }
    }

    fun copyRomToInternal(uri: Uri): String? {
        return try {
            val romsDir = File(context.filesDir, "roms").apply { mkdirs() }
            val romInfo = getRomInfo(uri) ?: return null

            val destFile = File(romsDir, romInfo.displayName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}