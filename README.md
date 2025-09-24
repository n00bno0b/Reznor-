# Reznor Emulation Manager
An Android app for your emulation needs

## Features

Reznor is a comprehensive emulation management app that provides a unified interface for managing various emulator components. The app includes:

### Supported Emulators

| Component | Purpose | Est. Size (per-ABI) | Notes |
|-----------|---------|---------------------|--------|
| Manager APK (Console UI) | Library, art, controller-first UX | 20–50 MB | Jetpack Compose, assets, glyphs |
| Lemuroid (RetroArch fork) | Multi-system emulation | 20–50 MB | Multiple systems support |
| DuckStation | PS1 standalone | 6–15 MB | Fast + accurate |
| PPSSPP | PSP | 20–40 MB | Gold standard |
| Dolphin | GameCube/Wii | 20–35 MB | Vulkan/OpenGL ES |
| Play! | PS2 | 15–30 MB | Lighter PS2 titles |
| suyu (Yuzu fork) | Nintendo Switch | 50–130 MB | Modern Nintendo Switch emulation |
| GameNative (+ Wine/Box64 bits) | PC/Steam titles | 250–450 MB | PC games on Android |

### Key Features

- **Modern UI**: Built with Jetpack Compose for a smooth, material design experience
- **Component Management**: Install only the emulators you need
- **Status Tracking**: Real-time status updates for installations and downloads
- **Detailed Information**: View supported formats, features, and size estimates for each emulator
- **Organized Library**: Filter between all, installed, and available emulators

## Build Instructions

1. Clone this repository
2. Open in Android Studio
3. Build and run the project

## Requirements

- Android SDK 24+ (Android 7.0)
- Android Studio Flamingo or newer
- Gradle 8.0+

## Architecture

The app is built using:
- **Kotlin** for the programming language
- **Jetpack Compose** for the UI framework
- **Material 3** for design system
- **ViewModel + StateFlow** for state management
- **MVVM Architecture** for clean separation of concerns
