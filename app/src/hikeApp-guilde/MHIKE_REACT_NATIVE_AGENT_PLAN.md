# M-Hike React Native App — Agent Briefing & Development Plan

**Version:** 1.0  
**Platform:** Cross-Platform (iOS + Android via React Native)  
**Tech Stack:** React Native, Expo, SQLite (via expo-sqlite), TypeScript, React Navigation  
**Status:** Coursework Implementation (COMP1786 Term 1)  
**Note:** Replaces Xamarin/MAUI requirement; same features as Android version  

---

## 1. PROJECT CONTEXT

### Overview
**M-Hike** React Native is the **cross-platform hybrid implementation** of the hiker management app. It provides feature parity with the Android native version (features a-d from coursework spec), running on both iOS and Android from a single codebase.

### Strategic Rationale: React Native vs. Xamarin/MAUI
- **Why React Native?**
  - JavaScript/TypeScript ecosystem more accessible than C#
  - Expo tooling simplifies development without native build overhead
  - Smaller bundle size; faster hot reload
  - Strong community and third-party packages for hike-related features (maps, camera, geolocation)
  - Better dev experience for coursework (no native compilation required)

### Target Users
- Same as Android version: outdoor hikers aged 18-65
- Extended reach: iPhone users (iOS 12+) + Android users (API 24+)
- Community sharing potential on both platforms

### Business Goals
1. Demonstrate cross-platform development capability
2. Reduce code duplication vs. separate Android/iOS implementations
3. Deliver consistent UX across platforms
4. Maintain local-first, no-internet-required model

### Constraints & Requirements
- **Technology**: React Native + Expo (not native Android/iOS code)
- **Persistence**: SQLite via expo-sqlite (consistent with Android version)
- **Feature Scope**: Replicate features a-d from coursework spec
- **Code Sharing**: ≥ 95% JavaScript/TypeScript shared between iOS and Android
- **Deliverables**: Working app (testable on both platforms via Expo) + demo + report
- **Assessment deadline**: Arranged by partnerships (end of term)

---

## 2. TECH STACK & SETUP

### Core Dependencies
```json
{
  "react-native": "^0.72.0",
  "expo": "^50.0.0",
  "react-navigation": "^6.x",
  "@react-navigation/native": "^6.x",
  "@react-navigation/bottom-tabs": "^6.x",
  "@react-navigation/native-stack": "^6.x",
  "expo-sqlite": "^13.x",
  "expo-calendar": "^13.x",
  "react-native-gesture-handler": "^2.x",
  "react-native-reanimated": "^3.x",
  "react-native-screens": "^3.x",
  "zustand": "^4.x",
  "typescript": "^5.x",
  "@react-native-async-storage/async-storage": "^1.x"
}
```

### Project Structure
```
mhike-react-native/
├── app.json                    # Expo configuration
├── app.tsx                     # Entry point
├── src/
│   ├── screens/                # Screen components
│   │   ├── HomeScreen.tsx
│   │   ├── HikeListScreen.tsx
│   │   ├── AddEditHikeScreen.tsx
│   │   ├── HikeDetailScreen.tsx
│   │   ├── SearchScreen.tsx
│   │   ├── AddObservationScreen.tsx
│   │   └── ObservationListScreen.tsx
│   ├── components/             # Reusable UI components
│   │   ├── HikeCard.tsx
│   │   ├── ObservationCard.tsx
│   │   ├── SearchFilter.tsx
│   │   ├── FormField.tsx
│   │   └── LoadingSpinner.tsx
│   ├── database/               # SQLite database logic
│   │   ├── db.ts
│   │   ├── schema.ts
│   │   └── queries.ts
│   ├── store/                  # State management (Zustand)
│   │   ├── hikeStore.ts
│   │   └── observationStore.ts
│   ├── types/                  # TypeScript interfaces
│   │   ├── hike.ts
│   │   └── observation.ts
│   ├── utils/                  # Helper functions
│   │   ├── validation.ts
│   │   ├── dateTime.ts
│   │   └── constants.ts
│   ├── navigation/             # React Navigation config
│   │   ├── RootNavigator.tsx
│   │   └── types.ts
│   └── theme/                  # Design tokens
│       ├── colors.ts
│       ├── typography.ts
│       └── spacing.ts
├── assets/                     # Images, icons
├── package.json
├── tsconfig.json
└── README.md
```

