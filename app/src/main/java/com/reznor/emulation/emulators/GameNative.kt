package com.reznor.emulation.emulators

import com.reznor.emulation.model.EmulatorComponent
import com.reznor.emulation.model.EmulatorStatus

val GameNative = EmulatorComponent(
    id = "gamenative",
    name = "GameNative (+ Wine/Box64 bits)",
    description = "PC and Steam titles with Wine/Box64",
    purpose = "PC/Steam titles",
    estimatedSizeRange = "250-450 MB",
    notes = "Wine/Box64 bits",
    supportedFormats = listOf("EXE", "MSI"),
    features = listOf("Wine compatibility", "Steam integration", "x86 translation")
    // downloadUrl and packageName not available yet
)