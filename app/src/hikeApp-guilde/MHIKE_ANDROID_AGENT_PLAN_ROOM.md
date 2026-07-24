# M-Hike Android App — Agent Briefing & Development Plan

**Version:** 2.0 (Updated to Room Database & M3 Button Styling)  
**Platform:** Android (Native Java)  
**Tech Stack:** Java, Android SDK, Room Database, Material Design 3  
**Status:** Coursework Implementation (COMP1786 Term 1)  

---

## 1. PROJECT CONTEXT

### Overview
**M-Hike** is a hiker management mobile application that allows outdoor enthusiasts to plan hikes, record detailed observations during hikes, and manage hike data locally. This is the **native Android implementation** (Part A features a-d).

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

### Constraints & Requirements
- **Technology**: Android Java (not Xamarin/MAUI, not React Native for this version)
- **Persistence**: Room Database (SQLite abstraction) only (no server integration in this phase)
- **Scope**: Features a-d plus additional feature(s) from feature g
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
  - Two or more additional fields (student's choice - be creative!)
    - Suggested: elevation gain, surface type, water source, estimated duration, guide name, etc.

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

**Rule 1: Room Database Schema**
- **Database Class**: `AppDatabase` (extends `RoomDatabase` with versioning and migration pathways)
- **Hikes Table (`hikes` Entity)**:
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
- **Observations Table (`observations` Entity)**:
  - `id`: `long` Primary Key, auto-generated.
  - `hike_id`: `long` (Foreign Key pointing to `hikes.id` with `ON DELETE CASCADE` and index tracking for query optimization).
  - `title`: `String` (Required).
  - `obs_time`: `String` (Required, HH:mm format).
  - `comment`: `String` (Optional).

**Rule 2: CRUD Operations**
- **Create**: Insert new hike/observation via `@Insert(onConflict = OnConflictStrategy.ABORT)` annotations.
- **Read**: Fetch lists or entries by ID using `@Query` compile-time validated SQL strings.
- **Update**: Edit fields using `@Update` annotations.
- **Delete**: Remove items using `@Delete` annotations. 
- **Reset**: `@Query("DELETE FROM hikes")` clears all hikes and automatically cascades to delete all observations.

**Rule 3: List Display**
- Show hikes in list view with key info: Name, Location, Date, Difficulty (color-coded text badge), Parking (icon/badge)
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
- Dynamic queries built safely at runtime via Room's `@RawQuery` using `SimpleSQLiteQuery` with positional arguments (`?`) to prevent SQL Injection.

---

### Feature G: Additional Features (10% of coursework)

**Requirement**: Implement creative enhancements (such as difficulty color-coded text badges, Material Design 3 outline styles, customized layout buttons, and cascade deletes).

---

## 3. SYSTEM ARCHITECTURE & DESIGN RULES

### Package Structure
```
com.example.m_hikeapp/
├── activity/           # Android Activities
│   ├── HikeListActivity.java
│   ├── HikeDetailActivity.java
│   ├── AddHikeActivity.java
│   ├── SearchFilterActivity.java
│   └── AddObservationActivity.java
├── database/           # Room Database definition & Migration
│   └── AppDatabase.java
├── dao/                # Room DAOs (interfaces compiled at build time)
│   ├── HikeDao.java
│   └── ObservationDao.java
├── model/              # Entity classes
│   ├── Hike.java
│   └── Observation.java
├── repository/         # Thread-safe repository wrapper
│   └── HikeRepository.java
├── adapter/            # ListAdapter + DiffUtil classes
│   ├── HikeAdapter.java
│   └── ObservationAdapter.java
└── util/               # Input validation helpers
    ├── ValidationResult.java
    └── ValidationUtils.java
```

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
- **Touch Targets**: Minimum `48dp` for standard clicks and `56dp` minimum height for list items.

---

## 5. CODE QUALITY RULES

- Follow standard Java naming patterns (PascalCase classes, camelCase methods/variables, UPPER_SNAKE_CASE constants).
- Keep logic out of Activities; delegate database execution to the `HikeRepository` background threads.
- Do not make hardcoded String references. Use `strings.xml` resource keys for validation messages, labels, hints, and dialog contents.
- Keep methods short and ensure classes adhere to single responsibilities.

---

**Last Updated**: 2026 Room Migration Update  
**Prepared for**: M-Hike Native Android Implementation  