### Development Environment
- **IDE**: VS Code or Android Studio + Xcode (Expo handles the rest)
- **Testing Device**: Expo Go app on physical device OR simulator
- **Build & Deploy**: `expo start` for development; `expo build` for production

---

## 3. FEATURE SPECIFICATIONS & RULES

### Feature A: Hike Data Entry (10% of coursework)

**Rule 1: Form Validation & Required Fields**
Same requirements as Android version:

- **Required Fields** (show error if empty):
  - Hike Name (e.g., "Snowdon", "Trosley Country Park")
  - Location (text input)
  - Date of hike (date picker)
  - Parking available (radio buttons: Yes/No)
  - Length of hike (numeric + unit: km/miles)
  - Difficulty level (picker: Easy, Moderate, Hard, Expert)

- **Optional Fields** (no error if empty):
  - Description (text area, max 500 chars)
  - Two+ custom fields (user's choice)

**Rule 2: React Native Form UX**
- Use controlled components (React state manages input values)
- `TextInput` for name, location, description
- `RNDateTimePicker` (via expo) for date selection
- `Picker` or custom selector for difficulty
- `SegmentedControl` or radio-style buttons for parking
- Real-time input validation feedback (green/red text below field)
- Save button disabled until all required fields filled

**Rule 3: Confirmation Screen**
- After form submission, show all entered data
- Buttons: "Confirm & Save" | "Back to Edit"
- Display in readable card format
- Allow user to press back button on phone to return to form

**Implementation Example**:
```typescript
// types/hike.ts
export interface Hike {
  id: string;
  name: string;
  location: string;
  date: string; // ISO 8601
  parkingAvailable: 'yes' | 'no';
  lengthKm: number;
  difficulty: 'easy' | 'moderate' | 'hard' | 'expert';
  description?: string;
  customField1?: string;
  customField2?: string;
  createdAt: string;
  updatedAt: string;
}

// store/hikeStore.ts (Zustand)
export const useHikeStore = create<HikeStore>((set) => ({
  hikes: [],
  addHike: async (hike: Hike) => {
    const result = await db.addHike(hike);
    set((state) => ({ hikes: [...state.hikes, hike] }));
  },
  // ... other methods
}));
```

---

### Feature B: Data Persistence & Management (15% of coursework)

**Rule 1: SQLite Database (via expo-sqlite)**
```sql
CREATE TABLE IF NOT EXISTS hikes (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  location TEXT NOT NULL,
  date TEXT NOT NULL,
  parking_available TEXT NOT NULL,
  length_km REAL NOT NULL,
  difficulty TEXT NOT NULL,
  description TEXT,
  custom_field_1 TEXT,
  custom_field_2 TEXT,
  created_at TEXT DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS observations (
  id TEXT PRIMARY KEY,
  hike_id TEXT NOT NULL,
  observation_text TEXT NOT NULL,
  observation_time TEXT NOT NULL,
  comments TEXT,
  created_at TEXT DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY(hike_id) REFERENCES hikes(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_hike_date ON hikes(date);
CREATE INDEX IF NOT EXISTS idx_observation_hike ON observations(hike_id);
```

**Rule 2: Database Layer Architecture**
```typescript
// database/db.ts
import * as SQLite from 'expo-sqlite';

const db = SQLite.openDatabase('mhike.db');

export const initializeDatabase = async () => {
  return new Promise<void>((resolve, reject) => {
    db.transaction((tx) => {
      tx.executeSql(CREATE_HIKES_TABLE);
      tx.executeSql(CREATE_OBSERVATIONS_TABLE);
      tx.executeSql(CREATE_INDEXES);
    }, reject, () => resolve());
  });
};

export const addHike = async (hike: Hike): Promise<void> => {
  return new Promise((resolve, reject) => {
    db.transaction((tx) => {
      tx.executeSql(
        `INSERT INTO hikes 
         (id, name, location, date, parking_available, length_km, difficulty, description, custom_field_1, custom_field_2)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
        [
          hike.id, hike.name, hike.location, hike.date, hike.parkingAvailable,
          hike.lengthKm, hike.difficulty, hike.description, hike.customField1, hike.customField2
        ],
        () => resolve(),
        (_, error) => reject(error)
      );
    });
  });
};

