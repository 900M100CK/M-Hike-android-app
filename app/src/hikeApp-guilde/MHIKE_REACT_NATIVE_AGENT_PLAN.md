# M-Hike React Native App — Agent Briefing & Development Plan

**Version:** 3.0 (Room + Firebase Hybrid + Extended Feature Pack)
**Platform:** Cross-Platform iOS + Android via React Native
**Tech Stack:** React Native, Expo (managed workflow), TypeScript, React Navigation v6, expo-sqlite (v4 schema), Zustand, Firebase Auth, Firebase Realtime Database, @rnmapbox/maps (Mapbox), Open-Meteo API, expo-camera, expo-location, expo-print, expo-sharing, react-native-paper (Material Design 3)
**Status:** Coursework Implementation (COMP1786 Term 1)
**Note:** Full feature parity with the Android native version — all features A through G (all 6 G sub-features), same data schema, same business logic, same UI/UX design language.

---

## 1. PROJECT CONTEXT

### Overview
**M-Hike** React Native is the cross-platform implementation of the hiker management app.
Running on both iOS and Android from a single TypeScript codebase, it has **full feature parity**
with the Android Java native version:

- Plan and record detailed hike entries (name, location, date, difficulty, parking, length)
- Capture real-time observations during hikes (title, time, comments, photos, temperature, step count)
- Search and filter hike records locally with or without internet
- **Local-first** — all core features work completely offline after login is cached
- Sync data to Firebase Realtime Database
- Display live weather via Open-Meteo API
- Full G-feature pack: Maps, Photos, Duration, Weather, PDF Export, Ratings

### Target Users
- Outdoor hikers aged 18–65, mixed technical proficiency
- iOS (iPhone) and Android users — unified codebase
- Users hiking locally (UK/Vietnam focus, but generalizable)

### Business Goals
1. Full feature parity with Android native (all features A–G)
2. Cross-platform delivery — one codebase, two platforms
3. Local-first model — SQLite works completely offline
4. Intuitive UI matching Material Design 3 Forest Green theme
5. Shareable PDF hike reports

### Constraints & Requirements
- **Technology**: React Native + Expo managed workflow (TypeScript)
- **Persistence**: expo-sqlite — schema v4 matching Android Room
- **Cloud**: Firebase Auth (email/password) + Firebase Realtime Database
- **Maps**: @rnmapbox/maps (Mapbox) — NOT react-native-maps / Google Maps
- **Weather**: Open-Meteo API (free, no API key required) via fetch
- **Features**: A–G fully implemented (all 6 G sub-features)
- **Min requirements**: iOS 12+ / Android API 26+

---

## 2. TECH STACK & DEPENDENCIES

### package.json dependencies
```json
{
  "react-native": "^0.74.0",
  "expo": "^51.0.0",
  "typescript": "^5.x",
  "@react-navigation/native": "^6.x",
  "@react-navigation/native-stack": "^6.x",
  "expo-sqlite": "^14.x",
  "zustand": "^4.x",
  "@react-native-async-storage/async-storage": "^1.x",
  "@rnmapbox/maps": "^10.x",
  "expo-location": "^17.x",
  "expo-image-picker": "^15.x",
  "expo-file-system": "^17.x",
  "expo-sharing": "^12.x",
  "expo-print": "^13.x",
  "@react-native-firebase/app": "^20.x",
  "@react-native-firebase/auth": "^20.x",
  "@react-native-firebase/database": "^20.x",
  "react-native-ratings": "^8.x",
  "react-native-paper": "^5.x",
  "react-native-safe-area-context": "^4.x",
  "react-native-screens": "^3.x",
  "react-native-gesture-handler": "^2.x",
  "react-native-reanimated": "^3.x"
}
```

### Project Directory Structure
```
mhike-rn/
├── App.tsx                         # Entry point + auth gate
├── app.json                        # Expo config + Mapbox token
├── tsconfig.json                   # strict: true
├── src/
│   ├── screens/
│   │   ├── LoginScreen.tsx         # Feature E: Firebase email/password
│   │   ├── HikeListScreen.tsx      # Feature B: list + live search + weather banner
│   │   ├── HikeDetailScreen.tsx    # Feature B+C: detail + observations + map + export
│   │   ├── AddHikeScreen.tsx       # Feature A: create/edit + all G extras
│   │   ├── AddObservationScreen.tsx # Feature C: observation form + weather auto-fill
│   │   ├── HikeMapScreen.tsx       # G1: Mapbox full-screen map
│   │   └── SearchFilterScreen.tsx  # Feature D: advanced multi-criteria filter
│   ├── components/
│   │   ├── HikeCard.tsx            # FlatList item: photo, name, badges, stars
│   │   ├── ObservationCard.tsx     # Obs item: time, title, photo, temp, steps
│   │   ├── DifficultyBadge.tsx     # Color-coded chip: Easy/Moderate/Hard/Expert
│   │   ├── WeatherBanner.tsx       # Live weather strip on hike list
│   │   ├── StarRating.tsx          # Trail rating 1–5 stars (react-native-ratings)
│   │   ├── FormField.tsx           # TextInput + label + inline error text
│   │   ├── PhotoCapture.tsx        # Camera button + photo preview
│   │   └── EmptyState.tsx          # Illustration + message for empty lists
│   ├── database/
│   │   ├── db.ts                   # expo-sqlite init + migration chain (v1→v4)
│   │   ├── schema.ts               # CREATE TABLE / ALTER TABLE SQL strings
│   │   └── queries.ts              # All typed SQL query functions
│   ├── store/
│   │   ├── hikeStore.ts            # Zustand: hikes CRUD + firebase sync trigger
│   │   └── observationStore.ts     # Zustand: observations CRUD
│   ├── services/
│   │   ├── firebaseAuth.ts         # Firebase Auth wrapper
│   │   ├── firebaseSync.ts         # RTDB push/remove (best-effort)
│   │   ├── weatherService.ts       # Open-Meteo API + translation + fallback
│   │   └── pdfService.ts           # expo-print HTML template + expo-sharing
│   ├── types/
│   │   ├── hike.ts                 # Hike interface (21 fields)
│   │   └── observation.ts          # Observation interface (8 fields)
│   ├── utils/
│   │   ├── durationCalculator.ts   # G3: Naismith rule (matches Android logic exactly)
│   │   ├── validation.ts           # All form validation (name, date, length, etc.)
│   │   ├── imageUtils.ts           # G2: photo capture, copy to permanent dir
│   │   └── constants.ts            # Difficulty, weather condition, wind constants
│   ├── navigation/
│   │   ├── RootNavigator.tsx       # Auth-gated root stack
│   │   └── types.ts                # NavigatorParamList types
│   └── theme/
│       ├── colors.ts               # Forest Green M3 palette
│       ├── typography.ts           # Font scale
│       └── spacing.ts              # 8dp grid (4, 8, 12, 16, 24, 32, 48)
└── assets/
```

