# Necromancy — Minecraft Forge 1.20.1

Starter mod project for Minecraft Java Edition 1.20.1 / Forge 47.2.0 / Java 17.

## Features
- **Soul**: 22% chance to drop when a player kills a non-player living entity.
- **Necromancer Staff**: right-click air or use on a block to summon a skeleton or zombie. In Survival, each summon consumes one Soul and damages the staff by 1 durability. Creative mode is free.
- **Soul Altar**: decorative, softly glowing purple block (crafting recipe not included yet).

## Build
1. Install a Java 17 JDK.
2. Install Minecraft Forge MDK for **1.20.1**, or let Gradle download dependencies.
3. In this folder, run:
   - Windows: `gradlew.bat build`
   - Linux/macOS: `./gradlew build`
4. The built jar will be in `build/libs/necromancy-1.0.0.jar`.
5. Copy the jar into `.minecraft/mods` and launch the Forge 1.20.1 profile.

This source project does not include the Gradle wrapper JAR/scripts; if `gradlew` is unavailable, open it in IntelliJ IDEA with Gradle installed or add the wrapper using `gradle wrapper`.

## Getting items in-game
Use commands:
- `/give @p necromancy:soul 8`
- `/give @p necromancy:necromancer_staff 1`
- `/give @p necromancy:soul_altar 1`

## Notes
This is a starter implementation, not a compiled/tested release. The altar is currently decorative and the summoned undead are vanilla mobs with a custom name; they do not yet have owner-following AI. Test in a separate world before using it in a main save.


## Easiest build method (GitHub Actions)
This project includes `.github/workflows/build.yml`. To build without installing Java/Gradle locally:
1. Sign in to GitHub and create a **new empty repository**.
2. Upload the extracted project files, including the hidden `.github` folder, and commit them to `main`.
3. Open the repository's **Actions** tab and select **Build Necromancy mod**.
4. Click **Run workflow** (or wait for the first push build to finish).
5. Open the completed workflow run. Under **Artifacts**, download `necromancy-forge-1.20.1`. It contains the built JAR.

GitHub must be able to access Maven/Gradle repositories to download Forge dependencies. This workflow has been added but has not been run from this environment, so the build result still needs verification.