// ... other CRUD operations
```

**Rule 3: State Management with Zustand**
- Single source of truth for all hikes & observations
- Persist to async storage for fast rehydration
- Synchronize with SQLite on app startup

**Rule 4: List Display (FlatList)**
```typescript
// screens/HikeListScreen.tsx
import { FlatList, StyleSheet } from 'react-native';
import { HikeCard } from '../components/HikeCard';

export const HikeListScreen = () => {
  const { hikes } = useHikeStore();

  const handleDelete = (hikeId: string) => {
    Alert.alert(
      'Delete Hike?',
      'This action cannot be undone.',
      [
        { text: 'Cancel', onPress: () => {}, style: 'cancel' },
        { text: 'Delete', onPress: () => deleteHike(hikeId), style: 'destructive' }
      ]
    );
  };

  return (
    <FlatList
      data={hikes}
      keyExtractor={(item) => item.id}
      renderItem={({ item }) => (
        <HikeCard
          hike={item}
          onPress={() => navigation.navigate('HikeDetail', { hikeId: item.id })}
          onDelete={() => handleDelete(item.id)}
        />
      )}
      ListEmptyComponent={<EmptyState />}
      contentContainerStyle={styles.listContent}
    />
  );
};
```

**Rule 5: Error Handling**
- Wrap all database operations in try-catch
- Show user-friendly error messages via Toast or Alert
- Log errors for debugging (use React Native's console or Sentry)

---

### Feature C: Observations (15% of coursework)

**Rule 1: Observation Data Model**
```typescript
// types/observation.ts
export interface Observation {
  id: string;
  hikeId: string;
  text: string; // Required
  time: string; // ISO 8601, required (defaults to now)
  comments?: string; // Optional
  createdAt: string;
}
```

**Rule 2: Workflow**
1. User navigates to hike detail screen
2. Shows hike info + list of observations (if any)
3. Taps "Add Observation" button
4. Opens form with:
   - Text input for observation (required)
   - DateTime picker (defaults to current time)
   - Text area for comments (optional)
5. User submits → observation saved to DB
6. Returns to hike detail screen
7. Observation appears in list immediately

**Rule 3: Observation List**
- Display all observations for a hike
- Show newest first (or oldest first, consistently)
- Each card shows: time, observation text, comment preview
- Tap to expand; swipe/long-press to edit/delete

---

### Feature D: Search (10% of coursework)

**Rule 1: Basic Search (Minimum)**
```typescript
// screens/SearchScreen.tsx
export const SearchScreen = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<Hike[]>([]);
  const { hikes } = useHikeStore();

  const handleSearch = (text: string) => {
    setQuery(text);
    if (text.trim() === '') {
      setResults([]);
      return;
    }
    // Case-insensitive partial match
    const filtered = hikes.filter((hike) =>
      hike.name.toLowerCase().includes(text.toLowerCase())
    );
    setResults(filtered);
  };

  return (
    <SafeAreaView>
      <TextInput
        placeholder="Search hikes by name..."
        value={query}
        onChangeText={handleSearch}
        clearButtonMode="while-editing"
      />
      <FlatList
        data={results}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => <HikeCard hike={item} />}
        ListEmptyComponent={
          query ? <Text>No hikes found</Text> : <Text>Start typing to search</Text>
        }
      />
    </SafeAreaView>
  );
};
```

**Rule 2: Advanced Search (Preferred)**
- Multi-filter interface:
  - Name (text input)
  - Location (text input)
  - Date range (date picker from/to)
  - Length range (slider or number inputs)
  - Difficulty (multi-select picker)
- Combine filters with AND logic
- Show number of results matching filters
- Filter state persists during navigation

---

### Feature G: Additional Features (10% of coursework)

**Suggested Enhancements** (implement 1-2):
- **Photo Capture**: Use `expo-camera` to take photos during hike; store as base64 in SQLite
- **GPS Tracking**: Use `expo-location` to auto-capture hike start/end coordinates
- **Map View**: Integrate `react-native-maps` to show hike locations on map
- **Export PDF**: Use `react-native-pdf` to generate hike report
- **Difficulty Badges**: Color-coded difficulty indicators (Easy=Green, Hard=Red, etc.)
- **Hike Duration Calculator**: Calculate estimated vs. actual time

**Rule**: Additional features should not break core functionality. Core features must work flawlessly first.

---

## 4. STATE MANAGEMENT & ARCHITECTURE

### Zustand Store Pattern
**Why Zustand?**
- Lightweight (no Provider boilerplate)
- Simple API (familiar to Redux users)
- Works seamlessly with React Native
- Built-in async support

**Example Store**:
```typescript
// store/hikeStore.ts
import { create } from 'zustand';
import * as db from '../database/db';

