# M-Hike Android App — Agent Briefing & Development Plan (v3.0)

**Version:** 3.0 (Room + Firebase Hybrid + Extended Feature Pack)
**Platform:** Android (Native Java)
**Tech Stack:** Java, Android SDK, Room Database, Material Design 3, Firebase Auth, Firebase Realtime Database, Google Maps SDK, Camera + FileProvider
**Status:** Coursework Implementation (COMP1786 Term 1)

---

## 1. PROJECT CONTEXT

### Overview
**M-Hike** is a hiker management mobile application that allows outdoor enthusiasts to plan hikes, record detailed observations during hikes, and manage hike data locally with optional cloud backup. This is the **native Android implementation** built on the **v2.0 Room baseline** with a **Firebase hybrid layer** (Auth + Realtime Database sync).

**v3.0 scope**: Everything in v2.0, plus the full extended feature pack — all six **Feature G** options:

| Code | Feature | Deliverable |
|------|---------|-------------|
| G1 | Map View & GPS | Hike position on map, GPS capture |
| G2 | Photo Capture & Storage | Camera photo per hike, stored locally |
| G3 | Duration Calculator | Estimated + actual duration |
| G4 | Weather Notes Template | Structured weather fields |
| G5 | Export to PDF / Share | Generate & share hike report |
| G6 | Trail Condition Ratings | 5-level trail rating |

### Target Users
- Outdoor hikers aged 18-65
- Mixed technical proficiency (no assumed programming knowledge)
- Users hiking locally (UK focus, but generalizable)
- Secondary use: community knowledge sharing (future scope)

### Business Goals
1. Simplify hike planning and documentation
2. Enable users to capture real-time observations (wildlife, vegetation, weather, trail conditions)
3. Build a local-first app that doesn't require internet connectivity for core features
4. Provide intuitive UI requiring no manual or instructions
5. Let users relocate/map a hike, document it with photos, and export a shareable PDF report

