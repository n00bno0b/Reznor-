package com.reznor.emulation.emulators

import com.reznor.emulation.model.EmulatorComponent
import com.reznor.emulation.model.EmulatorStatus

val Lemuroid = EmulatorComponent(
    id = "lemuroid",
    name = "Lemuroid (RetroArch)",
    description = "Multi-system emulator library",
    purpose = "Library, art, controller-first UX",
    estimatedSizeRange = "20-50 MB",
    notes = "Jetpack Compose, assets, glyphs",
    supportedFormats = listOf("NES", "SNES", "GB", "GBC", "GBA", "Genesis", "N64"),
    features = listOf("Multiple systems", "Save states", "Controller support"),
    downloadUrl = "https://github.com/Swordfish90/Lemuroid/releases/download/v1.15.0/lemuroid-1.15.0.apk",
    packageName = "com.swordfish.lemuroid"
)