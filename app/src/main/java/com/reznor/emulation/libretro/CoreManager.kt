package com.reznor.emulation.libretro

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class CoreManager(private val context: Context) {

    private val coresDir = File(context.filesDir, "cores")
    private val systemDir = File(context.filesDir, "system")

    init {
        coresDir.mkdirs()
        systemDir.mkdirs()
    }

    data class CoreInfo(
        val name: String,
        val system: String,
        val downloadUrl: String,
        val filename: String
    )

    // Predefined cores - using stable releases where possible, nightly as fallback
    val availableCores = listOf(
        CoreInfo(
            name = "Nestopia",
            system = "NES",
            downloadUrl = "https://buildbot.libretro.com/stable/android/latest/arm64-v8a/nestopia_libretro_android.so.zip",
            filename = "nestopia_libretro_android.so"
        ),
        CoreInfo(
            name = "Snes9x",
            system = "SNES",
            downloadUrl = "https://buildbot.libretro.com/stable/android/latest/arm64-v8a/snes9x_libretro_android.so.zip",
            filename = "snes9x_libretro_android.so"
        ),
        CoreInfo(
            name = "Genesis Plus GX",
            system = "Genesis",
            downloadUrl = "https://buildbot.libretro.com/stable/android/latest/arm64-v8a/genesis_plus_gx_libretro_android.so.zip",
            filename = "genesis_plus_gx_libretro_android.so"
        )
    )

    suspend fun downloadCore(coreInfo: CoreInfo): File? = withContext(Dispatchers.IO) {
        // Check if core is already bundled
        val bundledCore = getBundledCoreFile(coreInfo)
        if (bundledCore != null && bundledCore.exists()) {
            Log.i("CoreManager", "Core ${coreInfo.name} is already bundled")
            return@withContext bundledCore
        }

        // Check if already downloaded
        val downloadedCore = File(coresDir, coreInfo.filename)
        if (downloadedCore.exists()) {
            Log.i("CoreManager", "Core ${coreInfo.name} is already downloaded")
            return@withContext downloadedCore
        }

        // Proceed with download logic
        try {
            Log.i("CoreManager", "Downloading core: ${coreInfo.name}")

            val urlsToTry = listOf(
                coreInfo.downloadUrl,
                coreInfo.downloadUrl.replace("/stable/", "/nightly/")
            )

            for (urlString in urlsToTry) {
                try {
                    Log.i("CoreManager", "Trying URL: $urlString")
                    val url = URL(urlString)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 10000
                    connection.readTimeout = 30000
                    connection.connect()

                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        Log.w("CoreManager", "URL failed with response: ${connection.responseCode}")
                        connection.disconnect()
                        continue
                    }

                    // Download to temporary zip file
                    val tempZipFile = File.createTempFile("core_", ".zip", context.cacheDir)
                    FileOutputStream(tempZipFile).use { output ->
                        connection.inputStream.use { input ->
                            input.copyTo(output)
                        }
                    }
                    connection.disconnect()

                    // Extract the .so file from the zip
                    val coreFile = File(coresDir, coreInfo.filename)
                    if (extractCoreFromZip(tempZipFile, coreFile)) {
                        tempZipFile.delete() // Clean up temp file
                        Log.i("CoreManager", "Core extracted successfully: ${coreFile.absolutePath}")
                        return@withContext coreFile
                    } else {
                        tempZipFile.delete()
                        Log.w("CoreManager", "Failed to extract core from zip for URL: $urlString")
                    }
                } catch (e: Exception) {
                    Log.w("CoreManager", "Failed to download from $urlString", e)
                }
            }

            Log.e("CoreManager", "All download URLs failed for core: ${coreInfo.name}")
            null
        } catch (e: Exception) {
            Log.e("CoreManager", "Download error", e)
            null
        }
    }

    private fun extractCoreFromZip(zipFile: File, outputFile: File): Boolean {
        return try {
            ZipInputStream(zipFile.inputStream()).use { zipInput ->
                var entry = zipInput.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.endsWith(".so")) {
                        // Found the .so file, extract it
                        FileOutputStream(outputFile).use { output ->
                            zipInput.copyTo(output)
                        }
                        zipInput.closeEntry()
                        return true
                    }
                    zipInput.closeEntry()
                    entry = zipInput.nextEntry
                }
            }
            false // No .so file found in zip
        } catch (e: Exception) {
            Log.e("CoreManager", "Failed to extract core from zip", e)
            false
        }
    }

    fun getCoreFile(coreInfo: CoreInfo): File {
        // First check if core is bundled in assets
        val bundledCore = getBundledCoreFile(coreInfo)
        if (bundledCore != null && bundledCore.exists()) {
            return bundledCore
        }

        // Fall back to downloaded cores
        return File(coresDir, coreInfo.filename)
    }

    private fun getBundledCoreFile(coreInfo: CoreInfo): File? {
        return try {
            val assetManager = context.assets
            val bundledPath = "cores/${coreInfo.filename}"

            // Check if the core exists in assets
            assetManager.open(bundledPath).use { }
            val bundledFile = File(context.filesDir, "bundled_${coreInfo.filename}")

            if (!bundledFile.exists()) {
                Log.i("CoreManager", "Copying bundled core ${coreInfo.name} from assets to ${bundledFile.absolutePath}")
                // Copy from assets to internal storage
                assetManager.open(bundledPath).use { input ->
                    bundledFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                // Ensure the file has proper permissions
                bundledFile.setReadable(true, false)
                bundledFile.setExecutable(true, false)
                Log.i("CoreManager", "Successfully copied bundled core ${coreInfo.name}, size: ${bundledFile.length()} bytes")
            } else {
                Log.i("CoreManager", "Bundled core ${coreInfo.name} already exists at ${bundledFile.absolutePath}")
            }

            // Verify the file is accessible and has content
            if (bundledFile.exists() && bundledFile.canRead() && bundledFile.length() > 0) {
                Log.i("CoreManager", "Bundled core ${coreInfo.name} is valid and accessible")
                bundledFile
            } else {
                Log.e("CoreManager", "Bundled core ${coreInfo.name} is not valid: exists=${bundledFile.exists()}, readable=${bundledFile.canRead()}, size=${bundledFile.length()}")
                null
            }
        } catch (e: Exception) {
            Log.e("CoreManager", "Failed to access bundled core ${coreInfo.name}", e)
            null
        }
    }

    fun isCoreDownloaded(coreInfo: CoreInfo): Boolean {
        // Check if core is bundled first
        val bundledCore = getBundledCoreFile(coreInfo)
        if (bundledCore != null && bundledCore.exists()) {
            return true
        }

        // Check if core is downloaded
        return File(coresDir, coreInfo.filename).exists()
    }

    fun validateCore(coreInfo: CoreInfo): Boolean {
        val coreFile = getCoreFile(coreInfo)
        if (!coreFile.exists() || coreFile.length() < 1024 * 1024) { // At least 1MB
            Log.e("CoreManager", "Core validation failed: file doesn't exist or is too small. Path: ${coreFile.absolutePath}, exists: ${coreFile.exists()}, size: ${coreFile.length()}")
            return false
        }

        // Try to load the core briefly to validate it
        return try {
            Log.i("CoreManager", "Validating core: ${coreInfo.name} at ${coreFile.absolutePath}")
            val libretroCore = LibretroCore(context)
            val result = libretroCore.loadCore(coreFile.absolutePath)
            if (result) {
                libretroCore.unloadCore()
                Log.i("CoreManager", "Core validation successful for ${coreInfo.name}")
            } else {
                Log.e("CoreManager", "Core validation failed: loadCore returned false for ${coreInfo.name}")
            }
            result
        } catch (e: Exception) {
            Log.e("CoreManager", "Core validation failed for ${coreInfo.name}", e)
            false
        }
    }

    fun getDownloadedCores(): List<CoreInfo> {
        return availableCores.filter { isCoreDownloaded(it) }
    }

    fun getSystemDirectory(): File {
        return systemDir
    }

    fun isCoreBundled(coreInfo: CoreInfo): Boolean {
        return getBundledCoreFile(coreInfo) != null
    }
}