interface HikeStore {
  hikes: Hike[];
  loading: boolean;
  error: string | null;
  loadHikes: () => Promise<void>;
  addHike: (hike: Hike) => Promise<void>;
  updateHike: (id: string, hike: Partial<Hike>) => Promise<void>;
  deleteHike: (id: string) => Promise<void>;
  deleteAllHikes: () => Promise<void>;
}

export const useHikeStore = create<HikeStore>((set) => ({
  hikes: [],
  loading: false,
  error: null,

  loadHikes: async () => {
    set({ loading: true, error: null });
    try {
      const hikes = await db.getAllHikes();
      set({ hikes, loading: false });
    } catch (error) {
      set({ error: error.message, loading: false });
    }
  },

  addHike: async (hike: Hike) => {
    try {
      await db.addHike(hike);
      set((state) => ({ hikes: [...state.hikes, hike] }));
    } catch (error) {
      set({ error: error.message });
    }
  },

  // ... other methods
}));
```

### Initialization Flow
1. App starts → `App.tsx` initializes database
2. `useEffect` calls `useHikeStore.loadHikes()`
3. All subsequent operations update Zustand store
4. Zustand updates automatically trigger re-renders

---

## 5. UI/UX DESIGN RULES

### Design System: Material 3 + React Native Paper

**Color Palette** (same as Android version):
```typescript
// theme/colors.ts
export const colors = {
  primary: '#6750A4',      // Purple
  secondary: '#625B71',    // Purple variant
  tertiary: '#7D5260',     // Rose
  error: '#B3261E',        // Red
  background: '#FFFBFE',   // Off-white
  surface: '#FEFAF7',      // Cream
  onPrimary: '#FFFFFF',
  onBackground: '#1C1B1F',
  onSurface: '#1C1B1F',
};
```

**Typography**:
```typescript
// theme/typography.ts
export const typography = {
  displayLarge: { fontSize: 57, fontWeight: '400', lineHeight: 64 },
  headlineLarge: { fontSize: 32, fontWeight: '700', lineHeight: 40 },
  bodyLarge: { fontSize: 16, fontWeight: '400', lineHeight: 24 },
  bodyMedium: { fontSize: 14, fontWeight: '500', lineHeight: 20 },
  labelSmall: { fontSize: 12, fontWeight: '500', lineHeight: 16 },
};
```

**Spacing Baseline**: 8dp (8, 16, 24, 32, 48)

### Navigation Structure
```
RootNavigator (Stack)
  ├─ HomeScreen (Tab Navigator)
  │  ├─ HikeListTab
  │  └─ SearchTab
  ├─ HikeDetailScreen (Stack)
  │  ├─ ObservationListScreen
  │  └─ AddObservationScreen
  └─ AddEditHikeScreen
