# Sheep Weather 🐑🌤️

An intuitive, ad-free weather companion for Android built with Jetpack Compose, Material 3, and Open-Meteo.

Designed to be an intuitive weather app that is 100% ad-free and fun to use, Sheep Weather pairs real-time meteorological forecasts with dynamic clothing recommendations demonstrated by an animated mascot sheep.

---

## ✨ Key Features

- **🐑 Dynamic Mascot Outfits**: The mascot sheep automatically adapts its attire based on live temperature, precipitation, wind speed, and cloud cover.
- **📱 Samsung-Style Weather & Clock Home Screen Widget**:
  - Live clock and weather summary directly on your Android launcher.
  - Black or White card background with optional dark-mode auto-sync.
  - Granular transparency slider (0% to 100%).
  - Live preview configuration screen modeled after Samsung One UI.
  - Backward compatible from Android 10 (API 29) through Android 16 & 17+.
- **👕 Smart Layer & Gear Recommendations**: Practical suggestions on what to wear (jackets, sunglasses, umbrellas, rainboots, beanies).
- **📍 Multi-City Support**: Search and save your favorite locations worldwide with fast offline caching powered by Room Database.
- **🔄 Smart Refresh**: Refresh button highlights green when data is more than 1 hour old.
- **🚀 Modern Android Stack**: 100% Kotlin, Jetpack Compose, Material 3 dynamic color theming, Coroutines & Flow, Retrofit, and Room.

---

## 📥 Installation

Download the latest ready-to-install APK (`SheepWeather-v2.6.5.apk`) from the [Releases](https://github.com/TwinOfChase/Sheep-Weather/releases) tab.

1. Download `SheepWeather-v2.6.5.apk` to your Android device.
2. Tap the downloaded file in your browser downloads or Files app.
3. Tap **Install** (if prompted, allow "Install unknown apps" for your browser).
4. Launch **Sheep Weather**!

---

## 🛠️ Building from Source

```bash
# Clone the repository
git clone https://github.com/TwinOfChase/Sheep-Weather.git

# Navigate to project directory
cd Sheep-Weather

# Build debug APK
gradle assembleDebug
```
The compiled APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📄 License
Open source and ad-free.