---

## 3. DATABASE SCHEMA (expo-sqlite v4 — identical to Android Room)

### Schema SQL (database/schema.ts)
```typescript
export const CREATE_HIKES_TABLE = `
  CREATE TABLE IF NOT EXISTS hikes (
    id                     INTEGER PRIMARY KEY AUTOINCREMENT,
    name                   TEXT NOT NULL,
    location               TEXT NOT NULL,
    date                   TEXT NOT NULL,
    parking_available      INTEGER NOT NULL DEFAULT 0,
    length_km              REAL NOT NULL,
    difficulty             TEXT NOT NULL,
    description            TEXT,
    custom_field_1         TEXT,
    custom_field_2         TEXT,
    user_id                TEXT,
    is_synced              INTEGER NOT NULL DEFAULT 0,
    latitude               REAL,
    longitude              REAL,
    photo_uri              TEXT,
    estimated_duration_min INTEGER NOT NULL DEFAULT 0,
    actual_duration_min    INTEGER NOT NULL DEFAULT 0,
    weather_condition      TEXT,
    weather_notes          TEXT,
    trail_rating           INTEGER,
    trail_notes            TEXT
  )
`;

export const CREATE_OBSERVATIONS_TABLE = `
  CREATE TABLE IF NOT EXISTS observations (
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    hike_id              INTEGER NOT NULL,
    title                TEXT NOT NULL,
    obs_time             TEXT NOT NULL,
    comment              TEXT,
    step_count           INTEGER,
    photo_uri            TEXT,
    temperature_celsius  REAL,
    is_synced            INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY(hike_id) REFERENCES hikes(id) ON DELETE CASCADE
  )
`;

// Migration v3 -> v4 (adds extended observation fields + sync flag)
export const MIGRATION_3_4 = [
  "ALTER TABLE observations ADD COLUMN step_count INTEGER",
  "ALTER TABLE observations ADD COLUMN photo_uri TEXT",
  "ALTER TABLE observations ADD COLUMN temperature_celsius REAL",
  "ALTER TABLE observations ADD COLUMN is_synced INTEGER NOT NULL DEFAULT 0",
];
```

### TypeScript Types (types/hike.ts)
```typescript
export type Difficulty = "Easy" | "Moderate" | "Hard" | "Expert";
export type WeatherCondition =
  | "Sunny" | "Partly Cloudy" | "Cloudy" | "Rain"
  | "Snow" | "Wind" | "Fog" | "Storm";
export type WindLevel = "Calm" | "Light" | "Moderate" | "Strong" | "Gale";

export interface Hike {
  id: number;
  name: string;                         // Required, 1–100 chars
  location: string;                     // Required
  date: string;                         // Required, YYYY-MM-DD
  parkingAvailable: boolean;            // Required
  lengthKm: number;                     // Required, 0.1–500
  difficulty: Difficulty;               // Required
  description?: string;                 // Optional, max 500 chars
  customField1?: string;                // Optional
  customField2?: string;                // Optional
  userId?: string;                      // Firebase UID
  isSynced: boolean;                    // dirty flag
  // G1
  latitude?: number;                    // -90 to 90
  longitude?: number;                   // -180 to 180
  // G2
  photoUri?: string;                    // permanent file:// URI
  // G3
  estimatedDurationMin: number;         // auto-computed, default 0
  actualDurationMin: number;            // user-entered, default 0
  // G4
  weatherCondition?: WeatherCondition;
  weatherNotes?: string;
  // G6
  trailRating?: number;                 // 1–5, null if not rated
  trailNotes?: string;
}

// types/observation.ts
export interface Observation {
  id: number;
  hikeId: number;
  title: string;                        // Required
  obsTime: string;                      // Required, HH:mm
  comment?: string;
  stepCount?: number;
  photoUri?: string;
  temperatureCelsius?: number;
  isSynced: boolean;                    // dirty flag
}
```

---

## 4. FEATURE SPECIFICATIONS

### Feature A: Hike Data Entry (10%)

**Required Fields** (show error if empty/invalid):
- Hike Name — TextInput, 1–100 chars
- Location — TextInput, 1–100 chars
- Date — DateTimePicker (defaults to today), stored YYYY-MM-DD, range 1900–2100
- Parking — radio-style Yes/No buttons (SegmentedButtons from react-native-paper), no default
- Length — numeric TextInput, 0.1–500 km
- Difficulty — Picker dropdown (Easy / Moderate / Hard / Expert)

