package com.reznor.emulation.emulators

import com.reznor.emulation.model.EmulatorComponent
import com.reznor.emulation.model.EmulatorStatus

val Dolphin = EmulatorComponent(
    id = "dolphin",
    name = "Dolphin",
    description = "GameCube and Wii emulation",
    purpose = "GameCube/Wii",
    estimatedSizeRange = "20-35 MB",
    notes = "Vulkan/OpenGL ES",
    supportedFormats = listOf("ISO", "GCM", "WBFS", "WAD"),
    features = listOf("Motion controls", "Online play", "HD graphics"),
    downloadUrl = "https://dl.dolphin-emu.org/releases/2407/dolphin-master-2407-75.apk",
    packageName = "org.dolphinemu.dolphinemu"
)