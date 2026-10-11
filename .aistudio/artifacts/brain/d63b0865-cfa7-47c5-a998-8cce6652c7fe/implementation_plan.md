# Sheep Weather — Settings Overhaul, Update & APK Downloader, and Startup Sync

Implement a refined settings experience, direct in-app APK auto-downloader, GitHub update checker, startup splash synchronization, and top-bar cleanup while preserving native Android Auto capability.

## User Review & Critical Decisions

> [!IMPORTANT]
> **Summary of Confirmed Choices from Phase 1 Clarification:**
> 1. **Android Auto**: Preserve the native `CarAppService` and vehicle manifest integration, but completely remove the Car Mode button from the phone interface to eliminate UI clutter.
> 2. **APK Download & Updates**: Provide a one-tap button inside the Location pop-out menu that automatically downloads and prompts to install the latest APK (using Android's `DownloadManager` and GitHub releases), along with a "Check for Updates" version comparison.
> 3. **Startup Loading**: Keep the animated sheep splash screen active until the initial weather forecast is fully loaded in the background, transitioning seamlessly into the populated main screen without loading flashes.
> 4. **Settings Relocation & New Controls**: Move Settings to a prominent action inside the Location drawer. Add new capabilities:
>    - Toggle to disable Gemini AI features (hides the card and avoids API network calls).
>    - Celsius (°C) vs. Fahrenheit (°F) temperature toggle across all screens, cards, and hourly breakdowns.
>    - Day / Night theme selector with 3 options: Auto (solar-based), Fixed Light, and Fixed Dark.
>    - App style selector: Cartoon Sheep vs. Realistic Photo sheep.
> 5. **Main Screen Top-Bar Cleanup**: Remove the Cartoon/Photo pill, Car Mode icon, Day/Night toggle, and Settings gear from the top bar so only the Location selector pill and Refresh button remain.

---

## 1. Overview & Core Concept

- **What It Does**: Moves secondary configuration (theme, visual style, AI toggle, temperature units) into a dedicated Settings hub accessible from the Location drawer; streamlines the main dashboard; keeps the splash animation active until live data is ready; and provides direct APK auto-downloading and GitHub version updates.
- **Target Audience**: Users who want a fast, clutter-free weather dashboard with personalized unit choices (°C/°F), privacy controls (disabling AI), and effortless one-tap app updates.
- **Key Value**: Clean visual hierarchy, zero loading spinners on startup, full temperature unit flexibility, and functional self-updating without Google Play dependency.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Seamless Startup Experience**:
   - The user opens Sheep Weather.
   - The cute cartoon sheep breathing animation plays.
   - In the background, `WeatherViewModel` loads the selected location and fetches fresh weather from Open-Meteo and Room database cache.
   - Once data is fully loaded (or safety timeout reached), the splash screen fades smoothly into the fully populated main dashboard—no empty cards or loading spinners.

2. **Cleaned-Up Main Screen Header**:
   - The top header now contains only:
     - **Location Selector Pill** (`LocationOn` + City Name + Arrow) on the left.
     - **Refresh Button** (clean circular icon) on the right.
   - Extraneous buttons (Photo/Cartoon toggle, Car button, Sun/Moon toggle, Settings gear) are removed from the main screen surface, letting the sheep backdrop shine.

3. **Location Pop-Out & Quick Actions**:
   - Tapping the Location selector slides open the drawer.
   - Above the saved cities list, user sees:
     - **Settings Button** (`Settings` icon + "Settings & Preferences" subtitle).
     - **Check for Updates & Auto-Download APK Button** (`Download` / `SystemUpdate` icon + current version badge).
     - **Use Current Location** GPS detection button.
   - Below are the saved favorite cities with selection and delete options.

4. **New Settings Hub**:
   - Tapping Settings opens the dialog containing:
     - **AI Weather Features**: Toggle switch with Gemini logo. When turned OFF, completely removes the AI Overview card from the main screen and disables Gemini API calls.
     - **Temperature Unit**: Segmented toggle between **Celsius (°C)** and **Fahrenheit (°F)**. Automatically recalculates all values across current temp, real feel, min/max, 24h hourly strip, and 7-day forecast.
     - **Appearance & Theme**: 3-way selector for **Auto (Day/Night)**, **Light Mode**, and **Dark Mode**.
     - **Visual Style**: Radio toggle between **Cartoon Sheep** and **Realistic Photo**.
     - **Direct APK Download**: "Download Latest APK" button with direct file download link.
     - **Privacy & Data Saver**: Existing zero-tracking and cache options.

5. **In-App Update & Auto-Download**:
   - Tapping "Check for Updates" compares the local version (`v2.5`) against the latest GitHub release.
   - Shows a dialog with the latest version number, release changelog, and a prominent **"Download & Install APK"** button.
   - Tapping triggers an Android `DownloadManager` request downloading the `.apk` file to the device `Downloads` directory and opens the package installer intent.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Moving Toggles to Settings vs. Keeping in Header**
  - *Chosen Approach*: Move Style (Cartoon/Photo) and Theme (Day/Night/Auto) into Settings, accessed via the Location drawer.
  - *Why*: The top bar had become overcrowded with 5 separate icon buttons. Moving them declutters the header and establishes a clear single entry point for preferences.
- **Decision 2: Unit Conversion Strategy (°C vs. °F)**
  - *Chosen Approach*: Store temperatures in canonical Celsius in the data models / database, and apply unit formatting helper `TemperatureFormatter.format(celsius, unit)` in the UI layer.
  - *Why*: Keeps database cache and API contracts consistent while allowing instant switching without refetching network forecasts.
- **Decision 3: APK Auto-Download Implementation**
  - *Chosen Approach*: Dual-layer downloader:
    1. Query GitHub releases API (`/repos/{owner}/{repo}/releases/latest`) to find the `.apk` asset download URL.
    2. Enqueue the APK via Android `DownloadManager` with notification visibility, and launch `ACTION_VIEW` with `application/vnd.android.package-archive` MIME type when completed. Fallback to direct browser download if storage permissions are restricted.
  - *Why*: Guarantees a working download on any device or emulator without relying on external app stores.
- **Decision 4: Retaining Native Android Auto Service**
  - *Chosen Approach*: Retain `SheepWeatherCarAppService` and `automotive_app_desc.xml` in the Android manifest, but remove `WeatherScreen.CarMode` and the phone screen car button.
  - *Why*: Matches user clarification: Android Auto head unit capability remains intact for cars that connect, but the phone UI remains pure.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Sheep Weather ViewModel                         │
│  State: isLoading, weatherInfo, tempUnit (C/F), isAiEnabled, themeMode │
└───────────────────┬────────────────────────────────┬───────────────────┘
                    │                                │
                    ▼                                ▼
       ┌────────────────────────┐       ┌────────────────────────┐
       │   Locations Drawer     │       │     Settings Dialog    │
       │  • Saved Cities List   │       │  • AI Features Toggle  │
       │  • Use Current GPS     │       │  • °C vs °F Selector   │
       │  • Open Settings       │       │  • Auto/Light/Dark     │
       │  • Check for Updates   │       │  • Cartoon vs Photo    │
       │  • Auto-Download APK   │       │  • APK Direct Link     │
       └────────────┬───────────┘       └────────────────────────┘
                    │
                    ▼
       ┌────────────────────────────────────────────────────────┐
       │                AppUpdateManager (Helper)               │
       │  • Queries GitHub Latest Release                       │
       │  • Enqueues APK via Android DownloadManager            │
       │  • Prompts Package Installer Intent                    │
       └────────────────────────────────────────────────────────┘
```

### Component & State Mapping

1. **`WeatherViewModel` State Additions**:
   - `temperatureUnit: TemperatureUnit` (`CELSIUS` or `FAHRENHEIT`), persisted in `SharedPreferences`.
   - `isAiEnabled: Boolean` (default `true`), persisted in `SharedPreferences`.
   - `themeMode: ThemeMode` (`AUTO`, `LIGHT`, `DARK`), replacing binary boolean flags.
   - `isInitialLoadCompleted: StateFlow<Boolean>`: Emits `true` only when the first location's weather data has been retrieved and mapped.
2. **`AnimatedSplashScreen` Synchronization**:
   - Listens to `uiState.isInitialLoadCompleted`.
   - Minimum animation delay of 1.2s to enjoy the cute sheep animation, then dismisses as soon as `isInitialLoadCompleted == true` (with 4s max safety timeout).
3. **`TemperatureFormatter`**:
   - Utility converting Celsius values: `F = (C * 9/5) + 32`.
   - Formats values cleanly (e.g. `16°C` vs `61°F`) across Hero Temperature, Real Feel, Hourly cards, 7-Day forecast, and Layer advice.
4. **`AppUpdateManager`**:
   - Manages version check against GitHub releases API.
   - Implements download & install logic via `DownloadManager`.

---

## Implementation Verification Plan

1. **Compilation Check**: Run `compile_applet` to ensure all new settings, formatters, and update managers compile cleanly.
2. **Splash Screen Startup**: Verify the splash screen remains active until initial weather data is loaded.
3. **Settings Controls**: Verify AI toggle hides the AI card, Celsius/Fahrenheit updates all screen temperatures, and theme selector supports Auto/Light/Dark.
4. **Header Cleanliness**: Verify the main screen header only shows Location and Refresh.
5. **APK Auto-Download & Update Check**: Verify tapping "Check for Updates" and "Download APK" triggers the download action correctly.
