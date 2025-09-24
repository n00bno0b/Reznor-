package com.reznor.emulation.emulators

import com.reznor.emulation.model.EmulatorComponent
import com.reznor.emulation.model.EmulatorStatus

val DuckStation = EmulatorComponent(
    id = "duckstation",
    name = "DuckStation",
    description = "Fast and accurate PS1 emulation",
    purpose = "PS1 standalone",
    estimatedSizeRange = "6-15 MB",
    notes = "Fast + accurate",
    supportedFormats = listOf("PSX", "CUE", "BIN", "CHD"),
    features = listOf("Hardware acceleration", "Enhanced graphics", "Memory cards"),
    downloadUrl = "https://github.com/stenzek/duckstation/releases/download/v0.1-5922/duckstation-android.apk",
    packageName = "com.github.stenzek.duckstation"
)