**Optional Fields** (no error if empty):
- Description — multiline TextInput, max 500 chars
- Custom Field 1 & 2 — free-text in collapsible "Extras" section
- GPS (G1) — "Use My Location" button → expo-location
- Photo (G2) — "Take Photo" → expo-image-picker launchCameraAsync
- Duration (G3) — estimated auto-calculated; actual manually entered
- Weather (G4) — condition Picker + temperature TextInput + wind Picker + notes
- Trail Rating (G6) — StarRating (1–5) + trail notes TextInput

**Form UX Rules:**
- Screen: AddHikeScreen, full ScrollView form
- All inputs controlled (useState); onChangeText / onChange handlers
- Inline error via FormField component (red text below input)
- Save button: disabled until all required fields valid
- Live estimated duration recalculates via useEffect on [lengthKm, difficulty]
- Both Create mode (no hikeId param) and Edit mode (hikeId param passed via navigation)
- Success: goBack() + show success toast/snackbar

### Feature B: Data Persistence & Management (15%)

**Query Functions (database/queries.ts):**
```typescript
// Insert
export const insertHike = async (db, hike) => { /* INSERT INTO hikes ... */ };
export const insertObservation = async (db, obs) => { /* INSERT INTO observations ... */ };

// Update
export const updateHike = async (db, hike) => { /* UPDATE hikes SET ... WHERE id = ? */ };
export const updateObservation = async (db, obs) => { /* UPDATE observations SET ... WHERE id = ? */ };
export const markHikeSynced = async (db, id) => { /* UPDATE hikes SET is_synced=1 WHERE id=? */ };

// Delete
export const deleteHikeById = async (db, id) => { /* DELETE FROM hikes WHERE id=? */ };
export const deleteAllHikesByUser = async (db, uid) => { /* DELETE FROM hikes WHERE user_id=? */ };
export const deleteObservationById = async (db, id) => { /* DELETE FROM observations WHERE id=? */ };

// Read
export const getAllHikesByUser = async (db, uid): Promise<Hike[]>;
export const getHikeById = async (db, id): Promise<Hike | null>;
export const getUnsyncedHikesByUser = async (db, uid): Promise<Hike[]>;
export const getObservationsForHike = async (db, hikeId): Promise<Observation[]>;

// Search & Filter
export const searchHikesByName = async (db, query: string): Promise<Hike[]>;
  // SQL: WHERE LOWER(name) LIKE LOWER('%query%') ORDER BY date DESC

export const filterHikes = async (db, filters: FilterParams): Promise<Hike[]>;
  // Dynamic SQL: WHERE 1=1 + optional AND clauses; positional ? binding (safe from injection)
```

**FilterParams interface:**
```typescript
interface FilterParams {
  name?: string;
  location?: string;
  dateFrom?: string;     // YYYY-MM-DD
  dateTo?: string;
  minKm?: number;
  maxKm?: number;
  difficulty?: Difficulty;
}
```

**List Display Rules:**
- HikeListScreen: FlatList with keyExtractor={item => item.id.toString()}
- HikeCard: photo thumbnail (64px rounded), name, location, date, DifficultyBadge, parking icon, StarRating compact, length km
- EmptyState component: illustration + "No hikes yet — tap + to add your first hike"
- Delete: Alert.alert confirmation before deleting

**Threading:** expo-sqlite async API — all DB calls are async/await, never blocking JS thread

### Feature C: Observations (15%)

**AddObservationScreen:**
- Required: title (TextInput), obsTime (DateTimePicker defaults to now HH:mm)
- Optional: comment (multiline), stepCount (numeric TextInput), photo (PhotoCapture), temperatureCelsius (TextInput — auto-filled on mount)
- Auto weather fill: call weatherService.fetchDetailedWeather() on mount → pre-fill temperatureCelsius
- Falls back to Hanoi (21.0285, 105.8542) if no GPS permission
- Save → observationStore.addObservation() → navigation.goBack()

**Observation List in HikeDetailScreen:**
- FlatList with scrollEnabled={false} (outer ScrollView handles scroll)
- Ordered by obsTime ascending
- ObservationCard: time, title, comment, step count badge, temperature, photo thumbnail
- Edit/Delete via long-press actions or icon buttons

**Cascade Delete:** SQLite FK ON DELETE CASCADE → deleting a hike auto-deletes all its observations

### Feature D: Search & Filter (10%)

**Basic Search (HikeListScreen):**
- TextInput search bar with onChangeText handler (live)
- Empty → loadHikes(); non-empty → searchHikesByName(query)
- LOWER(name) LIKE LOWER('%query%') — case-insensitive partial match

**Advanced Filter (SearchFilterScreen):**
- Name (partial match), Location (partial match), Date From/To (DateTimePicker), Length Min/Max km, Difficulty (Picker)
- "Apply Filters" → filterHikes(filters) → results FlatList
- "Reset" button → clear all state + full list

### Feature E: Firebase Authentication (5%)

```typescript
// services/firebaseAuth.ts
import auth from "@react-native-firebase/auth";

export const signIn = (email: string, password: string) =>
  auth().signInWithEmailAndPassword(email, password);

export const signUp = (email: string, password: string) =>
  auth().createUserWithEmailAndPassword(email, password);

export const signOut = () => auth().signOut();
export const getCurrentUser = () => auth().currentUser;
export const onAuthStateChanged = (cb) => auth().onAuthStateChanged(cb);
```

