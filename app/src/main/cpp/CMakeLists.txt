cmake_minimum_required(VERSION 3.10.2)

project("reznor-emulation")

# Create shared library for libretro integration
add_library(libretro-integration SHARED
    libretro-integration.cpp)

# Include directories
target_include_directories(libretro-integration PRIVATE
    ../../../../emulator_repos/RetroArch/libretro-common/include
    ../../../../emulator_repos/RetroArch
    ../../../../emulator_repos/RetroArch/libretro-common/include/encodings
    ../../../../emulator_repos/RetroArch/libretro-common/include/string
    ../../../../emulator_repos/RetroArch/libretro-common/include/compat
    ../../../../emulator_repos/RetroArch/libretro-common/include/libretro)

# Link libraries
target_link_libraries(libretro-integration
    log
    android
    EGL
    GLESv2)