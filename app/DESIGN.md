---
name: M-Hike Design System — Forest Green M3 (Android)
version: "3.0"
description: Material Design 3 frontend specification for the native Android M-Hike app. Replaces the v1.0 "Alpine Modern" web-oriented system with an M3 token set, ViewBinding component contract, and screen-by-screen layout spec covering the v3.0 extended feature pack (map/GPS, photos, duration, weather, PDF export, trail ratings).
platform: Android (Java, XML, Material Components)
theme: Material 3 Forest Green (light)
---

## Overview

The v3.0 M-Hike frontend is a **native Android UI** built on Material Design 3 (Material Components for Android, `com.google.android.material`). It replaces the v1.0 web-style "Alpine Modern" system in favour of:

- A **Forest Green M3 token set** (aligned with the Room baseline used since v2.0).
- **ViewBinding** as the only view lookup mechanism.
- **Screen-by-screen XML contracts** for every activity, including the v3.0 additions (map, photo, weather, rating, export).
- Explicit **empty / error / loading states** for every async and data-backed screen.

### Active Baseline Parameters
- **TOKEN_BASIS**: M3 ColorScheme light (`Forest Green` seeded).
- **VIEW_LOOKUP**: ViewBinding only — no `findViewById`, no `ButterKnife`, no Kotlin synthetic imports.
- **INPUT_PREFERENCE**: Native Android controls (EditText, DatePickerDialog, TimePickerDialog, RadioGroup, Spinner, RatingBar, NumberPicker) before custom widgets.
- **TOUCH_TARGET_MIN**: 48dp standard, 56dp for list rows.
- **TEXT_SOURCES**: All user-visible strings from `strings.xml`.

---

## Design Tokens

### Colors (M3 Forest Green Light)

| Token | Hex | Usage |
|-------|-----|-------|
| `md_primary` | `#386A1F` | Primary actions, active states, FAB |
| `md_on_primary` | `#FFFFFF` | Text/icons on primary |
| `md_primary_container` | `#B7F397` | Chips, selected filter chips, soft badges |
| `md_on_primary_container` | `#0A2000` | Text on primary container |
| `md_secondary` | `#55624C` | Secondary text, tonal buttons (Export) |
| `md_secondary_container` | `#D9E6CF` | Tonal surface blocks |
| `md_surface` | `#FCFDF6` | Screen background (off-white, not pure white) |
| `md_surface_variant` | `#E1E4D8` | Card fills, outlined control fills |
| `md_outline` | `#73796D` | Borders, dividers, outlined buttons |
| `md_error` | `#BA1A1A` | Delete actions, validation errors |
| `md_on_error` | `#FFFFFF` | Text on error fills |

> **Anti-Lila Policy (kept from v1.0):** Purple/blue neon gradients and saturated multi-color buttons are banned. All emphasis comes from the Forest Green primary.

### Typography (Material Type Scale)

- **Headline (22sp / SemiBold)**: Screen titles (`HikeListActivity`, `HikeDetailActivity`).
- **Title Medium (16sp / Medium)**: Card titles, form section headers.
- **Body Medium (14sp / Regular)**: Descriptions, notes, form help text.
- **Label Large (14sp / Medium)**: Buttons, chips, badges.
- **Label Small (11sp / Medium, `labelStyle` mono-spaced for numbers)**: Numeric/metric labels — `length_km`, dates (`2026-08-01`), GPS coords, duration (`2 h 24 min`), temperature.

### Shape & Spacing

- **Corner radii**: 4dp (chips), 8dp (inputs/cards), 12dp (buttons), 16dp (dialogs/sheets), 999dp (pills, avatar).
- **Spacing scale**: 4 / 8 / 12 / 16 / 24 / 32 / 48 dp.
- **List rows**: 16dp padding, 56dp minimum height.

---

## Component Contract (ViewBinding)

Every binding is named `<layout_name>Binding` generated from its XML.

