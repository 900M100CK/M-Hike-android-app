# M-Hike: Mobile Hiker Management Application

An Android native mobile application built using Java and SQLite, designed to help hikers plan, record, and manage their hikes and real-time trail observations. This project is developed as part of the **COMP1786 Mobile Application Design and Development** coursework (Term 1, 2025-26).

---

## 🚀 Features

### Core Features (Features a-d)
*   **Hike Data Entry & Validation:** Record essential hike details with robust validation:
    *   Hike Name
    *   Location/Destination
    *   Date of Hike
    *   Parking Availability (Yes/No)
    *   Length of Hike (km)
    *   Difficulty Level (Easy, Medium, Hard, etc.)
    *   Additional Description
*   **Local SQLite Persistence (CRUD):** 
    *   Store hike data locally on the device (no internet required).
    *   View, edit, update, or delete existing hikes.
*   **Observations Tracking:**
    *   Add multiple real-time observations (e.g., wildlife sightings, weather changes, vegetation, trail blockages) to any specific hike.
    *   Each observation records a name, time of observation, and optional comments.
*   **Advanced Search & Filter:**
    *   Quickly search hikes by name, location, or date.
    *   Filter hikes based on difficulty or parking availability.

---

## 🛠️ Tech Stack & Architecture

*   **Platform:** Native Android
*   **Language:** Java
*   **UI/UX Framework:** Android XML layouts conforming to **Material Design 3 (M3)**
*   **Database:** SQLite (local-first storage with `DatabaseHelper` and helper classes)
*   **Design Patterns:**
    *   **Repository Pattern:** Decouples the UI logic from raw database operations.
    *   **DAO (Data Access Object) Pattern:** Abstracts SQLite query execution.
    *   **Adapter Pattern:** Efficiently populates lists via `RecyclerView` and custom adapters (`HikeAdapter`, `ObservationAdapter`).

---

## 📂 Project Structure

The project code is organized cleanly within the standard Android package layout:

```text
com.example.m_hikeapp
├── adapter/
│   ├── HikeAdapter.java          # Handles RecyclerView listing for hikes
│   └── ObservationAdapter.java   # Handles RecyclerView listing for observations
├── dao/
│   ├── HikeDao.java              # Database queries for hikes
│   └── ObservationDao.java       # Database queries for observations
├── database/
│   └── DatabaseHelper.java       # SQLite database initialization & migrations
├── model/
│   ├── Hike.java                 # Hike data model
│   └── Observation.java          # Observation data model
├── repository/
│   └── HikeRepository.java       # Business logic repository abstraction
├── util/
│   ├── ValidationResult.java     # Form validation results wrapper
│   └── ValidationUtils.java      # Reusable form validation helper methods
├── MainActivity.java             # Entry point of the application
├── HikeListActivity.java         # Activity displaying all recorded hikes
├── HikeDetailActivity.java       # Activity detailing a hike & displaying observations
├── AddHikeActivity.java          # Activity to add/edit a hike
├── AddObservationActivity.java   # Activity to add observation to a hike
└── SearchFilterActivity.java     # Activity to search and filter hikes
```

---

## ⚙️ Installation & Setup

1.  **Prerequisites:**
    *   Android Studio (Ladybug or newer recommended)
    *   Android SDK 34 or higher
    *   Java JDK 17
2.  **Steps to Run:**
    *   Clone this repository:
        ```bash
        git clone https://github.com/900M100CK/M-Hike-android-app.git
        ```
    *   Open Android Studio and choose **File > Open**, then select the project root directory.
    *   Sync Gradle files and build the project.
    *   Run the application on an Android Emulator or a physical device with USB debugging enabled.