```

### Key Screen Layouts

**1. Home Screen (Hike List Tab)**
- Top: Title "My Hikes"
- Center: FlatList of HikeCard components
- Bottom: Floating Action Button (FAB) for "Add Hike"
- Empty state: "No hikes. Tap + to create one."

**2. Add/Edit Hike Screen**
- ScrollView containing:
  - FormField components (name, location, etc.)
  - Date/Time picker (inline or modal)
  - Picker for difficulty
  - Radio buttons for parking
  - Number input for length
  - Save & Cancel buttons at bottom
- Validation errors show below each field

**3. Hike Detail Screen**
- ScrollView with:
  - Hike info cards (name, location, date, difficulty badge, parking icon, length)
  - "Add Observation" button
  - Observations list (FlatList)
  - Observation count badge
  - Edit/Delete buttons for hike

**4. Search Screen**
- SearchBar (collapsible filters below)
- Filter options: Name, Location, Date range, Length range, Difficulty
- Results list or empty state
- Tap result to navigate to HikeDetail

### Component Library
- **React Native Paper**: Pre-built Material 3 components
  - `Button`, `TextInput`, `Card`, `Chip`, `Badge`, `Snackbar`
- **Custom Components**: Wrap Paper components for consistency
  - `FormField`: TextInput + label + error message
  - `HikeCard`: Hike info with action buttons
  - `ObservationCard`: Observation display
  - `DifficultyBadge`: Color-coded difficulty

### Accessibility
- All interactive elements: minHeight 48dp (Apple) / 56dp (Google)
- Descriptive `accessible` prop and `accessibilityLabel` on all buttons/icons
- Color not sole indicator (use icons + text for status)
- Minimum text contrast: 4.5:1

### Responsiveness
- Use Dimensions API or ResponsiveDesign library
- Portrait & landscape support
- Adapt font sizes and spacing for different screen sizes (small phone vs. tablet)

---

## 6. CODE QUALITY RULES

### TypeScript Strict Mode
- Enable strict mode in `tsconfig.json`
- No `any` types; use proper interfaces
- Type all props, state, and returns

### Naming Conventions
- **Components**: PascalCase (e.g., `HikeListScreen`, `HikeCard`)
- **Functions**: camelCase (e.g., `handleSaveHike`, `validateInput`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_HIKE_NAME_LENGTH`)
- **Variables**: camelCase (e.g., `hikeList`, `isLoading`)
- **Files**: kebab-case or PascalCase matching content (e.g., `HikeCard.tsx`, `hike.store.ts`)

### Code Structure
- Functional components with hooks (no class components)
- Custom hooks for complex logic (e.g., `useHikeForm`)
- Extract long functions into utilities
- Keep components under 300 lines
- Props interfaces at top of file

**Example Component**:
```typescript
// components/HikeCard.tsx
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Card, Button } from 'react-native-paper';
import { Hike } from '../types/hike';

interface HikeCardProps {
  hike: Hike;
  onPress: () => void;
  onDelete: () => void;
}

export const HikeCard: React.FC<HikeCardProps> = ({ hike, onPress, onDelete }) => {
  return (
    <Card style={styles.card} onPress={onPress}>
      <Card.Content>
        <Text style={styles.title}>{hike.name}</Text>
        <Text style={styles.subtitle}>{hike.location}</Text>
        <View style={styles.meta}>
          <Text>{hike.date}</Text>
          <DifficultyBadge difficulty={hike.difficulty} />
          <Text>{hike.lengthKm} km</Text>
        </View>
      </Card.Content>
      <Card.Actions>
        <Button onPress={onDelete}>Delete</Button>
      </Card.Actions>
    </Card>
  );
};

const styles = StyleSheet.create({
  card: { marginBottom: 12, marginHorizontal: 16 },
  title: { fontSize: 18, fontWeight: '600', marginBottom: 4 },
  subtitle: { fontSize: 14, color: '#666', marginBottom: 8 },
  meta: { flexDirection: 'row', gap: 8, marginTop: 8 },
});
```

### Comment Guidelines
- Comments explain "why", not "what"
- Comment complex algorithms, workarounds, non-obvious decisions
- Avoid redundant comments

---

## 7. TESTING STRATEGY

### Unit Tests (Jest)
```typescript
// __tests__/validation.test.ts
import { validateHikeInput } from '../utils/validation';

describe('Hike Validation', () => {
  it('should reject empty name', () => {
    const result = validateHikeInput({ name: '', location: 'Test', ... });
    expect(result.errors).toContain('Name is required');
  });

  it('should accept valid input', () => {
    const result = validateHikeInput({ name: 'Test', location: 'Test', ... });
    expect(result.valid).toBe(true);
  });
});
```

