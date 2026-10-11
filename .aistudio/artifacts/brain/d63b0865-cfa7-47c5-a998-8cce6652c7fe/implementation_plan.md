# Sheep Weather — Android Auto & Four-Quadrant Car Display

Deliver a complete Android Auto experience for Sheep Weather so the app appears in the Android Auto launcher alongside apps like Spotify and Messages, while also providing an interactive **Car Display Mode** directly inside the app featuring the custom **4-Quadrant Layout**, a **Central Speak Aloud (TTS) Button**, and **Park vs. Driving Mode** adaptation.

## User Review & Critical Decisions

> [!IMPORTANT]
> **How Android Auto Templates vs. Custom 4-Quadrant Canvas Work Together**
> Android Auto head units enforce template restrictions (`GridTemplate` / `PaneTemplate`) for third-party projections while allowing custom canvas rendering on the phone screen or dashboard display. To give you the best of both worlds, we will implement:
> 1. **Native Android Auto Launcher Service (`CarAppService`)**: Registers Sheep Weather in the car's Android Auto launcher menu with a 4-item grid/dashboard, voice readout action, and live `CarUxRestrictions` / gear state detection for Park vs. Driving mode.
> 2. **Full-Screen 4-Quadrant Car Display Mode (In-App & Dashboard View)**: Renders the exact 2×2 four-quadrant split screen with the circular **Speak** button centered at the intersection of the four quadrants, automatic/manual **Park vs. Drive** mode switching, and native Android Text-to-Speech readout.