### Constraints & Requirements
- **Technology**: Android Java (not Xamarin/MAUI, not React Native for this version)
- **Persistence**: Room Database (SQLite abstraction) as the single source of truth
- **Cloud**: Firebase Auth (email/password) + Firebase Realtime Database (best-effort sync of the authenticated user's hikes)
- **Scope**: Features a-f plus all six additional features from feature g
- **Deliverables**: Working app + 15min demo video + comprehensive report
- **Assessment deadline**: Arranged by partnerships (end of term)

---

## 2. FEATURE SPECIFICATIONS & RULES

### Feature A: Hike Data Entry (10% of coursework)

**Rule 1: Form Validation**
- **Required Fields** (error message if empty):
  - Hike Name (e.g., "Snowdon", "Trosley Country Park")
  - Location (text input with optional postcode)
  - Date of hike (date picker, must allow future dates)
  - Parking available (radio buttons: Yes/No)
  - Length of hike (e.g., 5 km; numeric + unit picker)
  - Difficulty level (dropdown: Easy, Moderate, Hard, Expert)

- **Optional Fields** (no error if empty):
  - Description (multi-line text, max 500 chars)
  - Two or more additional fields — in v3.0 these are the structured G-feature fields (GPS, photo, weather, trail rating, duration), shown in a collapsible "Extras" section so the base form stays simple.

**Rule 2: UI/UX Standards**
- Use Android native controls (not custom implementations unless justified):
  - `EditText` for text input
  - `DatePicker` (dialog or spinner) for dates
  - `RadioGroup` for binary choices
  - `Spinner` for difficulty level
  - `NumberPicker` or formatted `EditText` for numeric inputs
- Minimize manual text entry; use pickers where possible
- Provide sensible defaults (e.g., today's date, common difficulty)
- Show **confirmation screen** after data entry with all fields displayed
- Allow user to edit or confirm from confirmation screen
- Clear error messaging: "Location is required" not "ERROR: null"

**Rule 3: Data Input Guidelines**
- Hike name: 1-100 characters
- Location: 1-100 characters
- Date: past or future, reasonable range (1900-2100)
- Length: 0.1-500 km
- Description: 0-500 characters
- Custom fields: match app's design consistency

---

### Feature B: Data Persistence & Management (15% of coursework)

**Rule 1: Room Database Schema (v3)**
- **Database Class**: `AppDatabase` (extends `RoomDatabase` with versioning and migration pathways).
  - Schema version **3** (migrated from v2 via `MIGRATION_2_3`). Cloud sync relies on destructive-migration safety only as a fallback (`fallbackToDestructiveMigration()` must never be triggered in normal use).
- **Hikes Table (`hikes` Entity)** — v2 columns:
  - `id`: `long` Primary Key, auto-generated.
  - `name`: `String` (Required).
  - `location`: `String` (Required).
  - `date`: `String` (Required, YYYY-MM-DD).
  - `parking_available`: `boolean` (Required, maps to INTEGER 1/0 in SQLite).
  - `length_km`: `double` (Required).
  - `difficulty`: `String` (Required).
  - `description`: `String` (Optional).
  - `custom_field_1`: `String` (Optional).
  - `custom_field_2`: `String` (Optional).
  - `user_id`: `String` (Optional — Firebase UID owner; nullable to preserve local-first behavior).
  - `is_synced`: `boolean` (default 0 — dirty flag for RTDB sync).
- **Hikes Table (`hikes` Entity)** — **v3 additions (all nullable/optional)**:
  - `latitude`: `Double` (Optional — G1 GPS; nullable boxed type so Room stores SQL NULL).
  - `longitude`: `Double` (Optional — G1 GPS).
  - `photo_uri`: `String` (Optional — G2; `content://` or absolute file path).
  - `estimated_duration_min`: `Integer` (Optional — G3, auto-computed).
  - `actual_duration_min`: `Integer` (Optional — G3, user-entered).
  - `weather_condition`: `String` (Optional — G4; picker value).
  - `weather_notes`: `String` (Optional — G4; free text, max 500 chars).
  - `trail_rating`: `Integer` (Optional — G6; 1-5 inclusive).
  - `trail_notes`: `String` (Optional — G6; free text).
- **Observations Table (`observations` Entity)** — unchanged from v2:
  - `id`: `long` Primary Key, auto-generated.
  - `hike_id`: `long` (Foreign Key pointing to `hikes.id` with `ON DELETE CASCADE` and index tracking).
  - `title`: `String` (Required).
  - `obs_time`: `String` (Required, HH:mm format).
  - `comment`: `String` (Optional).

**Rule 2: CRUD Operations**
- **Create**: Insert new hike/observation via `@Insert(onConflict = OnConflictStrategy.ABORT)` annotations.
- **Read**: Fetch lists or entries by ID using `@Query` compile-time validated SQL strings.
- **Update**: Edit fields using `@Update` annotations.
- **Delete**: Remove items using `@Delete` annotations.
- **Reset**: `@Query("DELETE FROM hikes")` clears all hikes and automatically cascades to delete all observations.
- **Unsynced read**: `@Query("SELECT * FROM hikes WHERE user_id = :uid AND is_synced = 0")` returns rows pending RTDB push.

**Rule 3: List Display**
- Show hikes in list view with key info: Name, Location, Date, Difficulty (color-coded text badge), Parking (icon/badge), Trail Rating (stars), Photo (thumbnail if present)
- Implement `RecyclerView` with `ListAdapter` + `DiffUtil` for animated, high-performance rendering.
- Empty state message when no hikes exist.

**Rule 4: Threading & Performance**
- **Main Thread Isolation**: Room queries are strictly prohibited on the main thread (ensure `.allowMainThreadQueries()` is not configured).
- **Background Dispatch**: Run operations through `HikeRepository` utilizing a background `ExecutorService` and post updates back via main `Handler` loop to keep the UI smooth and responsive (eliminates ANR risk).

---

### Feature C: Observations (15% of coursework)

**Rule 1: Observation Data Model**
- **Required Fields**:
  - Title (what was observed)
  - Time (defaults to current time, formatted HH:mm, user can override via TimePickerDialog)
- **Optional Fields**:
  - Comments (detailed notes)

**Rule 2: Workflow**
1. User selects a hike from the list.
2. UI displays hike details + button to "Add Observation" and list of observations.
3. Form lets user configure Title, Time, and Comment.
4. Save observation via Repository wrapper using Room database.
5. Display list of observations for this hike (ordered by time ascending).
6. Edit / Delete individual observations with immediate updates.

---

### Feature D: Search & Filter (10% of coursework)

**Rule 1: Basic Search**
- User enters hike name in the top search field.
- Dynamic key listeners execute a live case-insensitive name search query using wildcards: `@Query("SELECT * FROM hikes WHERE LOWER(name) LIKE LOWER(:nameQuery)...")`
- Results update instantly in the list.

**Rule 2: Advanced Search**
- Multi-criteria filter screen allowing filters on:
  - Location (partial match)
  - Date range (from/to)
  - Length range (min/max km)
  - Difficulty (dropdown multi-single select)
- Dynamic queries built safely at runtime via Room's `@RawQuery` using `SimpleSQLiteQuery` with positional arguments (`?`) to prevent SQL Injection.

---

### Feature E: Authentication (Firebase Auth)

**Rule 1: Sign-In Flow**
- Email/password authentication via `FirebaseAuth` (`createUserWithEmailAndPassword`, `signInWithEmailAndPassword`).
- On app launch, `MainActivity`/`HikeListActivity` checks `FirebaseAuth.getInstance().getCurrentUser()`:
  - Signed out → redirect to `LoginActivity`.
  - Signed in → show hike list.
- `HikeListActivity` signs out on demand (menu / button) and clears the authenticated view.

**Rule 2: Offline Behavior**
- Auth is required to see the hike list, but hike data (Room) remains fully local-first and usable without a network connection after login is cached.

---

### Feature F: Cloud Sync (Firebase Realtime Database)

**Rule 1: Sync Model**
- **Single source of truth**: Room database remains authoritative for all reads/writes.
- **Sync direction**: local → cloud (best-effort). Every hike is tagged with `user_id` (the signed-in Firebase UID) and `is_synced` (0 = pending).
- After each local insert/update/delete, the repository queues the pending rows and pushes them to path `users/{uid}/hikes/{id}`.
- On successful push, `is_synced` is flipped to 1 locally.

**Rule 2: Repository Contract**
- `HikeRepository` is the **only** gateway to Room. Activities never touch DAOs directly.
- Firebase interactions live in a dedicated `FirebaseSyncHelper` invoked by the repository after local commits.
- All Firebase operations run off the main thread; callbacks marshalled to the UI thread via `Handler(mainLooper)`.

---

### Feature G: Additional Features (10% of coursework) — v3.0 Extended Pack

All six options are in scope for v3.0. Each sub-feature below lists its **rules**, **schema impact**, **screens**, **validation**, and **relative weight** within the 10% Feature G allocation.

#### G1 — Map View & GPS (Weight ~2.0 / 10)

**Rule 1: GPS Capture**
- Add `ACCESS_FINE_LOCATION` (and `ACCESS_COARSE_LOCATION`) to the manifest with a runtime permission request on Android 6.0+ (API 23+, minSdk 26 is safe).
- Use `FusedLocationProviderClient` from Google Play Services Location.
- In the Add/Edit Hike form, a **"Use my location"** button requests a single `getLastLocation()` fix and stores `latitude`/`longitude` as `Double` on the hike.

**Rule 2: Map View**
- Add `com.google.android.gms:play-services-maps` dependency and a `SupportMapFragment` in `HikeDetailActivity` (and/or a dedicated `HikeMapActivity`).
- The map shows the saved position with a custom marker (brand primary `#386A1F`) and camera zoom 14.
- If no coordinates are saved, the map fragment shows a "No location saved" empty state instead of a blank map.

**Rule 3: Validation & Fallbacks**
- Latitude must be within **-90.0 .. 90.0**, longitude within **-180.0 .. 180.0**; validate before insert.
- Maps API key stored in `AndroidManifest.xml` via a manifest placeholder / `local.properties`; document that a key is required for the map to render. Layout fallbacks (badge + coordinates text) must still work without the key.

**Schema impact**: `latitude Double`, `longitude Double` columns on `hikes`.
**Screens**: `activity_add_hike.xml` (location button + read-only lat/lng fields), `activity_hike_detail.xml` (embedded map card), optional `HikeMapActivity`.
**Rules weight**: map display 50%, GPS capture 30%, validation/fallback 20%.

#### G2 — Photo Capture & Storage (Weight ~2.0 / 10)

**Rule 1: Capture**
- Launch system camera via `MediaStore.ACTION_IMAGE_CAPTURE` intent from the Add/Edit Hike form.
- Expose a `FileProvider` (`<provider>` in manifest with `paths.xml` mapping `getExternalFilesDir(Pictures)`), pass the capture `content://` URI as `EXTRA_OUTPUT` so the full-resolution image is saved, not just the thumbnail.
- Declare `CAMERA` permission as optional (`tools:ignore="QueryAllPackagesPermission"` or runtime request); gracefully disable the button if no camera app is available (`resolveActivity` returns null).

**Rule 2: Storage**
- Store the image file in `getExternalFilesDir(Pictures)` (no storage permission needed for app-specific dirs).
- Persist the **URI string** in the `photo_uri` column. Store the stable `content://` URI (not a runtime path) so it survives permission-scoped reads.
- Deleting a hike deletes its image file via the repository (best-effort, swallow file-not-found).

**Rule 3: Display**
- `HikeDetailActivity` shows the photo in a card with `ImageView` + a "Retake / Replace" affordance.
- List item shows a 64dp thumbnail when `photo_uri` is present, otherwise a placeholder drawable.
- Use `FileProvider.getUriForFile` + `FLAG_GRANT_READ_URI_PERMISSION` when sharing/exporting.

**Schema impact**: `photo_uri String` column on `hikes`.
**Screens**: `activity_add_hike.xml` (capture button + preview), `activity_hike_detail.xml` (photo card), `item_hike.xml` (thumbnail).
**Rules weight**: capture 40%, storage/cleanup 30%, display 30%.

#### G3 — Duration Calculator (Weight ~1.5 / 10)

**Rule 1: Estimated Duration (auto)**
- Compute an estimate from length + difficulty using Naismith's rule base: **12 minutes per km** on flat ground, adjusted by difficulty multiplier:
  - Easy × 1.0
  - Moderate × 1.3
  - Hard × 1.6
  - Expert × 2.0
- Formula: `estimated_duration_min = round(length_km * 12 * multiplier)`, clamped to max **720 min** (12 h).
- The estimate is recomputed live whenever length or difficulty changes and shown as read-only text in the form ("Estimated: 2 h 24 min") while storing the integer minutes.

**Rule 2: Actual Duration (manual)**
- User may enter actual duration in minutes (or via a picker); optional.
- Detail screen shows both values and the delta ("Slower than estimated / On pace / Faster than estimated").

**Rule 3: Validation**
- Estimated value is derived, never user-editable → always valid.
- Actual duration must be **1..1440 minutes** when present.

**Schema impact**: `estimated_duration_min Integer`, `actual_duration_min Integer` on `hikes`.
**Screens**: `activity_add_hike.xml` (live estimate label), `activity_hike_detail.xml` (duration card).
**Rules weight**: estimate engine 40%, live recompute 30%, actual-duration capture 30%.

#### G4 — Weather Notes Template (Weight ~1.5 / 10)

**Rule 1: Structured Weather Capture**
- Add a weather section in the Add/Edit form with:
  - **Condition picker** (`Spinner`): Sunny, Partly Cloudy, Cloudy, Rain, Snow, Wind, Fog, Storm.
  - **Temperature**: optional numeric `EditText` with °C suffix (valid range **-60..60** °C).
  - **Wind**: optional `Spinner` (Calm, Light, Moderate, Strong, Gale).
  - **Notes**: optional multi-line free text, max 500 chars.
- Store the chosen condition string + notes in dedicated columns.

**Rule 2: Detail Rendering**
- Detail screen renders the weather as an icon + label row using the condition string to pick a drawable; "No weather recorded" empty state otherwise.

**Schema impact**: `weather_condition String`, `weather_notes String` on `hikes`.
**Screens**: `activity_add_hike.xml` (weather collapsible section), `activity_hike_detail.xml` (weather card).
**Rules weight**: template fields 40%, validation 30%, rendering 30%.

#### G5 — Export to PDF / Share (Weight ~1.5 / 10)

**Rule 1: PDF Generation**
- Generate a PDF report for a hike with `android.graphics.pdf.PdfDocument` (no extra dependency).
- Report layout (A4 portrait): app header band, hike name + location, detail table (date, length, difficulty, duration, weather, trail rating), description block, observations list, and GPS coordinates + thumbnail photo if present.
- Write to `getCacheDir()/reports/<hikeId>.pdf` and expose via `FileProvider` (same provider as G2).

**Rule 2: Share**
- `HikeDetailActivity` toolbar action "Export PDF" builds the report then fires `Intent.ACTION_SEND` with `EXTRA_STREAM` = `content://` URI and `FLAG_GRANT_READ_URI_PERMISSION`.
- System share sheet lets the user pick email, Drive, WhatsApp, etc.
- Long-running generation runs on a background thread; UI shows a progress indicator.

**Rule 3: Robustness**
- Guard against missing fields (empty description, no observations, no photo) — report must still render with placeholder text.
- Reuse the same `@xml/file_paths` used by G2 (add `cache-path` entry).

**Schema impact**: none (derived at export time).
**Screens**: `activity_hike_detail.xml` (export action), new `activity_pdf_preview.xml` optional preview.
**Rules weight**: generation 50%, share/URI granting 30%, robustness 20%.

#### G6 — Trail Condition Ratings (Weight ~1.5 / 10)

**Rule 1: Rating Input**
- 5-level star rating (Material `RatingBar`, `stepSize=1.0`) in the Add/Edit form: 1 = Very Poor, 2 = Poor, 3 = Fair, 4 = Good, 5 = Excellent.
- Store as `Integer` 1-5 in `trail_rating`; optional companion free-text `trail_notes`.

**Rule 2: Display**
- Detail screen renders stars + label ("4 / 5 — Good").
- List item shows compact stars next to difficulty badge.

**Rule 3: Validation**
- When present, rating must be an integer in **1..5** (RatingBar enforces this).

**Schema impact**: `trail_rating Integer`, `trail_notes String` on `hikes`.
**Screens**: `activity_add_hike.xml` (rating row), `activity_hike_detail.xml` (rating card), `item_hike.xml` (compact stars).
**Rules weight**: input 40%, display 40%, validation 20%.

---

## 3. SYSTEM ARCHITECTURE & DESIGN RULES

### Package Structure (v3)
```
com.example.m_hikeapp/
├── activity/              # Android Activities
│   ├── MainActivity.java          # auth gate / redirect
│   ├── LoginActivity.java         # Firebase email/password sign-in
│   ├── HikeListActivity.java      # list + search + logout
│   ├── HikeDetailActivity.java    # detail + map + photo + export
│   ├── HikeMapActivity.java       # G1 full-screen map
│   ├── AddHikeActivity.java       # create/edit + extras
│   ├── AddObservationActivity.java
│   └── SearchFilterActivity.java
├── database/              # Room Database definition & Migration
│   └── AppDatabase.java           # version 3, MIGRATION_2_3
├── dao/
│   ├── HikeDao.java
│   └── ObservationDao.java
├── model/
│   ├── Hike.java                  # v3 entity (all G columns)
│   └── Observation.java
├── repository/
│   └── HikeRepository.java        # only Room gateway (thread-safe)
├── sync/
│   └── FirebaseSyncHelper.java    # F: RTDB push/pull + dirty flags
├── export/
│   └── PdfReportBuilder.java      # G5: PdfDocument report
├── adapter/
│   ├── HikeAdapter.java
│   └── ObservationAdapter.java
├── util/
│   ├── ValidationResult.java
│   ├── ValidationUtils.java
│   ├── DurationCalculator.java    # G3: Naismith estimate
│   ├── WeatherContract.java       # G4: condition constants
│   └── GpsUtils.java              # G1: permission + fused provider
└── provider/
    └── FileProvider (manifest-only; shares @xml/file_paths)
```

### Architectural Invariants (v3)
- `HikeRepository` is the **only** gateway to Room. Activities never touch DAOs directly.
- Single-threaded `ExecutorService` in the repository; results posted to UI thread via `Handler(mainLooper)`.
- No `allowMainThreadQueries()`.
- Firebase sync is best-effort and must never block or corrupt the local Room write.
- All user-facing strings live in `strings.xml`.

---

## 4. UI/UX DESIGN RULES

### Design System: Material Design 3
- **Forest Green Theme Colors**:
  - Primary: `#386A1F`
  - Primary Container: `#B7F397`
  - Secondary: `#55624C`
  - Error: `#BA1A1A`
  - Outline: `#73796D`
- **Actions Styling**:
  - **Filter Button**: Outlined button tinted with primary green (`@color/md_primary`).
  - **Delete All Button**: Outlined button tinted with error red (`@color/md_error`).
  - **Export Button**: Filled tonal button (secondary) in detail screen.
  - **Primary CTA** ("Save Hike", "Sign In"): filled primary.
- **Touch Targets**: Minimum `48dp` for standard clicks and `56dp` minimum height for list items.

### Navigation Flow (v3)
```
LoginActivity ──(auth success)──▶ HikeListActivity ──▶ HikeDetailActivity
                                     │  ▲                     │
                                     │  └─ AddHikeActivity ────┘ (form + G extras)
                                     │  └─ SearchFilterActivity
                                     │  └─ HikeMapActivity (G1)
HikeDetailActivity ──▶ AddObservationActivity
HikeDetailActivity ──▶ (export PDF → system share sheet, G5)
```

### Screen Inventory
| Screen | Purpose | New in v3 |
|--------|---------|-----------|
| `activity_login` | Firebase sign-in | — |
| `activity_hike_list` | List, search, logout | thumbnail + rating on items |
| `activity_add_hike` | Create/edit form + extras | G1-G4, G6 inputs |
| `activity_hike_detail` | Detail + actions | map card, photo card, weather, rating, export |
| `activity_hike_map` | Full-screen map | G1 |
| `activity_add_observation` | Observation form | — |
| `activity_search_filter` | Advanced filters | — |

### Empty / Error / Loading States
- **Empty list**: centered illustration + "No hikes yet — add your first hike".
- **No location**: map card replaced with "Location not saved — tap 'Use my location'".
- **No photo**: placeholder drawable.
- **Export in progress**: indeterminate `ProgressBar` + disabled action.

---

## 5. CODE QUALITY RULES

- Follow standard Java naming patterns (PascalCase classes, camelCase methods/variables, UPPER_SNAKE_CASE constants).
- Keep logic out of Activities; delegate database execution to the `HikeRepository` background threads; delegate PDF/map/photo work to `util`/`export` helpers.
- Do not make hardcoded String references. Use `strings.xml` resource keys for validation messages, labels, hints, and dialog contents.
- Keep methods short and ensure classes adhere to single responsibilities.
- Document the Google Maps API key requirement and the `MIGRATION_2_3` SQL statements in code comments.

---

## 6. DEPENDENCIES & MANIFEST CHANGES (v3)

**New Gradle dependencies (add to `app/build.gradle.kts`):**
```kotlin
implementation("com.google.android.gms:play-services-maps:19.0.0")     // G1
implementation("com.google.android.gms:play-services-location:21.3.0") // G1
```

**Manifest additions:**
```xml
<!-- G1 -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.CAMERA" /> <!-- G2, optional -->
<!-- G1 maps key (from local.properties/build config) -->
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
<!-- G2/G5 -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

---

**Last Updated**: 2026-08-01 (v3.0 Extended Feature Pack)
**Prepared for**: M-Hike Native Android Implementation
