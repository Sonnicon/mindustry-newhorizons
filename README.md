# New Horizons
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/Sonnicon/mindustry-newhorizons/Java%20CI%20with%20Gradle?style=for-the-badge) 
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/Sonnicon/mindustry-newhorizons?style=for-the-badge) 
![GitHub](https://img.shields.io/github/license/Sonnicon/mindustry-newhorizons?label=LICENSE&style=for-the-badge)\
[![forthebadge](https://forthebadge.com/images/badges/made-with-crayons.svg)](https://forthebadge.com)
[![forthebadge](https://forthebadge.com/images/badges/uses-badges.svg)](https://forthebadge.com)

A very WIP Mindustry mod.

## Compiling
JDK 8.

### Windows
Plain Jar: `gradlew build`\
Dexify Plain Jar: `gradlew dexify`\
Build Plain & Dexify Jar: `gradlew buildDex`

### *nix
Plain Jar: `./gradlew build`\
Dexify Plain Jar: `./gradlew dexify`\
Build Plain & Dexify Jar: `./gradlew buildDex`

Plain Jar is for JVMs (desktop).\
Dexed Jar is for ARTs (Android). This requires `dx` on your path (Android build-tools).\
These two are separate in order to decrease size of mod download.
