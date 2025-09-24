package com.reznor.emulation.emulators

import com.reznor.emulation.model.EmulatorComponent
import com.reznor.emulation.model.EmulatorStatus

val Play = EmulatorComponent(
    id = "play_ps2",
    name = "Play!",
    description = "Lighter PS2 titles support",
    purpose = "PS2",
    estimatedSizeRange = "15-30 MB",
    notes = "Lighter PS2 titles",
    supportedFormats = listOf("ISO", "BIN", "ELF"),
    features = listOf("Basic PS2 support", "Save states"),
    downloadUrl = "https://github.com/jpd002/Play-/releases/download/0.60/play-0.60.apk",
    packageName = "com.virtualapplications.play"
)