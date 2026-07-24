# M-Hike Android App — Agent Briefing & Development Plan

**Version:** 1.0  
**Platform:** Android (Native Java)  
**Tech Stack:** Java, Android SDK, SQLite, Material Design 3  
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
- **Persistence**: SQLite local database only (no server integration in this phase)
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

**Rule 1: SQLite Database Schema**
```
TABLE hikes (
  hike_id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  location TEXT NOT NULL,
  date TEXT NOT NULL (ISO 8601 format: YYYY-MM-DD),
  parking_available TEXT NOT NULL (Yes/No),
  length_km REAL NOT NULL,
  difficulty TEXT NOT NULL,
  description TEXT,
  custom_field_1 TEXT,
  custom_field_2 TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

TABLE observations (
  observation_id INTEGER PRIMARY KEY AUTOINCREMENT,
  hike_id INTEGER NOT NULL,
  observation_text TEXT NOT NULL,
  observation_time TEXT NOT NULL (ISO 8601 format),
  comments TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY(hike_id) REFERENCES hikes(hike_id) ON DELETE CASCADE
);
```

**Rule 2: CRUD Operations**
- **Create**: Insert new hike into database
- **Read**: List all hikes; view single hike details
- **Update**: Edit any hike's fields
- **Delete**: Remove individual hike or all hikes with confirmation dialog
- **Reset**: Clear entire database with explicit user confirmation ("Delete all hikes permanently?")

**Rule 3: List Display**
- Show hikes in list view with key info: Name, Location, Date, Difficulty (with color coding)
- Implement `RecyclerView` with `ListAdapter` for efficient rendering
- Long-tap or swipe actions: Edit, Delete, View Details
- Empty state message when no hikes exist

**Rule 4: Error Handling**
- Database errors: Show user-friendly message ("Failed to load hikes. Please try again.")
- Don't crash on database failures
- Log errors internally for debugging

---

### Feature C: Observations (15% of coursework)

**Rule 1: Observation Data Model**
- **Required Fields**:
  - Observation text (what was observed)
  - Time (defaults to current date/time, user can override)
- **Optional Fields**:
  - Additional comments (detailed notes)

**Rule 2: Workflow**
1. User selects a hike from the hike list
2. UI shows hike details + button to "Add Observation"
3. Opens observation entry form with:
   - Text input for observation
   - DateTime picker (defaults to now)
   - Text input for comments
4. Save observation to database (linked to hike_id)
5. Show list of all observations for this hike
6. Each observation: show time, text, comments
7. Swipe/long-tap to: View, Edit, Delete observation

**Rule 3: Multiple Observations Per Hike**
- No limit on number of observations
- Display in chronological order (newest first or oldest first, consistent)
- Show observation count badge on hike card

---

### Feature D: Search (10% of coursework)

**Rule 1: Basic Search (Minimum)**
- User enters hike name
- Display first matching hike
- Case-insensitive search
- Partial matching (search "Snow" → find "Snowdon")

**Rule 2: Advanced Search (Preferred)**
- Multi-criteria search filters:
  - Name (partial match)
  - Location (partial match)
  - Date range (from/to)
  - Length range (min/max km)
  - Difficulty level
- Allow combining filters
- Display all matching results in list view
- Tappable result to view full details

**Rule 3: Search UX**
- Search field accessible from main screen
- Clear/cancel search easily
- Show "X results found"
- Handle zero results gracefully ("No hikes match your search")

---

### Feature G: Additional Features (10% of coursework)

**Requirement**: Implement 1-2 creative enhancements **in addition to** core features (a-d).

**Examples**:
- Photo capture & storage (user takes photo at hike location)
- GPS integration (capture start/end location automatically)
- Map view (show hike location on map)
- Export hike to PDF or email
- Difficulty color-coding on list
- Hike duration calculator
- Weather notes template
- Trail condition ratings

**Rule**: Additional features must not compromise core functionality. Core features should work perfectly first.

---

## 3. SYSTEM ARCHITECTURE & DESIGN RULES

### Package Structure
```
com.example.mhike/
├── activity/           # Android Activities (screens)
│   ├── MainActivity.java
│   ├── HikeListActivity.java
│   ├── HikeDetailActivity.java
│   ├── AddEditHikeActivity.java
│   ├── SearchActivity.java
│   └── AddObservationActivity.java
├── database/           # SQLite & DAOs
│   ├── AppDatabase.java
│   ├── HikeDAO.java
│   └── ObservationDAO.java
├── model/              # Data models
│   ├── Hike.java
│   └── Observation.java
├── repository/         # Business logic
│   ├── HikeRepository.java
│   └── ObservationRepository.java
├── adapter/            # RecyclerView adapters
│   ├── HikeListAdapter.java
│   └── ObservationListAdapter.java
└── ui/utils/           # Helpers
    ├── DateTimeUtils.java
    └── ValidationUtils.java
```

