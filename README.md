# Weather Boi

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.3-purple?style=for-the-badge&logo=kotlin" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Gradle-9.5-blue?style=for-the-badge&logo=gradle" alt="Gradle" />
  <img src="https://img.shields.io/badge/API-Open--Meteo-cyan?style=for-the-badge" alt="API" />
  <img src="https://img.shields.io/badge/Platform-Cross--Platform-green?style=for-the-badge" alt="Platform" />
</p>

```text
██╗    ██╗███████╗ █████╗ ████████╗██╗  ██╗███████╗██████╗     ██████╗  ██████╗ ██╗
██║    ██║██╔════╝██╔══██╗╚══██╔══╝██║  ██║██╔════╝██╔══██╗    ██╔══██╗██╔═══██╗██║
██║ █╗ ██║█████╗  ███████║   ██║   ███████║█████╗  ██████╔╝    ██████╔╝██║   ██║██║
██║███╗██║██╔══╝  ██╔══██║   ██║   ██╔══██║██╔══╝  ██╔══██╗    ██╔══██╗██║   ██║██║
╚███╔███╔╝███████╗██║  ██║   ██║   ██║  ██║███████╗██║  ██║    ██████╔╝╚██████╔╝██║
 ╚══╝╚══╝ ╚══════╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝    ╚═════╝  ╚═════╝ ╚═╝
```

> **Weather Boi** is a clean, aesthetic terminal dashboard written in Kotlin. It queries free geocoding and forecast services to render real-time weather information and a 3-day forecast inside a colored terminal box accompanied by dynamic weather-related ASCII/Unicode art and retro synthetic sound cues.

---

## Features

* **Rich Terminal Aesthetics:** Utilizes ANSI escape codes for coloring and styling text, drawing beautiful borders dynamically adjusted to content length.
* **Synth Audio Cues:** Programmatically generates a clear, high-pitched `A5` synth ping on startup (with a standard terminal bell `\u0007` fallback for headless environments).
* **Interactive & CLI Modes:** Run the tool with command-line arguments for a quick forecast check, or fire up the interactive mode to query multiple cities sequentially.
* **Zero Keys Needed:** Uses the **Open-Meteo Geocoding and Weather APIs** which require no accounts, API keys, or signup verification.
* **Text-Based Spinners:** Showcases smooth terminal loading spinners while fetching and geocoding details from the server.

---

## Installation & Setup

### Prerequisites
* **Java Development Kit (JDK 21 or higher)**
* **Kotlin Compiler**
* **Gradle**

We recommend installing these using **[SDKMAN!](https://sdkman.io/)**:

```bash
# 1. Install SDKMAN!
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# 2. Install Java 21 & Kotlin
sdk install java 21.0.2-open
sdk install kotlin
sdk install gradle
```

---

## Running the App

Navigate to the project root directory and execute the Gradle tasks:

### Interactive Mode
Launch the main application prompt where you can search for multiple cities or type `exit`:

```bash
./gradlew run
```

### Single-City Quick Search
Get the weather report for a specific city immediately and exit:

```bash
./gradlew run --args="Cebu City"
```

---

## Layout Showcase

```text
┌──────────────────────────────────────────────────────────────┐
│              │ CEBU CITY, CENTRAL VISAYAS, PHILIPPINES       │
│       .--.   │ ☁️  Overcast                                  │
│    .-(    )--. │ 🌡️  Temp:        26.7°C (Feels like 33.5°C)  │
│   (           ) │ 💧  Humidity:    94.0%                        │
│    `---------' │ 💨  Wind Speed:  2.7 km/h                     │
│              │ 🌧️  Precip:      0.0 mm                      │
├──────────────────────────────────────────────────────────────┤
│                        3-DAY FORECAST                        │
├──────────────────────────────────────────────────────────────┤
│  Tue:  🌦️  Showers                Min: 26.3°C  Max: 32.2°C  │
│  Wed:  🌧️  Drizzle                Min: 26.3°C  Max: 32.4°C  │
│  Thu:  🌦️  Showers                Min: 25.5°C  Max: 32.4°C  │
└──────────────────────────────────────────────────────────────┘
```

---

## Technologies Used
- **Kotlin:** Modern, concise JVM language.
- **Gradle Kotlin DSL:** Type-safe build tool configuration.
- **Ktor Client (CIO):** Asynchronous HTTP requests.
- **Kotlinx Serialization:** Highly performant JSON parsing.
- **Java Sound API:** Synthesized audio generation.