| Component | Binding field | Spec |
|-----------|---------------|------|
| **Primary Button** | `btn_<action>` → `MaterialButton` (`style="@style/Widget.Material3.Button"`) | Filled `md_primary`; 12dp radius; 48dp min height; used for Save, Sign In, Add Hike, Use my location |
| **Outlined Button** | `btn_<action>` → `MaterialButton` (`Widget.Material3.Button.OutlinedButton`) | `md_outline` border; `md_primary` text; Filter, Delete All, Retake photo |
| **Tonal Button** | `btn_<action>` → `MaterialButton` (`Widget.Material3.Button.TonalButton`) | `md_secondary_container`; Export PDF |
| **Text Input** | `et_<field>` → `TextInputEditText` inside `TextInputLayout` | `md_surface_variant` fill, 8dp radius; helper + error text from strings |
| **Pickers** | `dp_<field>` → `MaterialDatePicker` / `MaterialTimePicker` | Date default today; time default now |
| **Radio Group** | `rg_<field>` → `RadioGroup` with `RadioButton` pairs | Parking: Yes/No |
| **Spinner** | `sp_<field>` → `Spinner` with array from strings | Difficulty, Weather, Wind |
| **Rating** | `rb_trail_rating` → `RatingBar` (`stepSize="1.0"`, `numStars="5"`) | Trail condition |
| **List** | `rv_hikes` / `rv_observations` → `RecyclerView` | `ListAdapter` + `DiffUtil` |
| **Empty State** | `tv_empty` + `img_empty` | Centered illustration + message |
| **Progress** | `progress_<context>` → `CircularProgressIndicator` | Export, sync, login loading |

---

## Screen-by-Screen Layout Spec

### 1. `activity_login` — Firebase sign-in
- Vertical `LinearLayout` centered: app title, `et_email`, `et_password` (password toggle), `btn_sign_in`, `btn_create_account`, inline `tv_error`.
- On loading: disable both buttons, show `progress_login`.
- Success → navigate to `HikeListActivity` and finish.

### 2. `activity_hike_list` — List + search + logout
- `Toolbar` (title "My Hikes", overflow menu: Sign out).
- `et_search` (`TextInputEditText`) — live `LIKE` name search (Feature D basic).
- `btn_filter` (outlined) → `SearchFilterActivity` (Feature D advanced).
- `btn_add_hike` (FAB, `md_primary`) → `AddHikeActivity`.
- `rv_hikes` with `item_hike` rows:
  - 64dp `img_hike_thumb` (photo or placeholder),
  - `tv_hike_name`, `tv_hike_location`,
  - meta row: date, length, difficulty chip, parking icon, trail stars (compact).
- `tv_empty` when no hikes: "No hikes yet — tap + to add your first hike".
- Click row → `HikeDetailActivity` (with `EXTRA_HIKE_ID`).

### 3. `activity_add_hike` — Create / edit form + G extras
Scrollable `NestedScrollView` with sections:

**Section: Hike Details (always visible)**
- `et_name` (required), `et_location` (required), `dp_date` (required), `rg_parking`, `et_length_km` (numeric), `sp_difficulty`, `et_description` (multiline, optional).

**Section: Location & Map (G1)**
- `btn_use_location` (primary) → FusedLocationProvider fix; `et_latitude`, `et_longitude` (read-only, filled on fix); coordinate pair validated ±90 / ±180.

**Section: Photo (G2)**
- `img_photo_preview` (thumbnail or placeholder), `btn_capture_photo` (primary), `btn_retake_photo` (outlined, hidden until captured). Launches `MediaStore.ACTION_IMAGE_CAPTURE` via `FileProvider` URI.

**Section: Duration (G3)**
- `tv_estimated_duration` (read-only, live-updates on length/difficulty change: "Estimated: 2 h 24 min"), `et_actual_duration_min` (optional, 1–1440).

**Section: Weather (G4, collapsible)**
- `sp_weather_condition`, `et_temperature_c` (optional, −60..60), `sp_wind`, `et_weather_notes` (optional, max 500).

**Section: Trail Rating (G6, collapsible)**
- `rb_trail_rating` (1–5), `tv_rating_label` ("4 / 5 — Good"), `et_trail_notes` (optional).

**Footer**
- `btn_save_hike` (primary), `btn_cancel` (text). In edit mode: toolbar `btn_delete_hike` (error red, confirm dialog).

