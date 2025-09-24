#!/bin/bash

# Script to download/clone emulator repositories

echo "Downloading emulator repositories..."

# Create a directory for repos if it doesn't exist
mkdir -p emulator_repos
cd emulator_repos

# Clone RetroArch
echo "Cloning RetroArch..."
git clone https://github.com/libretro/RetroArch.git

# Clone PPSSPP
echo "Cloning PPSSPP..."
git clone https://github.com/hrydgard/ppsspp.git

# Clone Dolphin
echo "Cloning Dolphin..."
git clone https://github.com/dolphin-emu/dolphin.git

# Clone PCSX2
echo "Cloning PCSX2..."
git clone https://github.com/PCSX2/pcsx2.git

# Clone Play-
echo "Cloning Play-..."
git clone https://github.com/jpd002/Play-.git

# Clone Winlator
echo "Cloning Winlator-CMOD..."
git clone https://github.com/Stredohori/Winlator-CMOD.git

echo "All repositories downloaded successfully!"