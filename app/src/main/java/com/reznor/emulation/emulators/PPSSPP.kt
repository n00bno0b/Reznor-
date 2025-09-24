package com.reznor.emulation.emulators

import com.reznor.emulation.model.EmulatorComponent
import com.reznor.emulation.model.EmulatorStatus

val PPSSPP = EmulatorComponent(
    id = "ppsspp",
    name = "PPSSPP",
    description = "Gold standard PSP emulator",
    purpose = "PSP",
    estimatedSizeRange = "20-40 MB",
    notes = "Gold standard",
    supportedFormats = listOf("ISO", "CSO", "PBP"),
    features = listOf("HD graphics", "Save states", "Multiplayer"),
    downloadUrl = "https://ppsspp.org/files/1_16_6/ppsspp.apk",
    packageName = "org.ppsspp.ppsspp"
)