### Architecture Pattern: Repository + DAO
- **Reason**: Separates data access from business logic; easier testing & maintenance
- Use SQLiteOpenHelper or Room (preferred)
- DAO layer provides CRUD abstraction
- Repository orchestrates DAOs & validation

### Threading Model
- Database operations: Background threads (AsyncTask, Coroutines, or Thread)
- UI updates: Main thread only
- Show loading indicators for long operations

### Error Handling
- Try-catch around database operations
- User-facing messages for errors (no technical jargon)
- Logging for debugging (use Android Log class)

---

## 4. UI/UX DESIGN RULES

### Design System: Material Design 3
- **Colors**:
  - Primary: `#6750A4` (Purple)
  - Secondary: `#625B71` (Purple variant)
  - Tertiary: `#7D5260` (Rose)
  - Error: `#B3261E` (Red)
  - Background: `#FFFBFE` (Off-white)
  - Surface: `#FEFAF7`

- **Typography**:
  - Display Large: 57sp, weight 400
  - Headline Large: 32sp, weight 700
  - Body Large: 16sp, weight 400
  - Body Medium: 14sp, weight 500 (labels, buttons)
  - Label Small: 12sp, weight 500

- **Spacing**: 8dp baseline (8, 16, 24, 32, 48dp)
- **Corner Radius**: 12dp (default), 16dp (large components)

### Navigation Hierarchy
```
MainActivity (Home/Hike List)
├─ Add Hike → AddEditHikeActivity
├─ View Hike → HikeDetailActivity
│  ├─ Edit Hike
│  ├─ Add Observation → AddObservationActivity
│  └─ View Observations → ObservationList
└─ Search → SearchActivity
```

### Key Screens

**1. Hike List Screen (MainActivity)**
- Floating Action Button (FAB): "Add Hike"
- RecyclerView showing all hikes
- Each card: Name, Location, Date, Difficulty (color-coded), Parking (icon)
- Swipe/long-tap menu: View, Edit, Delete
- Empty state: "No hikes yet. Tap + to add one."

**2. Add/Edit Hike Screen**
- ScrollView with form fields
- Date picker (dialog popup)
- Spinner for difficulty
- RadioGroup for parking
- Number picker for length
- Buttons: Save, Cancel
- Form validation on Save

**3. Confirmation Screen**
- Display all entered fields
- Buttons: Confirm, Back to Edit
- Clean, readable layout

**4. Hike Detail Screen**
- Show all hike fields
- Observations section with list
- Buttons: Edit Hike, Add Observation, Delete Hike
- Observation count badge

**5. Search Screen**
- Search bar (clearable)
- Filter options (collapsible section)
- Results list or empty state
- Tap result to view details

### Accessibility
- All buttons & icons have descriptive content descriptions
- Minimum 48dp touch targets
- Color not sole indicator (use icons + text for difficulty)
- Text contrast ≥ 4.5:1