### 4. `activity_hike_detail` — Detail + actions (hub screen)
`NestedScrollView`, top `CollapsingToolbarLayout` or plain toolbar with:
- Overflow menu: **Export PDF** (tonal `btn_export_pdf`), **Edit**, **Delete**.
- Hero block: name (headline), location, date.
- **Metrics card**: length, difficulty chip (color-coded), parking, estimated/actual duration (G3).
- **Photo card (G2)**: `img_photo` full-width; empty placeholder state.
- **Map card (G1)**: `SupportMapFragment` with marker at saved lat/lng; if no coords → "Location not saved" empty state with `btn_use_location` shortcut.
- **Weather card (G4)**: condition icon + label, temperature, wind; "No weather recorded" empty state.
- **Rating card (G6)**: `RatingBar` (read-only) + label.
- **Observations card**: `btn_add_observation` → `AddObservationActivity`; `rv_observations` (`item_observation`); empty state "No observations yet".
- **Export**: runs `PdfReportBuilder` on background thread with `progress_export`; on completion `Intent.ACTION_SEND` share sheet (G5).

### 5. `activity_hike_map` — Full-screen map (G1)
- `SupportMapFragment` filling content area; marker at hike coords, camera zoom 14; back navigation to detail.

### 6. `activity_add_observation` — Observation form
- `et_obs_title` (required), `dp_obs_time` (TimePicker, defaults to now, HH:mm), `et_obs_comment` (optional), `btn_save_obs`, `btn_cancel`.

### 7. `activity_search_filter` — Advanced filters (Feature D)
- `et_filter_location`, `dp_date_from` / `dp_date_to`, `et_length_min` / `et_length_max`, `sp_filter_difficulty`, `btn_apply_filters` (primary), `btn_clear` (text). Builds `SimpleSQLiteQuery` safely.

---

## Navigation Flow (v3)

```
LoginActivity ──auth OK──▶ HikeListActivity ──row──▶ HikeDetailActivity
                              │  ▲                         │
                              │  ├─ FAB ──▶ AddHikeActivity (form + G extras) ◀─ Edit
                              │  ├─ Filter ──▶ SearchFilterActivity
                              │  └─ G1 ──▶ HikeMapActivity
HikeDetailActivity ── Add observation ──▶ AddObservationActivity
HikeDetailActivity ── Export PDF ──▶ (share sheet, G5)
```

Back behaviour: every new activity has `android:parentActivityName`; the FAB/create flow finishes on save.

---

## States & Patterns

- **Empty**: centered `img_empty` + `tv_empty` per screen (no hikes / no observations / no location / no photo / no weather).
- **Loading**: `CircularProgressIndicator` on login, export, GPS fix.
- **Error**: inline `tv_error` (login), `TextInputLayout` error strings (form fields), `Snackbar` for transient failures (sync, photo missing).
- **Delete**: `MaterialAlertDialogBuilder` confirmation before any destructive action (hike, observation, delete-all).
- **Confirm**: after save in create mode, show a confirmation screen/dialog listing entered fields (Feature A) before returning.

---

## Rules Recap (Mapping to v3.0 Plan)

| Doc rule | Frontend contract |
|----------|-------------------|
| Feature A form validation | `TextInputLayout` errors, pickers with defaults, confirmation step |
| Feature B Room v3 | No UI impact beyond new fields; list shows thumbnail + rating |
| Feature D search/filter | Live name search field; advanced filter screen |
| G1 Map & GPS | `Use my location` button; map card + full-screen map; empty state |
| G2 Photos | Capture via camera intent + `FileProvider`; preview; list thumbnail |
| G3 Duration | Live estimate label; actual duration field |
| G4 Weather | Collapsible condition/wind/temp/notes section |
| G5 Export | Tonal Export PDF action; progress; share sheet |
| G6 Rating | 5-star `RatingBar` in form + read-only in detail + compact stars in list |

---

## Do's and Don'ts

### Do's
- **DO** use ViewBinding everywhere (`ActivityXxxBinding.inflate(...)`).
- **DO** use M3 widgets (`MaterialButton`, `TextInputLayout`, `MaterialDatePicker`) over stock widgets.
- **DO** keep all strings in `strings.xml`; never hardcode UI text in Java.
- **DO** respect 48dp touch targets and 56dp list rows.
- **DO** use mono-styled labels for numeric metrics (length, dates, coords, duration).
- **DO** show an explicit empty/loading/error state for every data-backed screen.

### Don'ts
- **DON'T** use purple/blue neon glows or rainbow gradients (anti-lila).
- **DON'T** use `findViewById` — ViewBinding only.
- **DON'T** run Room, PDF, or GPS work on the main thread (repository / helpers only).
- **DON'T** use generic emojis for icons — use Material vector drawables.
- **DON'T** hide destructive actions behind a single tap — always confirm.
