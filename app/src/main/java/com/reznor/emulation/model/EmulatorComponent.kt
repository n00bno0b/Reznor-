package com.reznor.emulation.model

enum class EmulatorStatus {
    NOT_INSTALLED,
    INSTALLED,
    DOWNLOADING,
    ERROR
}

data class EmulatorComponent(
    val id: String,
    val name: String,
    val description: String,
    val purpose: String,
    val estimatedSizeRange: String,
    val notes: String,
    val status: EmulatorStatus = EmulatorStatus.NOT_INSTALLED,
    val iconRes: Int? = null,
    val supportedFormats: List<String> = emptyList(),
    val features: List<String> = emptyList()
)

object EmulatorComponents {
    val ALL_COMPONENTS = listOf(
        EmulatorComponent(
            id = "lemuroid",
            name = "Lemuroid (RetroArch)",
            description = "Multi-system emulator library",
            purpose = "Library, art, controller-first UX",
            estimatedSizeRange = "20-50 MB",
            notes = "Jetpack Compose, assets, glyphs",
            supportedFormats = listOf("NES", "SNES", "GB", "GBC", "GBA", "Genesis", "N64"),
            features = listOf("Multiple systems", "Save states", "Controller support")
        ),
        EmulatorComponent(
            id = "duckstation",
            name = "DuckStation",
            description = "Fast and accurate PS1 emulation",
            purpose = "PS1 standalone",
            estimatedSizeRange = "6-15 MB",
            notes = "Fast + accurate",
            supportedFormats = listOf("PSX", "CUE", "BIN", "CHD"),
            features = listOf("Hardware acceleration", "Enhanced graphics", "Memory cards")
        ),
        EmulatorComponent(
            id = "ppsspp",
            name = "PPSSPP",
            description = "Gold standard PSP emulator",
            purpose = "PSP",
            estimatedSizeRange = "20-40 MB",
            notes = "Gold standard",
            supportedFormats = listOf("ISO", "CSO", "PBP"),
            features = listOf("HD graphics", "Save states", "Multiplayer")
        ),
        EmulatorComponent(
            id = "dolphin",
            name = "Dolphin",
            description = "GameCube and Wii emulation",
            purpose = "GameCube/Wii",
            estimatedSizeRange = "20-35 MB",
            notes = "Vulkan/OpenGL ES",
            supportedFormats = listOf("ISO", "GCM", "WBFS", "WAD"),
            features = listOf("Motion controls", "Online play", "HD graphics")
        ),
        EmulatorComponent(
            id = "play_ps2",
            name = "Play!",
            description = "Lighter PS2 titles support",
            purpose = "PS2",
            estimatedSizeRange = "15-30 MB",
            notes = "Lighter PS2 titles",
            supportedFormats = listOf("ISO", "BIN", "ELF"),
            features = listOf("Basic PS2 support", "Save states")
        ),
        EmulatorComponent(
            id = "suyu",
            name = "suyu (Yuzu fork)",
            description = "Nintendo Switch emulation",
            purpose = "Nintendo Switch",
            estimatedSizeRange = "50-130 MB",
            notes = "Yuzu fork",
            supportedFormats = listOf("NSP", "XCI", "NCA"),
            features = listOf("Modern graphics", "Vulkan support", "Touch controls")
        ),
        EmulatorComponent(
            id = "gamenative",
            name = "GameNative",
            description = "PC and Steam titles with Wine/Box64",
            purpose = "PC/Steam titles",
            estimatedSizeRange = "250-450 MB",
            notes = "Wine/Box64 bits",
            supportedFormats = listOf("EXE", "MSI"),
            features = listOf("Wine compatibility", "Steam integration", "x86 translation")
        )
    )
}