### Integration Tests (Detox or E2E)
- Test user workflows: Add hike → View → Search → Delete
- Verify persistence across app restart
- Test error scenarios

### Manual Testing Checklist
- [ ] Add hike → appears in list
- [ ] Edit hike → changes persist
- [ ] Delete hike → removed + observations deleted
- [ ] Add observation → appears under hike
- [ ] Search works (partial match, case-insensitive)
- [ ] Offline functionality (no network required)
- [ ] App survives foreground/background transitions
- [ ] Rotate device → layout adapts
- [ ] Error handling (invalid input, DB errors)

### Performance Testing
- **FlatList**: Render 100+ hikes smoothly (check with PerformanceMonitor)
- **Search**: < 500ms to filter 100 hikes
- **Memory**: No memory leaks (use React Native Performance Monitor)

---

## 8. PLATFORM-SPECIFIC CONSIDERATIONS

### iOS vs. Android Differences
**Handled by React Native automatically**:
- Button styling (iOS: text-based; Android: elevated)
- Navigation back button (iOS: automatic; Android: hardware button)
- DatePicker UI (iOS: wheel picker; Android: dialog picker)

**Manual adjustments needed**:
- Safe area insets (notches, home indicator) → use `useSafeAreaInsets()`
- Keyboard behavior → adjust scroll behavior per platform
- StatusBar color → set via `StatusBar.setBarStyle()`

### Build Configuration (app.json)
```json
{
  "expo": {
    "name": "M-Hike",
    "slug": "mhike",
    "version": "1.0.0",
    "platforms": ["ios", "android"],
    "ios": { "bundleIdentifier": "com.example.mhike" },
    "android": { "package": "com.example.mhike" }
  }
}
```

### Testing on Real Devices
- **Via Expo Go App**: Scan QR code from `expo start`
- **Android**: USB debugging enabled; run `expo start --android`
- **iOS**: iPhone with Expo Go app; LAN connection

---

## 9. DELIVERABLES CHECKLIST

### Code Deliverable
- [ ] Complete React Native source code (TypeScript)
- [ ] `README.md` with:
  - Setup instructions (Node.js, Expo CLI)
  - Database schema explanation
  - How to run app (Expo Go or build)
  - Key architectural decisions
  - Any third-party packages used
- [ ] `package.json` with all dependencies locked
- [ ] `.env` example (if applicable)
- [ ] No hardcoded credentials or API keys

### Cross-Platform Testing
- [ ] Tested on Android device/simulator
- [ ] Tested on iOS device/simulator (if possible)
- [ ] Verified layout adaptation for different screen sizes
- [ ] Performance acceptable on both platforms

### Demo Video (15 minutes)
- [ ] Show project structure & architecture
- [ ] Demonstrate features a-d on both platforms:
  - Add hike (form validation on Android and iOS)
  - View/edit/delete hike
  - Add observations
  - Search functionality
- [ ] Show offline persistence (kill app, restart, data remains)
- [ ] Demonstrate additional feature(s)
- [ ] Walk through key code sections (store, database, screens)
- [ ] Explain design decisions (why Zustand, why this component structure, etc.)

### Report (Same as Android)
- **Section 1 (2%)**: Feature checklist
- **Section 2 (2%)**: Screenshots from iOS and Android
- **Section 3 (4%)**: Reflection on cross-platform development challenges
- **Section 4 (8%)**: Evaluation of app(s) with cross-platform considerations
- **Section 5 (2%)**: Code listings (focus on shared JavaScript/TypeScript)

---

## 10. COMMON PITFALLS TO AVOID

1. **Database Initialization Race Conditions**
   - Always await `initializeDatabase()` before loading hikes
   - Check for initialization flag before operations

2. **UI Not Updating After DB Operations**
   - Remember to update Zustand store after DB changes
   - Don't rely on manual state updates

3. **Memory Leaks from Event Listeners**
   - Unsubscribe from navigation listeners in cleanup functions
   - Cancel async tasks on unmount

4. **FlatList Performance Issues**
   - Provide stable `keyExtractor`
   - Use `useMemo` for derived data
   - Implement `removeClippedSubviews` for long lists

5. **Platform Differences Not Handled**
   - Test thoroughly on both iOS and Android
   - Use Platform.select() for platform-specific code
   - SafeAreaView for notch/home indicator