**Auth Gate (App.tsx):**
```typescript
const [user, setUser] = useState<FirebaseAuthTypes.User | null>(null);
const [loading, setLoading] = useState(true);
useEffect(() => {
  const unsub = auth().onAuthStateChanged(u => { setUser(u); setLoading(false); });
  return unsub;
}, []);
// loading → ActivityIndicator; user null → LoginScreen; user set → HikeListScreen
```

**Sign-Out:** Header menu "Logout" → signOut() → auth state listener fires → LoginScreen

### Feature F: Cloud Sync (Firebase Realtime Database) (5%)

```typescript
// services/firebaseSync.ts
import database from "@react-native-firebase/database";

export const pushHike = async (uid: string, hike: Hike) =>
  database().ref(`users/${uid}/hikes/${hike.id}`).set(hike);

export const removeHike = async (uid: string, hikeId: number) =>
  database().ref(`users/${uid}/hikes/${hikeId}`).remove();

export const pushObservation = async (uid: string, hikeId: number, obs: Observation) =>
  database().ref(`users/${uid}/hikes/${hikeId}/observations/${obs.id}`).set(obs);

export const removeObservation = async (uid: string, hikeId: number, obsId: number) =>
  database().ref(`users/${uid}/hikes/${hikeId}/observations/${obsId}`).remove();

export const removeAllHikes = async (uid: string) =>
  database().ref(`users/${uid}/hikes`).remove();
```

- **Direction:** Local → Cloud (best-effort push; no pull)
- **Hike Path:** users/{uid}/hikes/{hikeId}
- **Observation Path:** users/{uid}/hikes/{hikeId}/observations/{obsId}
- **After every local write:** call pushHike() or pushObservation() in store; on success: markSynced(db, id)
- **On failure:** entry stays with isSynced=false; retry on next load call
- **Offline:** SQLite is always source of truth; Firebase failure never crashes app

### Feature G1: Map View & GPS

```typescript
// screens/HikeMapScreen.tsx
import MapboxGL from "@rnmapbox/maps";
MapboxGL.setAccessToken(MAPBOX_ACCESS_TOKEN); // from app.json extra

// Full-screen MapboxGL.MapView
// Custom green marker (color "#386A1F") at hike.latitude, hike.longitude
// Camera: zoomLevel=14, centerCoordinate=[longitude, latitude]
// Animate camera on map ready
// Guard: if !hike.latitude → Alert.alert("No location saved") + navigation.goBack()
```

**GPS Capture:**
```typescript
// utils/imageUtils.ts — GPS section
import * as Location from "expo-location";

export const captureCurrentLocation = async () => {
  const { status } = await Location.requestForegroundPermissionsAsync();
  if (status !== "granted") return null;
  const loc = await Location.getCurrentPositionAsync({
    accuracy: Location.Accuracy.Balanced,
  });
  return { latitude: loc.coords.latitude, longitude: loc.coords.longitude };
};
// Used in AddHikeScreen "Use My Location" button
// Validate: lat -90 to 90, lon -180 to 180
```

### Feature G2: Photo Capture & Storage

```typescript
// components/PhotoCapture.tsx
import * as ImagePicker from "expo-image-picker";
import * as FileSystem from "expo-file-system";

export const takePhoto = async (): Promise<string | null> => {
  const { status } = await ImagePicker.requestCameraPermissionsAsync();
  if (status !== "granted") return null;
  const result = await ImagePicker.launchCameraAsync({ quality: 0.8 });
  if (result.canceled) return null;
  // Copy to permanent storage (temp URIs expire on iOS)
  const dir = FileSystem.documentDirectory + "photos/";
  await FileSystem.makeDirectoryAsync(dir, { intermediates: true });
  const dest = dir + "photo_" + Date.now() + ".jpg";
  await FileSystem.copyAsync({ from: result.assets[0].uri, to: dest });
  return dest; // permanent file:// URI stored in photo_uri column
};
```

- Both Hike and Observation have photoUri field
- Display with `<Image source={{ uri: photoUri }}>`; fallback placeholder icon
- Delete photo file on hike/observation delete (FileSystem.deleteAsync, ignore errors)

### Feature G3: Duration Calculator

```typescript
// utils/durationCalculator.ts
const MINUTES_PER_KM = 12;
const MAX_MINUTES = 720; // 12h cap — matches Android DurationCalculator

const MULTIPLIERS: Record<string, number> = {
  Easy: 1.0, Moderate: 1.3, Hard: 1.6, Expert: 2.0,
};

export const estimateMinutes = (lengthKm: number, difficulty: string): number => {
  if (lengthKm <= 0) return 0;
  const raw = lengthKm * MINUTES_PER_KM * (MULTIPLIERS[difficulty] ?? 1.0);
  return Math.min(Math.round(raw), MAX_MINUTES);
};

export const formatMinutes = (minutes: number): string => {
  if (minutes <= 0) return "0m";
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  if (h === 0) return `${m}m`;
  if (m === 0) return `${h}h`;
  return `${h}h ${m}m`;
};

export const getDurationDelta = (est: number, actual: number): string => {
  if (actual <= 0) return "";
  const diff = actual - est;
  if (Math.abs(diff) < 10) return "On pace";
  return diff > 0 ? "Slower than estimated" : "Faster than estimated";
};
```

Live recalculation in AddHikeScreen:
```typescript
useEffect(() => {
  const est = estimateMinutes(parseFloat(lengthKm) || 0, difficulty);
  setEstimatedDuration(est);
  // Auto-update hike form state: estimatedDurationMin = est
}, [lengthKm, difficulty]);
```

