# M-Hike: Native Android Hiker Management Application 🏔️

**M-Hike** is a feature-rich, local-first native Android application built in **Java** for outdoor enthusiasts to plan, document, and track hiking journeys and trail observations. Developed for the **COMP1786 Mobile Application Design and Development** coursework (Term 1, 2025-26).

---

## ✨ Features Overview

### 📋 Core Features (Features A – D)
- **Feature A — Hike Data Entry & Validation:**
  - Mandatory fields: Hike Name, Location, Date (DatePicker), Parking (Yes/No), Length (km), Difficulty (Easy, Moderate, Hard, Expert).
  - Optional fields: Description, Custom Note 1 & 2, Cover Photo, GPS Coordinates, Trail Rating, Weather Notes.
  - Real-time inline error handling via `TextInputLayout` and pre-save confirmation modal.
- **Feature B — Local Room Database Persistence (CRUD):**
  - Powered by **Room Database v4** with full migration support.
  - Local-first architecture — complete offline availability without internet.
  - Interactive `RecyclerView` list with DiffUtil for smooth animated updates, difficulty color badges, and cover photo thumbnails.
- **Feature C — In-Field Observations:**
  - Attach unlimited observations to any hike (Title, locked current clock time, comments, step count, photo, temperature).
  - Auto-fills current temperature (°C) and 1-hour weather forecast via Open-Meteo API.
  - Cascading deletion: deleting a hike automatically removes its observations.
- **Feature D — Advanced Search & Filter:**
  - Live search bar with instant name-matching (`LOWER(name) LIKE`).
  - Multi-criteria filter screen (location, date range, min/max length, difficulty) built safely with parameterized `SupportSQLiteQuery`.

---

### 🌟 Advanced Feature Pack (Features E – G)
- **Feature E — Firebase Authentication:**
  - Secure email/password login & registration via Firebase Auth.
  - User session caching for offline access.
- **Feature F — Cloud Synchronization:**
  - Best-effort background push sync to **Firebase Realtime Database** (`users/{uid}/hikes/{hikeId}`).
  - Local dirty flag (`is_synced`) with automatic retry on network reconnection.
- **Feature G1 — Map View & GPS Trailhead Capture:**
  - Integrated **Mapbox Maps SDK** for full-screen interactive trail mapping.
  - GPS trailhead location capture via `FusedLocationProviderClient` with auto-clipboard copy.
- **Feature G2 — Photo Capture & Storage:**
  - Take cover photos for Hikes and Observations using system Camera or Gallery picker (`FileProvider`).
  - Displayed as 56x56dp rounded card covers in the list and 200dp hero banners in the detail view.
- **Feature G3 — Walking Duration Calculator:**
  - Auto-calculates estimated walking duration based on Naismith's Rule (12 min/km × difficulty multiplier).
  - Displays pace comparison ("On pace", "Faster", "Slower").
- **Feature G4 — Weather Forecast Integration:**
  - Live weather banner powered by **Open-Meteo API** (free, no API key required).
  - Surfaces current outdoor temperature alongside predicted temperature 1 hour ahead when adding observations.
  - Weather warnings for upcoming rain, snow, or storm conditions.
- **Feature G5 — Export to PDF & Share:**
  - Generates beautiful A4 PDF hike reports natively using `android.graphics.pdf.PdfDocument`.
  - Instant sharing via Android system Share Sheet (Email, Drive, WhatsApp).
- **Feature G6 — Trail Condition Rating System:**
  - 1–5 star rating system (`RatingBar`) with descriptive labels (*Very Poor* to *Excellent*) and review notes.
- **🌱 Automatic Developer Seed Data (`DevSeedHelper`):**
  - Automatically seeds 8 realistic hikes (Vietnam & UK) + 19 detailed observations whenever the database is empty.

---

## 🛠️ Tech Stack & Architecture