6. **Ignoring TypeScript Errors**
   - Strict mode enabled = type safety
   - Don't bypass with `any` or `@ts-ignore`
   - Proper types catch bugs early

7. **Poor Error Handling**
   - Wrap DB operations in try-catch
   - Show user-friendly messages
   - Log errors for debugging

8. **Incomplete Features**
   - Finish core features completely before polish
   - Test thoroughly before moving on

---

## 11. DEVELOPMENT WORKFLOW

### Phase 1: Setup & Database (Week 1-2)
- [ ] Expo project initialized
- [ ] SQLite database schema & queries
- [ ] Zustand store structure
- [ ] TypeScript types defined

### Phase 2: Feature A & B (Week 3-4)
- [ ] AddEditHikeScreen UI
- [ ] Form validation logic
- [ ] HikeListScreen with FlatList
- [ ] Edit/delete operations

### Phase 3: Feature C (Week 5)
- [ ] AddObservationScreen
- [ ] ObservationListScreen
- [ ] Link observations to hikes
- [ ] Display observations under hike detail

### Phase 4: Feature D (Week 6)
- [ ] SearchScreen UI
- [ ] Basic search implementation
- [ ] Advanced search filters
- [ ] Test search performance

### Phase 5: Polish & Feature G (Week 7)
- [ ] Cross-platform testing (iOS + Android)
- [ ] Bug fixes
- [ ] UI/UX refinement
- [ ] Implement additional feature(s)
- [ ] Performance optimization

### Phase 6: Demo & Report (Week 8)
- [ ] Build Expo APK/IPA for demo
- [ ] Record 15-minute video demonstration
- [ ] Write comprehensive report
- [ ] Prepare for Q&A

---

## 12. AI AGENT INSTRUCTIONS

When generating supporting documents (architecture specs, UI mockups, code templates, etc.) from this brief:

1. **Follow React Native patterns** — hooks, functional components, TypeScript strict
2. **Provide complete implementations** — not pseudo-code or placeholders
3. **Include error handling** — every async operation wrapped in try-catch
4. **Show platform considerations** — if iOS/Android differences exist, handle them
5. **Reference Material 3** — use specified colors, typography, spacing
6. **Performance-conscious** — avoid unnecessary re-renders, memoize when needed
7. **Accessibility-first** — include accessible props, touch targets, color + icons
8. **TypeScript strict mode** — no `any` types, proper interfaces
9. **Testing mindset** — write code that's testable and debuggable
10. **Link to official resources** — React Native Docs, Expo Docs, React Navigation

---

## 13. REFERENCES & RESOURCES

- React Native Docs: https://reactnative.dev/
- Expo Docs: https://docs.expo.dev/
- React Navigation: https://reactnavigation.org/
- expo-sqlite: https://docs.expo.dev/versions/latest/sdk/sqlite/
- React Native Paper: https://callstack.github.io/react-native-paper/
- Zustand: https://github.com/pmndrs/zustand
- Material 3: https://m3.material.io/
- TypeScript Handbook: https://www.typescriptlang.org/docs/

---

**Document Version**: 1.0  
**Last Updated**: 2025-26 COMP1786 Term 1  
**Prepared for**: M-Hike React Native Development (Coursework Implementation)

---

## APPENDIX: Comparison with Xamarin/MAUI

The coursework specification mentions Xamarin/MAUI for features e-f. This plan substitutes React Native + Expo because:

| Aspect | Xamarin/MAUI | React Native (This Plan) |
|--------|--------------|--------------------------|
| Language | C# | JavaScript/TypeScript |
| Learning Curve | Moderate (C# needed) | Lower (JavaScript known) |
| Dev Experience | Hot Reload (good) | Fast Hot Reload + Expo Go |
| Community | Smaller | Large, active |
| Third-party Packages | Limited | Extensive (maps, camera, etc.) |
| Bundle Size | Larger | Smaller |
| iOS Testing | Requires Mac + Xcode | Possible via Expo Go on device |
| Build Complexity | Requires native builds | Managed by Expo |

**Result**: Same feature parity, faster development, better tooling for coursework context.