### Responsiveness
- Layouts adapt to portrait/landscape
- RecyclerView items stack vertically on narrow screens
- Use `match_parent` / `wrap_content` appropriately
- Test on: Nexus 5X (5.2"), Nexus 6P (5.7"), Pixel 3 (5.5")

---

## 5. CODE QUALITY RULES

### Naming Conventions (Java)
- **Classes**: PascalCase (e.g., `HikeDetailActivity`)
- **Methods**: camelCase (e.g., `saveHike()`, `validateInput()`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_HIKE_NAME_LENGTH`)
- **Variables**: camelCase (e.g., `hikeList`, `userInput`)

### Code Structure
- Methods ≤ 30 lines (break into smaller units)
- Classes ≤ 500 lines (separate concerns)
- No duplicate code; extract to utilities
- Comments for "why", not "what" (code speaks for itself)

### Example Comment Style
```java
// BAD: Describes what code does
// This loop iterates through hikes
for (Hike hike : hikes) { ... }

// GOOD: Explains intention
// Filter out hikes with no observations (reduces visual clutter in list)
List<Hike> hikesWith observations = hikes.stream()
    .filter(h -> h.getObservations().size() > 0)
    .collect(Collectors.toList());
```

### Android Best Practices
- Use `findViewById()` or ViewBinding, avoid `findViewById()` repeatedly
- Implement `Parcelable` for passing objects between Activities
- Use `Bundle` for Activity arguments (not direct object passing)
- Lifecycle-aware components (avoid memory leaks)
- Keep Activities lightweight (move logic to Repositories/ViewModels)

---

## 6. TESTING STRATEGY

### Unit Tests (Optional but Recommended)
- Test `HikeRepository` methods
- Test `ValidationUtils` (required field checks)
- Test `DateTimeUtils`

### Manual Testing Checklist
- [ ] Add hike with all valid data → saves and displays correctly
- [ ] Add hike with missing required field → shows error
- [ ] Edit hike → changes reflected in database
- [ ] Delete hike → removed from list + observations deleted
- [ ] Add observation to hike → displays in observation list
- [ ] Search by name → finds hike (partial match)
- [ ] Advanced search with filters → correct results
- [ ] Rotate device → data retained, UI reorients
- [ ] Empty states → clear messaging
- [ ] Error cases (e.g., invalid date) → handled gracefully

### Performance Testing
- List with 100+ hikes → scrolls smoothly
- Search performance acceptable (< 500ms)
- No memory leaks (use Android Profiler)

---

## 7. DELIVERABLES CHECKLIST

### Code Deliverable
- [ ] Source code in organized package structure
- [ ] `README.md` with:
  - Build instructions (Android Studio setup)
  - Database schema explanation
  - Key class descriptions
  - Any external libraries used
- [ ] Gradle dependencies documented
- [ ] No hardcoded API keys or credentials

### Demo Video (15 minutes)
- [ ] Start by showing app structure/code organization
- [ ] Demonstrate each feature (a-d) in action:
  - Add hike (show form validation)
  - View hike list & details
  - Add observations
  - Search functionality
  - Edit/delete operations
- [ ] Show database persistence (restart app, data remains)
- [ ] Demonstrate additional feature(s)
- [ ] Walk through relevant code sections
- [ ] Explain design decisions (why you chose specific patterns)

### Report Sections (Part B)
- **Section 1 (2%)**: Feature checklist (which features completed, bugs, etc.)
- **Section 2 (2%)**: Screenshots with annotations
- **Section 3 (4%)**: 350-word reflection on development lessons
- **Section 4 (8%)**: 700-1000 word evaluation covering:
  - HCI: UI intuitiveness, accessibility, user flow
  - Security: Input validation, data storage safety
  - Screen sizes: How app adapts to different devices
  - Live deployment: What changes needed for production
- **Section 5 (2%)**: Code listings + language proficiency

---

## 8. COMMON PITFALLS TO AVOID

1. **Database Crashes**
   - Always use transactions for multi-step operations
   - Test edge cases: null values, empty lists, concurrent access

2. **UI Freezing**
   - Never perform database operations on main thread
   - Use background threads; post results back to main thread

3. **Memory Leaks**
   - Don't hold context references in static variables
   - Cancel async tasks when Activity is destroyed
   - Use ViewBinding (not findViewById in a loop)

4. **Poor UX**
   - Forgetting to disable Save button during submission
   - No loading indicators for long operations
   - Confusing error messages
   - No confirmation before destructive actions

5. **Validation Gaps**
   - Not validating on both client (immediate feedback) and server (when applicable)
   - Assuming user input is always valid
   - Not handling edge cases (empty strings, very large numbers)

6. **Incomplete Features**
   - Starting too many features; finishing few completely
   - Prioritize: Core features > polish > additional features
   - Test thoroughly before moving on

---

## 9. DEVELOPMENT WORKFLOW

### Phase 1: Setup & Core Database (Week 1-2)
- [ ] Android Studio project initialized
- [ ] Database schema implemented (SQLite)
- [ ] DAO layer created
- [ ] Repository layer implemented

### Phase 2: Feature A & B (Week 3-4)
- [ ] Add/Edit Hike Activity UI
- [ ] Form validation
- [ ] Hike List Activity
- [ ] Edit/Delete functionality

### Phase 3: Feature C (Week 5)
- [ ] Observation entry form
- [ ] Observation list display
- [ ] Link observations to hikes

### Phase 4: Feature D (Week 6)
- [ ] Search Activity UI
- [ ] Basic search implementation
- [ ] Advanced search filters

### Phase 5: Polish & Feature G (Week 7)
- [ ] Bug fixes
- [ ] UI/UX refinement
- [ ] Implement 1-2 additional features
- [ ] Performance testing

### Phase 6: Demo & Report (Week 8)
- [ ] Record 15-minute video demo
- [ ] Write comprehensive report
- [ ] Prepare for Q&A session

---

## 10. AI AGENT INSTRUCTIONS

When creating supporting documents (system design, UI mockups, code templates, etc.) from this brief, follow these rules:

1. **Do not invent requirements** — stick to features a-d + one feature g
2. **Always provide reasoning** for architectural choices (e.g., "Using Repository pattern because...")
3. **Include concrete examples** — show code snippets, screen layouts, database queries
4. **Prioritize core features first** — additional features are bonus only
5. **Assume moderate developer** — explain Android concepts but assume Java familiarity
6. **Reference Material Design 3** — provide specific color codes, typography sizes
7. **Include error cases** — show what happens when user enters invalid data
8. **Test-driven mindset** — design with testing in mind
9. **No placeholders** — every section should be implementable
10. **Link to Android documentation** — provide official resource links when helpful

---

## 11. REFERENCES & RESOURCES

- Android Developers: https://developer.android.com/
- Material Design 3: https://m3.material.io/
- Android Architecture Guide: https://developer.android.com/topic/architecture
- SQLite Android: https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase
- Java Naming Conventions: https://www.oracle.com/java/technologies/javase/codeconventions-136057.html

---

**Document Version**: 1.0  
**Last Updated**: 2025-26 COMP1786 Term 1  
**Prepared for**: M-Hike Android Development (Coursework Implementation)