- **Platform:** Native Android (API 26+ / Android 8.0+)
- **Target SDK:** 35 (Android 15) | **Compile SDK:** 35
- **Language:** Java
- **UI Architecture:** Material Design 3 (Forest Green Theme `#386A1F`), ViewBinding throughout.
- **Database:** Room v4 (Entities: `Hike`, `Observation`) with single-threaded `ExecutorService` & `Handler` main-thread dispatching.
- **Authentication & Cloud:** Firebase Auth & Firebase Realtime Database (`com.google.firebase:firebase-bom:32.7.0`).
- **Mapping & Location:** Mapbox Maps SDK (`com.mapbox.maps:android`) & Google Play Services Location (`play-services-location`).
- **Network & JSON:** OkHttp 4.12.0 & Gson 2.10.1.
- **PDF Export:** Native `android.graphics.pdf.PdfDocument` + `FileProvider`.

---

## 📂 Project Structure

```text
com.example.m_hikeapp
├── adapter/
│   ├── HikeAdapter.java              # RecyclerView adapter for hike list (with cover photo & diff badges)
│   └── ObservationAdapter.java       # RecyclerView adapter for observation list
├── dao/
│   ├── HikeDao.java                  # Room DAO queries for hikes (@Query, @RawQuery)
│   └── ObservationDao.java           # Room DAO queries for observations
├── database/
│   └── AppDatabase.java              # Room database singleton v4 with migrations (1_2, 2_3, 3_4)
├── export/
│   └── PdfReportBuilder.java         # PDF document generation helper (Feature G5)
├── model/
│   ├── Hike.java                     # Hike entity (21 columns, v4 schema)
│   └── Observation.java              # Observation entity (8 columns, FK ON DELETE CASCADE)
├── repository/
│   └── HikeRepository.java           # Thread-safe repository gateway (ExecutorService + Handler)
├── sync/
│   └── FirebaseSyncHelper.java       # Firebase Realtime Database cloud sync delegate
├── util/
│   ├── DevSeedHelper.java            # Automatic sample data seeder (8 hikes + 19 observations)
│   ├── DurationCalculator.java       # Naismith rule walking duration calculator (Feature G3)
│   ├── ImageUriUtils.java            # FileProvider photo capture & URI helper (Feature G2)
│   ├── ValidationResult.java         # Form validation error map
│   ├── ValidationUtils.java          # Reusable validation logic
│   ├── WeatherContract.java          # Weather condition constants
│   └── WeatherHelper.java            # Open-Meteo API client with 1-hour forecast (Feature G4)
├── AddHikeActivity.java              # Create & edit hike form + GPS + Camera
├── AddObservationActivity.java       # Observation form + auto-time + weather forecast
├── HikeDetailActivity.java           # Detailed hike view + hero cover + observations list + PDF export
├── HikeListActivity.java             # Main activity: list, search, weather banner, auto-seed
├── HikeMapActivity.java              # Mapbox interactive map view (Feature G1)
├── LoginActivity.java                # Firebase email/password authentication
├── MainActivity.java                 # Auth gate & launcher redirect
├── MhikeApplication.java            # Application class
└── SearchFilterActivity.java         # Advanced multi-criteria search screen
```

---

## ⚙️ Installation & Setup

1. **Prerequisites:**
   - Android Studio (Ladybug 2024.2+ recommended)
   - JDK 17
   - Android SDK 35

2. **Clone & Open:**
   ```bash
   git clone https://github.com/900M100CK/M-Hike-android-app.git
   ```
   Open the root directory in Android Studio.

3. **Mapbox & Firebase Configuration:**
   - Add your Mapbox Access Token to `local.properties`:
     ```properties
     MAPBOX_DOWNLOADS_TOKEN=your_mapbox_secret_download_token
     ```
     And set `<string name="mapbox_access_token">your_public_mapbox_token</string>` in `app/src/main/res/values/mapbox_access_token.xml`.
   - Place your `google-services.json` in the `app/` directory for Firebase Auth/RTDB functionality.

4. **Run Project:**
   - Sync Gradle and press **Run** (Shift + F10) on an emulator or physical device.
   - On first launch, 8 realistic sample hikes with observations will be seeded automatically!
