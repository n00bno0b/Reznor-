package com.reznor.emulation.libretro

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reznor.emulation.viewmodel.EmulationManagerViewModel
import kotlinx.coroutines.launch
import java.io.File

fun getCoreInfoForSystem(system: String, coreManager: CoreManager): CoreManager.CoreInfo? {
    return when (system.lowercase()) {
        "nes" -> coreManager.availableCores.find { it.system == "NES" }
        "snes", "sfc" -> coreManager.availableCores.find { it.system == "SNES" }
        "sega genesis", "mega drive" -> coreManager.availableCores.find { it.system == "Genesis" }
        else -> null
    }
}

class EmulationActivity : ComponentActivity() {

    private lateinit var coreManager: CoreManager
    private lateinit var romManager: RomManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        coreManager = CoreManager(this)
        romManager = RomManager(this)

        // Get ROM information from intent
        val romPath = intent.getStringExtra("ROM_PATH")
        val romSystem = intent.getStringExtra("ROM_SYSTEM")

        setContent {
            EmulationScreen(romPath, romSystem)
        }
    }

    internal fun launchRetroArch(romPath: String, corePath: String) {
        try {
            val retroIntent = Intent(this, com.retroarch.browser.retroactivity.RetroActivityFuture::class.java)

            // Set up RetroArch intent parameters
            retroIntent.putExtra("ROM", romPath)
            retroIntent.putExtra("LIBRETRO", corePath)
            retroIntent.putExtra("CONFIGFILE", com.retroarch.browser.preferences.util.UserPreferences.getDefaultConfigPath(this))
            retroIntent.putExtra("IME", Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD))
            retroIntent.putExtra("DATADIR", applicationInfo.dataDir)
            retroIntent.putExtra("APK", applicationInfo.sourceDir)
            retroIntent.putExtra("SDCARD", Environment.getExternalStorageDirectory().absolutePath)
            val external = Environment.getExternalStorageDirectory().absolutePath + "/Android/data/" + packageName + "/files"
            retroIntent.putExtra("EXTERNAL", external)

            // Launch RetroArch activity directly
            startActivity(retroIntent)
            finish() // Close our activity since RetroArch takes over

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to launch RetroArch: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getCoreInfoForSystem(system: String): CoreManager.CoreInfo? {
        return when (system.lowercase()) {
            "nes" -> coreManager.availableCores.find { it.system == "NES" }
            "snes", "sfc" -> coreManager.availableCores.find { it.system == "SNES" }
            "sega genesis", "mega drive" -> coreManager.availableCores.find { it.system == "Genesis" }
            else -> null
        }
    }

    internal fun copyRomToTempFile(uri: Uri): String? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val tempFile = File.createTempFile("rom_", ".tmp", cacheDir)
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                tempFile.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmulationScreen(
    initialRomPath: String? = null,
    initialRomSystem: String? = null,
    viewModel: EmulationManagerViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedCore by remember { mutableStateOf<CoreManager.CoreInfo?>(null) }
    var selectedRom by remember { mutableStateOf<RomInfo?>(null) }
    var isDownloading by remember { mutableStateOf(false) }

    val coreManager = remember { CoreManager(context) }
    val romManager = remember { RomManager(context) }

    // ROM Picker
    val romPicker = rememberLauncherForActivityResult(RomPickerContract()) { uri ->
        uri?.let {
            romManager.getRomInfo(it)?.let { romInfo ->
                selectedRom = romInfo
                // Auto-select recommended core for this ROM
                romManager.getRecommendedCore(romInfo.coreType)?.let { recommendedCore ->
                    selectedCore = recommendedCore
                }
            }
        }
    }

    // Start emulation if ROM was provided
    LaunchedEffect(initialRomPath, initialRomSystem) {
        if (initialRomPath != null && initialRomSystem != null) {
            try {
                // Find appropriate core for the system
                val coreInfo = getCoreInfoForSystem(initialRomSystem, coreManager)
                if (coreInfo == null) {
                    android.widget.Toast.makeText(context, "No core available for $initialRomSystem games", android.widget.Toast.LENGTH_LONG).show()
                    return@LaunchedEffect
                }

                // Check if core is downloaded
                if (!coreManager.isCoreDownloaded(coreInfo)) {
                    android.widget.Toast.makeText(context, "${coreInfo.name} core not downloaded. Please download it first.", android.widget.Toast.LENGTH_LONG).show()
                    return@LaunchedEffect
                }

                // Get core file path
                val coreFile = coreManager.getCoreFile(coreInfo)

                // Copy ROM to temp file and launch RetroArch
                val romUri = Uri.parse(initialRomPath)
                val romTempPath = (context as EmulationActivity).copyRomToTempFile(romUri)
                if (romTempPath != null) {
                    (context as EmulationActivity).launchRetroArch(romTempPath, coreFile.absolutePath)
                } else {
                    android.widget.Toast.makeText(context, "Failed to prepare ROM file", android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Failed to start emulation: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reznor Console") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Core and ROM Selection
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Select Game", style = MaterialTheme.typography.headlineSmall)

                // ROM Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { romPicker.launch("*/*") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (selectedRom != null) Icons.Default.CheckCircle else Icons.Default.Add,
                            contentDescription = null,
                            tint = if (selectedRom != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedRom?.displayName ?: "Select a ROM file",
                                style = MaterialTheme.typography.titleMedium
                            )
                            selectedRom?.let {
                                Text(
                                    "${it.coreType.displayName} • ${(it.size / 1024).toInt()} KB",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                selectedRom?.let { rom ->
                    Text("Recommended Core", style = MaterialTheme.typography.headlineSmall)

                    romManager.getRecommendedCore(rom.coreType)?.let { recommendedCore ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { selectedCore = recommendedCore }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCore == recommendedCore,
                                    onClick = { selectedCore = recommendedCore }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(recommendedCore.name, style = MaterialTheme.typography.titleMedium)
                                    Text("${recommendedCore.system} Emulator", style = MaterialTheme.typography.bodyMedium)
                                    if (coreManager.isCoreDownloaded(recommendedCore)) {
                                        Text("Ready to play", color = MaterialTheme.colorScheme.primary)
                                    } else {
                                        Text("Download required", color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Button
                selectedCore?.let { core ->
                    if (!coreManager.isCoreDownloaded(core)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isDownloading = true
                                    coreManager.downloadCore(core)
                                    isDownloading = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isDownloading
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (isDownloading) "Downloading..." else "Download Core")
                        }
                    } else {
                        Button(
                            onClick = {
                                // Copy ROM to internal storage and launch RetroArch
                                selectedRom?.uri?.let { uri ->
                                    val romTempPath = (context as EmulationActivity).copyRomToTempFile(uri)
                                    if (romTempPath != null) {
                                        (context as EmulationActivity).launchRetroArch(romTempPath, coreManager.getCoreFile(core).absolutePath)
                                    } else {
                                        android.widget.Toast.makeText(context, "Failed to prepare ROM file", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = selectedRom != null
                        ) {
                            Text("Start Game")
                        }
                    }
                }
            }
        }
    }
}