package com.reznor.emulation.model

import com.reznor.emulation.emulators.*

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
    val features: List<String> = emptyList(),
    val downloadUrl: String? = null,
    val packageName: String? = null
)

object EmulatorComponents {
    val ALL_COMPONENTS = listOf(
        Lemuroid,
        DuckStation,
        PPSSPP,
        Dolphin,
        Play,
        Suyu,
        GameNative,
        Winlator
    )
}