- **Confirmed Decision 1 — Four-Quadrant Layout**:
  - **Quadrant 1 (Top-Left — Temperature)**: Large current temperature, "Feels like" temperature, condition icon, and city label mirroring the phone's primary weather display.
  - **Quadrant 2 (Top-Right — Hourly Forecast)**: Capped at **4 upcoming hours** in Driving Mode for quick glanceability; unlocks a **scrollable full hourly forecast** when in Park Mode.
  - **Quadrant 3 (Bottom-Left — Layer / Outfit & Umbrella)**: Strictly minimal display showing **only** the recommended layer count (`3 Layers`) and umbrella status (`No Umbrella` / `Umbrella Needed` / `Too Windy for Umbrella`) with bold iconography and no extra text clutter.
  - **Quadrant 4 (Bottom-Right — Today's AI Overview)**: Large-font, shortened single-day summary while driving; expands to a **detailed AI overview** in Park Mode.
- **Confirmed Decision 2 — Center Speak Aloud Button**:
  - Anchored at the center intersection of the 4 quadrants. Tapping reads aloud a natural spoken weather briefing (current temperature, feels-like, recommended clothing layers, umbrella advice, and the day's progression from morning to evening) using Android's `TextToSpeech` engine.
- **Confirmed Decision 3 — Vehicle Park Mode Enhancements**:
  - Detects when the vehicle is in **Park** (via Android Auto `CarUxRestrictions` / stationary sensor state, plus a manual Park/Drive selector for testing) to reveal **Sunrise & Sunset times**, a **scrollable hourly forecast**, and an **expanded AI overview**.
- **Confirmed Decision 4 — Build Stability Fix**:
  - Resolves the KSP headless AWT `NullPointerException` build error by configuring headless JVM properties and KSP2 execution settings.

---

## 1. Overview & Core Concept

- **What It Does**: Extends Sheep Weather to the vehicle dashboard with a distraction-optimized 4-quadrant layout, one-tap spoken weather and outfit briefings over car audio, and automatic detail expansion when the car is parked.
- **Target Audience / Persona**: Drivers and commuters who want an immediate visual or spoken check of the temperature, how many layers to wear, whether an umbrella is needed (or if it's too windy), and how the day's weather will evolve.
- **Key Value**: Eliminates small text and multi-screen scrolling while driving by condensing essential weather intelligence into four high-contrast quadrants and a single central voice readout button.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Connecting to Android Auto**:
   - When the phone connects to an Android Auto head unit, **Sheep Weather** appears in the car's app launcher alongside navigation, media, and messaging apps.
   - Selecting Sheep Weather opens the car dashboard with instant access to all 4 weather categories, a **Speak Forecast** action in the header/center, and automatic **Park vs. Drive** detail switching.
2. **Using the 4-Quadrant Car Display**:
   - Tapping the **Car Mode (Android Auto)** icon on the main phone screen (or rotating/docking in the car) launches the full-screen 2×2 quadrant layout:
     - **Top-Left (Q1 — Temperature)**: Extra-large temperature readout (e.g., `16°C`), `Feels like 12°C`, condition icon, and location name. When in **Park Mode**, displays **Sunrise & Sunset** badges (`Sunrise 6:15 AM • Sunset 7:45 PM`).
     - **Top-Right (Q2 — Hourly)**: In **Drive Mode**, displays a fixed 4-column row of the next 4 hours (`Now`, `+1h`, `+2h`, `+3h`) at large scale. In **Park Mode**, unlocks smooth horizontal/vertical scrolling through the next 24 hours.
     - **Bottom-Left (Q3 — Layer / Outfit)**: Displays only the layer count badge (e.g., `👕 3 Layers`) and the umbrella badge (e.g., `🌂 No Umbrella`, `☂️ Umbrella Needed`, or `💨 Too Windy`) in oversized, high-contrast pills with zero secondary text clutter.
     - **Bottom-Right (Q4 — AI Overview)**: In **Drive Mode**, displays a concise, single-day progression in large 20sp+ text (morning, midday, and evening summary). In **Park Mode**, expands into a scrollable detailed overview with full day analysis and dressing tips.
3. **Center Speak Button Interaction**:
   - Floating at the exact center intersection of the four quadrants is a large circular **Speak** button (`64dp+` touch target).
   - Tapping it synthesizes and speaks a natural narrative aloud:
     > *"It's currently 16 degrees and feels like 12 degrees, 3 layers of clothing is recommended and an umbrella is not needed. Wellington starts with clear skies and a cool 11 degrees, towards midday more overcast and temperatures of around 16. Showers starting early evening."*
   - While speaking, the center button displays an animated audio waveform ring and allows tapping again to stop playback immediately.

### Visual Identity & Theme
- **Aesthetic Direction**: Automotive Cockpit High-Contrast UI — deep slate-navy surfaces (`#0B132B` / `#14203B`), crisp quadrant dividers, oversized numerals, and warm amber/cyan accents optimized for peripheral glanceability in both daytime glare and night driving.
- **Typography & Hierarchy**:
  - Quadrant 1 Hero Temperature: `48sp` ExtraBold
  - Quadrant 3 Layer & Umbrella Status: `22sp` Bold with `36dp` icons
  - Quadrant 2 Hourly Items & Quadrant 4 Short AI Overview: `18sp–20sp` Medium with high line height for effortless reading at arm's length.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dual Support — Native `CarAppService` + In-App 4-Quadrant Car Display**
  - *Chosen Approach*: Implement both the official Jetpack `androidx.car.app` service (so Android Auto head units list Sheep Weather in the car launcher) and a dedicated full-screen 4-Quadrant Car Display screen inside the app.
  - *Why*: Android Auto's projection protocol enforces standardized templates (`GridTemplate` / `PaneTemplate`) on external car screens, whereas the in-app Car Display renders the exact custom 2×2 quadrant geometry with the overlapping center Speak button and lets you test Drive vs. Park mode immediately in the preview.
- **Decision 2: Natural Spoken Weather Script Generator for TTS**
  - *Chosen Approach*: Build a dedicated spoken-script formatter that combines current temp, feels-like temp, layer count, umbrella status (including wind warnings), and a morning/midday/evening progression derived from the hourly and AI forecast.
  - *Why*: Matches the exact conversational readout style requested without reading raw symbols or markdown tags aloud.
- **Decision 3: Hardware & UX Restriction Park Detection**
  - *Chosen Approach*: Listen to Android Auto `CarUxRestrictions` (`UX_RESTRICTIONS_NO_SETUP` / `UX_RESTRICTIONS_LIMIT_STRING_LENGTH` cleared when parked) and device stationary/speed state, paired with an on-screen **DRIVE / PARK** status pill that can also be toggled manually.
  - *Why*: Automatically adapts in vehicles that report gear/park state to Android Auto while remaining testable anytime.

---

## 4. Technical Architecture & Data Strategy

### Architecture & Component Diagram

```
┌───────────────────────────────────────────────────────────────────────────┐
│                         Sheep Weather Repository                          │
│  Open-Meteo Forecast + LayersForecastCalculator + GeminiWeatherService    │
└───────────────────┬───────────────────────────────────┬───────────────────┘
                    │                                   │
                    ▼                                   ▼
┌───────────────────────────────────────┐ ┌─────────────────────────────────┐
│     Android Auto Head Unit Service    │ │   4-Quadrant Car Display UI     │
│  (SheepWeatherCarAppService & Screen) │ │     (CarModeWeatherScreen)      │
│  • Appears in Android Auto Launcher   │ │  ┌──────────────┬────────────┐  │
│  • Observes CarUxRestrictions (Park)  │ │  │ Q1: Temp     │ Q2: Hourly │  │
│  • Grid/Pane Templates + Voice Action │ │  │ (+Sun in P)  │ (4h / All) │  │
└───────────────────┬───────────────────┘ │  ├──────┬───────┴─────┬──────┤  │
                    │                     │  │      │ 🔊 SPEAK    │      │  │
                    │                     │  ├──────┴───────┬─────┴──────┤  │
                    │                     │  │ Q3: Layers   │ Q4: AI Day │  │
                    │                     │  │ & Umbrella   │ Overview   │  │
                    │                     │  └──────────────┴────────────┘  │
                    └──────────┬──────────┴─────────────┬───────────────────┘
                               ▼                        ▼
                    ┌─────────────────────────────────────────────┐
                    │       WeatherSpeechSynthesizer (TTS)        │
                    │  • Formats natural spoken weather narrative │
                    │  • Manages Android TextToSpeech lifecycle   │
                    └─────────────────────────────────────────────┘
```

### Interactive Component & State Mapping
- **Park / Drive Mode State**:
  - **Driving State**: Locks Q2 to `hourly.take(4)` (non-scrolling), hides sunrise/sunset metadata in Q1 to reduce visual density, and formats Q4 as a concise 2–3 sentence day progression (morning, midday, evening) in `20sp` font.
  - **Parked State**: Unlocks horizontal/vertical scrolling across all 24 hours in Q2, reveals Sunrise & Sunset badges in Q1, and expands Q4 to show the full Today summary, Upcoming trend, and Dressing tips.
- **Center Speak Button (`WeatherSpeechSynthesizer`)**:
  - Initializes `android.speech.tts.TextToSpeech` with `Locale.getDefault()`.
  - Generates the exact spoken string:
    `"It's currently {temp} degrees and feels like {realFeel} degrees, {layerCount} layers of clothing is recommended and {umbrellaPhrase}. {locationName} starts with {morningDesc} and a {morningFeel} {morningTemp} degrees, towards midday {middayDesc} and temperatures of around {middayTemp}. {eveningPhrase}."`
  - Toggles between idle (`Speak`) and active speaking (`Stop`) states with visual feedback.
- **KSP & Gradle Stability**:
  - Configures `-Djava.awt.headless=true` in `gradle.properties` and aligns the KSP plugin configuration so Room and Moshi code generation complete cleanly without AWT event queue exceptions.
