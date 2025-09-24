package com.reznor.emulation.emulators

import com.reznor.emulation.model.EmulatorComponent
import com.reznor.emulation.model.EmulatorStatus

val Winlator = EmulatorComponent(
    id = "winlator",
    name = "Winlator CMOD",
    description = "Windows emulator for Android",
    purpose = "Windows games and apps",
    estimatedSizeRange = "100-200 MB",
    notes = "Wine-based Windows compatibility",
    supportedFormats = listOf("EXE", "MSI", "DLL"),
    features = listOf("DirectX support", "Windows API compatibility", "Customizable settings")
    // downloadUrl and packageName to be added
)