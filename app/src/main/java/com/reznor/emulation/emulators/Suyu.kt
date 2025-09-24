package com.reznor.emulation.emulators

import com.reznor.emulation.model.EmulatorComponent
import com.reznor.emulation.model.EmulatorStatus

val Suyu = EmulatorComponent(
    id = "suyu",
    name = "suyu (Yuzu fork)",
    description = "Nintendo Switch emulation",
    purpose = "Nintendo Switch",
    estimatedSizeRange = "50-130 MB",
    notes = "Yuzu fork",
    supportedFormats = listOf("NSP", "XCI", "NCA"),
    features = listOf("Modern graphics", "Vulkan support", "Touch controls"),
    downloadUrl = "https://github.com/suyu-emu/suyu/releases/download/1/suyu-emu-suyu-android-1.apk",
    packageName = "org.suyu.suyu"
)