### Feature G4: Weather Notes + Live Weather

**Form section in AddHikeScreen:**
- weatherCondition: Picker (Sunny / Partly Cloudy / Cloudy / Rain / Snow / Wind / Fog / Storm)
- temperature: TextInput numeric, –60 to 60 °C
- wind: Picker (Calm / Light / Moderate / Strong / Gale)
- weatherNotes: multiline TextInput, max 500 chars

**weatherService.ts:**
```typescript
const FALLBACK = { lat: 21.0285, lon: 105.8542 }; // Hanoi

export const fetchCurrentWeather = async (lat: number, lon: number): Promise<string> => {
  try {
    const url = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current_weather=true`;
    const res = await fetch(url);
    const json = await res.json();
    const w = json.current_weather;
    return `${w.temperature.toFixed(1)}°C • ${translateCode(w.weathercode)}`;
  } catch {
    return "Unable to fetch weather";
  }
};

export const fetchDetailedWeather = async (lat: number, lon: number) => {
  const url = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current_weather=true`;
  const res = await fetch(url);
  const json = await res.json();
  return { temperature: json.current_weather.temperature as number };
};

const translateCode = (code: number): string => {
  if (code === 0)              return "☀️ Clear sky / Sunny";
  if ([1].includes(code))      return "🌤️ Mainly clear";
  if ([2].includes(code))      return "⛅ Partly cloudy";
  if ([3].includes(code))      return "☁️ Overcast / Cloudy";
  if ([45,48].includes(code))  return "🌫️ Foggy";
  if ([51,53,55].includes(code)) return "🌦️ Drizzle";
  if ([61,63,65].includes(code)) return "🌧️ Rain";
  if ([71,73,75].includes(code)) return "❄️ Snow";
  if ([80,81,82].includes(code)) return "🌧️ Showers";
  if ([95,96,99].includes(code)) return "🌩️ Thunderstorm";
  return "🌬️ Breezy";
};
```

**WeatherBanner component in HikeListScreen:**
- Fetches on useFocusEffect (every time screen is focused)
- Uses expo-location to get current coords; falls back to Hanoi
- Displays: "27.3°C • ☀️ Clear sky / Sunny"

**AddObservationScreen:** calls fetchDetailedWeather on mount → pre-fills temperatureCelsius

### Feature G5: Export PDF & Share

```typescript
// services/pdfService.ts
import * as Print from "expo-print";
import * as Sharing from "expo-sharing";

export const generateAndShareHikePdf = async (
  hike: Hike,
  observations: Observation[]
): Promise<void> => {
  const html = buildPdfHtml(hike, observations);
  const { uri } = await Print.printToFileAsync({ html, base64: false });
  await Sharing.shareAsync(uri, {
    mimeType: "application/pdf",
    dialogTitle: "Share Hike Report",
  });
};

const buildPdfHtml = (hike: Hike, obs: Observation[]): string => `
<html><body style="font-family:sans-serif;margin:40px">
  <div style="background:#386A1F;color:#fff;padding:20px;border-radius:8px">
    <h1 style="margin:0">M-Hike Report</h1>
  </div>
  <h2>${hike.name}</h2><p>${hike.location}</p>
  <table style="width:100%;border-collapse:collapse">
    <tr><td>Date</td><td>${hike.date}</td></tr>
    <tr><td>Length</td><td>${hike.lengthKm} km</td></tr>
    <tr><td>Difficulty</td><td>${hike.difficulty}</td></tr>
    <tr><td>Parking</td><td>${hike.parkingAvailable ? "Yes" : "No"}</td></tr>
    <tr><td>Duration (est)</td><td>${formatMinutes(hike.estimatedDurationMin)}</td></tr>
    <tr><td>Duration (actual)</td><td>${hike.actualDurationMin > 0 ? formatMinutes(hike.actualDurationMin) : "N/A"}</td></tr>
    <tr><td>Weather</td><td>${hike.weatherCondition ?? "Not recorded"}</td></tr>
    <tr><td>Trail Rating</td><td>${hike.trailRating ? hike.trailRating + "/5" : "Not rated"}</td></tr>
    <tr><td>GPS</td><td>${hike.latitude ? hike.latitude + ", " + hike.longitude : "Not recorded"}</td></tr>
  </table>
  ${hike.description ? `<p>${hike.description}</p>` : ""}
  <h3>Observations (${obs.length})</h3>
  ${obs.map(o => `<div><b>${o.obsTime} — ${o.title}</b><p>${o.comment ?? ""}</p>${o.temperatureCelsius ? `<p>${o.temperatureCelsius}°C</p>` : ""}</div>`).join("")}
</body></html>`;
```

- "Export PDF" button in HikeDetailScreen (header action)
- Show ActivityIndicator while generating
- On complete: system share sheet (email, Drive, WhatsApp, etc.)

### Feature G6: Trail Condition Ratings

```typescript
// components/StarRating.tsx
import { Rating } from "react-native-ratings";

const LABELS = ["Very Poor", "Poor", "Fair", "Good", "Excellent"];

export const StarRating = ({ value, onChange, readonly = false }) => (
  <>
    <Rating
      type="star"
      ratingCount={5}
      imageSize={32}
      startingValue={value ?? 0}
      onFinishRating={onChange}
      readonly={readonly}
      ratingColor="#386A1F"
      tintColor="#FDFDF5"
    />
    {value ? <Text>{LABELS[value - 1]}</Text> : null}
  </>
);
```

- AddHikeScreen: StarRating (editable) + trailNotes TextInput
- HikeDetailScreen: StarRating (readonly) + label + notes
- HikeCard: compact star display next to difficulty badge

---

## 5. UI/UX DESIGN RULES

### Material Design 3 — Forest Green Theme

**Colors (theme/colors.ts):**
```typescript
export const colors = {
  primary:            "#386A1F",   // Forest Green
  onPrimary:          "#FFFFFF",
  primaryContainer:   "#B7F397",   // Light green
  onPrimaryContainer: "#042100",
  secondary:          "#55624C",
  onSecondary:        "#FFFFFF",
  secondaryContainer: "#D9E8CB",
  background:         "#FDFDF5",
  surface:            "#FDFDF5",
  error:              "#BA1A1A",
  onError:            "#FFFFFF",
  outline:            "#73796D",
  onBackground:       "#1C1B1F",
};
```

**DifficultyBadge Colors:**
- Easy:     bg "#C8E6C9", text "#1B5E20"
- Moderate: bg "#FFF9C4", text "#F57F17"
- Hard:     bg "#FFE0B2", text "#E65100"
- Expert:   bg "#FFCDD2", text "#B71C1C"

**Typography (theme/typography.ts):**
```typescript
export const typography = {
  headlineLarge:  { fontSize: 32, fontWeight: "700", lineHeight: 40 },
  titleLarge:     { fontSize: 22, fontWeight: "600", lineHeight: 28 },
  titleMedium:    { fontSize: 16, fontWeight: "600", lineHeight: 24 },
  bodyLarge:      { fontSize: 16, fontWeight: "400", lineHeight: 24 },
  bodyMedium:     { fontSize: 14, fontWeight: "400", lineHeight: 20 },
  labelSmall:     { fontSize: 12, fontWeight: "500", lineHeight: 16 },
};
```

**Spacing:** 8dp grid: 4, 8, 12, 16, 24, 32, 48

**Touch Targets:** minHeight: 48 (iOS) / 56 (Android) for all interactive elements

### Screen Inventory

| Screen | Component | Navigation |
|---|---|---|
| Login | LoginScreen | Root stack (unauthenticated) |
| Hike List | HikeListScreen | Root stack (authenticated, initial) |
| Hike Detail | HikeDetailScreen | Pushed from HikeListScreen |
| Add/Edit Hike | AddHikeScreen | Pushed (FAB or Edit button) |
| Add/Edit Obs | AddObservationScreen | Pushed from HikeDetailScreen |
| Map View | HikeMapScreen | Pushed from HikeDetailScreen (G1) |
| Search Filter | SearchFilterScreen | Pushed from HikeListScreen |

### Key Layout Patterns

**HikeListScreen:**
- Header: "M-Hike" title (left) + logout icon (right)
- WeatherBanner below header
- SearchBar (live search)
- Row: Filter button (outlined) + Delete All button (outlined error)
- FlatList of HikeCard
- EmptyState if empty
- FAB bottom-right: "+" → AddHikeScreen

**HikeCard:**
- Card with shadow/elevation
- Left: 64×64px rounded photo (Image or placeholder icon)
- Right: name (titleLarge), location + date (bodyMedium muted)
- Bottom row: DifficultyBadge + parking icon + StarRating compact + length km text
- Trailing: delete icon button
- Touchable: navigate to HikeDetailScreen

**HikeDetailScreen:**
- ScrollView with sections:
  1. Info card (name, location, date, parking, length, difficulty)
  2. "View Map" (outlined) + "Edit Hike" (outlined) buttons
  3. Photo card (if photoUri)
  4. Duration card (estimated + actual + delta)
  5. Weather card (condition + notes)
  6. Trail Rating card (StarRating readonly + label + notes)
  7. Observations header + "Add Observation" button
  8. FlatList observations (scrollEnabled=false)
  9. EmptyState if no observations

**AddHikeScreen:**
- ScrollView form:
  - Required section: name, location, date picker, parking radio, length, difficulty
  - Optional: description, custom fields (collapsible)
  - G1: "Use My Location" button + lat/lng read-only text
  - G2: PhotoCapture component (button + preview)
  - G3: estimated duration label (live) + actual duration input
  - G4: condition picker, temperature, wind picker, notes
  - G6: StarRating (editable) + trail notes
  - Footer: "Save Hike" (filled primary) / "Update Hike" (edit mode) + Cancel

### Empty / Error / Loading States
- Empty hike list: EmptyState component — illustration + "No hikes yet — tap + to add your first hike"
- No GPS for map: Alert.alert + goBack()
- No photo: placeholder icon (camera outline)
- PDF generating: ActivityIndicator + disabled export button
- Weather unavailable: "Unable to fetch weather" banner text
- Invalid hikeId from navigation: Alert + goBack()

### Accessibility
- All interactive elements: accessibilityLabel + accessibilityRole
- Touch targets: minHeight 48 (iOS) / 56 (Android)
- Color never sole status indicator — icon + text alongside colored badges
- Text contrast ≥ 4.5:1
- SafeAreaView wrapping on all screens (react-native-safe-area-context)
- KeyboardAvoidingView on all form screens (behavior "padding" on iOS)

---

## 6. STATE MANAGEMENT (Zustand)

```typescript
// store/hikeStore.ts
import { create } from "zustand";

interface HikeStore {
  hikes: Hike[];
  loading: boolean;
  error: string | null;
  loadHikes: (userId: string) => Promise<void>;
  addHike: (hike: Omit<Hike, "id">) => Promise<number>;
  updateHike: (hike: Hike) => Promise<void>;
  deleteHike: (id: number) => Promise<void>;
  deleteAllHikes: (userId: string) => Promise<void>;
}

export const useHikeStore = create<HikeStore>((set, get) => ({
  hikes: [],
  loading: false,
  error: null,

  loadHikes: async (userId) => {
    set({ loading: true, error: null });
    try {
      const db = await getDatabase();
      const hikes = await getAllHikesByUser(db, userId);
      // Retry unsynced hikes
      const unsynced = hikes.filter(h => !h.isSynced);
      unsynced.forEach(h =>
        pushHike(userId, h).then(() => markHikeSynced(db, h.id)).catch(() => {})
      );
      set({ hikes, loading: false });
    } catch (e: any) {
      set({ error: e.message, loading: false });
    }
  },

  addHike: async (hike) => {
    const db = await getDatabase();
    const userId = getCurrentUser()?.uid ?? null;
    const saved = { ...hike, userId, isSynced: false };
    const id = await insertHike(db, saved);
    const withId = { ...saved, id };
    if (userId) {
      pushHike(userId, withId)
        .then(() => markHikeSynced(db, id))
        .catch(() => {});
    }
    set(s => ({ hikes: [withId, ...s.hikes] }));
    return id;
  },

  // updateHike, deleteHike, deleteAllHikes follow same pattern
}));
```

---

## 7. CODE QUALITY RULES

- **TypeScript strict mode** — no `any` types; proper interfaces everywhere
- **Naming:** PascalCase components, camelCase functions/hooks, UPPER_SNAKE_CASE constants
- **Architecture:** Screens = UI + navigation only; business logic in services/ + utils/; DB only in database/queries.ts
- **Comments:** Explain WHY; TSDoc for all exported functions; tag G-features: `// G1:`, `// G2:`
- **Error handling:** all async wrapped in try-catch; user-friendly Alert messages; never crash on Firebase failure
- **No inline styles** — use StyleSheet.create or theme constants
- **SafeAreaView + KeyboardAvoidingView** — on all screens (iOS notch/home indicator handling)

---

## 8. TESTING

### Unit Tests (Jest)
```typescript
// durationCalculator.test.ts
test("moderate 10km = 156 min", () => expect(estimateMinutes(10, "Moderate")).toBe(156));
test("expert 30km capped at 720", () => expect(estimateMinutes(30, "Expert")).toBe(720));
test("format 156 min = 2h 36m", () => expect(formatMinutes(156)).toBe("2h 36m"));
```

### Manual Testing Checklist
- [ ] Login valid credentials → hike list
- [ ] Login wrong password → error message
- [ ] Add hike all required fields → appears in list
- [ ] Add hike empty required field → inline error shown
- [ ] Edit hike → changes persist
- [ ] Delete hike → removed + observations cascade deleted
- [ ] Delete All → empty state shown
- [ ] Add observation → visible in hike detail
- [ ] Edit/delete observation works
- [ ] Basic search (partial match, case-insensitive)
- [ ] Advanced filter (location + date range + difficulty)
- [ ] "Use My Location" → GPS saved
- [ ] View Map → Mapbox marker at hike coords
- [ ] Take photo → thumbnail in list + detail
- [ ] Duration live recalculates on length/difficulty change
- [ ] Export PDF → share sheet + readable PDF
- [ ] Weather banner shows live data
- [ ] Observation temperature auto-filled
- [ ] Kill + restart → SQLite data intact
- [ ] Offline: CRUD works without network
- [ ] Logout → auth screen; re-login shows same data
- [ ] iOS: content not hidden behind notch/home indicator
- [ ] Android: keyboard doesn't cover form inputs

---

## 9. COMMON PITFALLS TO AVOID

1. **Async SQLite** — always use async expo-sqlite API; never await inside render
2. **Photo URI expiry** — copy to FileSystem.documentDirectory before storing; temp URIs expire on iOS
3. **Mapbox token** — set in app.json under `expo.extra.mapboxToken`; won't render without it
4. **FlatList inside ScrollView** — set `scrollEnabled={false}` on inner FlatList for observations
5. **Firebase auth state race** — use onAuthStateChanged listener; never read currentUser synchronously at startup
6. **Weather fallback** — always default to Hanoi (21.0285, 105.8542) when expo-location denied
7. **Duration not recalculating** — useEffect deps must include BOTH lengthKm AND difficulty state
8. **Firebase offline** — SQLite is source of truth; Firebase error must never crash (try-catch + isSynced retry)
9. **KeyExtractor on FlatList** — use `item => item.id.toString()` for numeric IDs
10. **SafeAreaView** — wrap all screens with SafeAreaView from react-native-safe-area-context

---

## 10. DEVELOPMENT WORKFLOW

### Phase 1: Foundation (Week 1–2)
- [ ] Expo project init (TypeScript template)
- [ ] expo-sqlite: db.ts init + schema.ts + migration chain v1→v4
- [ ] TypeScript types: Hike + Observation interfaces + FilterParams
- [ ] Zustand stores scaffold (hikeStore, observationStore)
- [ ] theme/: colors.ts, typography.ts, spacing.ts

### Phase 2: Features A & B (Week 3–4)
- [ ] AddHikeScreen: form + validation (create mode)
- [ ] HikeListScreen: FlatList + HikeCard + FAB + EmptyState
- [ ] HikeDetailScreen: info card + placeholder sections
- [ ] Edit hike (hikeId param) + delete flow

### Phase 3: Features C & D (Week 5–6)
- [ ] AddObservationScreen: form + time picker
- [ ] ObservationCard + FlatList in HikeDetailScreen
- [ ] Basic search in HikeListScreen
- [ ] SearchFilterScreen: advanced filter

### Phase 4: Features E & F (Week 6–7)
- [ ] LoginScreen: email/password Firebase Auth
- [ ] Auth gate in App.tsx (onAuthStateChanged)
- [ ] firebaseSync.ts: pushHike, removeHike, removeAllHikes
- [ ] User-scoped queries; retry unsynced on load

### Phase 5: Feature G Pack (Week 7–8)
- [ ] G1: HikeMapScreen (Mapbox) + GPS capture (expo-location)
- [ ] G2: PhotoCapture component (expo-image-picker) on Hike + Observation
- [ ] G3: durationCalculator.ts + live useEffect in AddHikeScreen
- [ ] G4: weatherService.ts (Open-Meteo) + WeatherBanner + form section
- [ ] G5: pdfService.ts (expo-print + expo-sharing)
- [ ] G6: StarRating component (react-native-ratings)

### Phase 6: Polish & Demo (Week 8)
- [ ] Forest Green theme applied consistently
- [ ] Accessibility audit (labels, touch targets, contrast)
- [ ] Cross-platform testing (iOS + Android)
- [ ] 15-minute demo video
- [ ] Comprehensive report

---

## 11. AI AGENT INSTRUCTIONS

When generating code or documentation from this brief:

1. **React Native TypeScript ONLY** — no Java, no Kotlin, no Swift
2. **expo-sqlite async API** — no synchronous DB calls; always async/await
3. **Zustand** — single source of truth; no Redux, no plain Context for data
4. **@react-native-firebase** — use RN Firebase SDK; NOT the web Firebase SDK
5. **@rnmapbox/maps** — Mapbox ONLY; NOT react-native-maps / Google Maps
6. **Open-Meteo** (free, no API key) — NOT OpenWeatherMap or any paid service
7. **expo-print + expo-sharing** — for PDF; no third-party PDF library
8. **expo-image-picker** — for camera; copy to FileSystem.documentDirectory for persistence
9. **Forest Green M3** — exact hex #386A1F; no generic colors
10. **G-feature tags** — comment `// G1:`, `// G2:` etc. in code
11. **Same schema as Android Room** — 21 hike fields + 8 observation fields; same column names
12. **Naismith formula** — Easy x1.0, Moderate x1.3, Hard x1.6, Expert x2.0; base 12 min/km; cap 720 min
13. **TypeScript strict** — no `any`; proper interfaces for all data shapes
14. **Complete implementations** — real working code, not pseudo-code

---

## 12. DELIVERABLES CHECKLIST

### Code
- [ ] Expo React Native project (TypeScript)
- [ ] package.json with all dependencies
- [ ] expo-sqlite schema v4 + migration chain
- [ ] All screens + components
- [ ] Firebase Auth + RTDB integration
- [ ] Mapbox Maps integration
- [ ] Open-Meteo weather integration
- [ ] PDF export (expo-print + expo-sharing)
- [ ] google-services.json / GoogleService-Info.plist excluded from git (.gitignore)
- [ ] README.md with setup instructions (Mapbox token, Firebase config)

### Testing
- [ ] Manual testing checklist completed (Section 8)
- [ ] Tested on Android device/emulator (API 26+)
- [ ] Tested on iOS device/simulator (iOS 12+)

### Demo Video (15 min)
- [ ] Project structure + architecture walkthrough
- [ ] Features A–D (CRUD, validation, observations, search)
- [ ] Feature E login/logout flow
- [ ] Feature F Firebase sync
- [ ] Feature G pack (map, photo, duration, weather, PDF, rating)
- [ ] Offline persistence demo
- [ ] Code walkthrough: db.ts, hikeStore.ts, weatherService.ts, pdfService.ts, durationCalculator.ts
- [ ] Design decisions explained (why Zustand, why expo-sqlite, why Mapbox, why Open-Meteo)

### Report
- Section 1 (2%): Feature checklist with iOS + Android screenshots
- Section 2 (2%): Annotated screenshots of all 7 screens (both platforms)
- Section 3 (4%): Reflection 350 words (cross-platform challenges, trade-offs)
- Section 4 (8%): Evaluation 700–1000 words (HCI, security, screen sizes, deployment, cross-platform vs native)
- Section 5 (2%): Code listings (db.ts, hikeStore.ts, weatherService.ts)

---

## 13. REFERENCES & RESOURCES

- Expo Docs: https://docs.expo.dev/
- expo-sqlite: https://docs.expo.dev/versions/latest/sdk/sqlite/
- expo-location: https://docs.expo.dev/versions/latest/sdk/location/
- expo-image-picker: https://docs.expo.dev/versions/latest/sdk/image-picker/
- expo-print: https://docs.expo.dev/versions/latest/sdk/print/
- expo-sharing: https://docs.expo.dev/versions/latest/sdk/sharing/
- @rnmapbox/maps: https://github.com/rnmapbox/maps
- @react-native-firebase: https://rnfirebase.io/
- Open-Meteo API: https://open-meteo.com/en/docs
- Zustand: https://github.com/pmndrs/zustand
- React Navigation: https://reactnavigation.org/
- Material Design 3: https://m3.material.io/
- react-native-ratings: https://github.com/Monte9/react-native-ratings
- react-native-paper: https://callstack.github.io/react-native-paper/

---

**Document Version**: 2.0
**Last Updated**: 2026-08-05 (Rewritten — React Native kept; full feature parity with Android A–G)
**Platform**: Cross-platform React Native + Expo
**Features**: A, B, C, D, E, F, G1, G2, G3, G4, G5, G6 — complete
**Schema**: v4 — identical structure to Android Room database
**UI/UX**: Material Design 3 Forest Green — same as Android native version
