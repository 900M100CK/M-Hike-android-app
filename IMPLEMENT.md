# M-Hike Application - Technical Implementation Specification

This document details the architecture and features of the M-Hike Android application, intended as a reference for re-implementing the app in **React Native**.

## 1. Project Overview
M-Hike is a mobile application for hikers to record their journeys, capture observations (weather, wildlife, trail conditions), and track their real-time path on a map.

## 2. Technical Stack (Android)
- **Language**: Java
- **UI Framework**: XML Layouts + Material Design 3
- **Local Database**: Room (SQLite)
- **Cloud Database**: Firebase Realtime Database
- **Authentication**: Firebase Auth
- **Maps**: Mapbox Maps SDK for Android v11.27.0
- **Location**: Mapbox Common Location Service + Google Play Services Location

## 3. Data Models

### Hike Entity
| Field | Type | Description |
|---|---|---|
| id | long (PK) | Auto-generated local ID |
| name | String | Name of the hike (Required) |
| location | String | General location name (Required) |
| date | String | Hike date (YYYY-MM-DD) |
| parkingAvailable | boolean | Flag for parking availability |
| lengthKm | double | Total distance in kilometers |
| difficulty | String | Easy, Moderate, Hard, Expert |
| description | String | Optional user notes |
| latitude / longitude | Double | Trailhead GPS coordinates |
| userId | String | Firebase UID for data ownership |
| isSynced | boolean | Local sync status with Firebase |

### Observation Entity
| Field | Type | Description |
|---|---|---|
| id | long (PK) | Auto-generated local ID |
| hikeId | long (FK) | Reference to parent Hike |
| title | String | Observation name (e.g., "Heavy Rain") |
| time | String | Time of observation (HH:mm) |
| comments | String | Optional additional details |

## 4. Feature Set & Business Logic

### A. Authentication
- Email/Password Sign-in and Sign-up via Firebase.
- Persistent session: App redirects to Login if user is not authenticated.

### B. Hike Management
- **List View**: RecyclerView showing all hikes for the current user.
- **Search**: Real-time filtering by hike name.
- **Advanced Filter**: Filter by location, date range, and distance.
- **Confirmation**: Show summary dialog before saving a new hike.

### C. Map & GPS (Feature G1)
- **Official Mapbox v11**: Using official "Outdoors" style.
- **Trailhead Display**: Dropping a marker at the saved coordinates.
- **Tap-to-Set**: User can tap anywhere on the map to update the hike's trailhead position.
- **My Location**: Button to center map on device's current location.
- **Trail Tracking**:
    - Uses Mapbox Location Service (Highest accuracy).
    - Draws a blue **Polyline** as the user moves.
    - Uses Mapbox **Viewport Plugin** to keep the user in center.
- **Search Bar**: Uses Nominatim API to find places and jump to them.
- **Offline Maps**: Uses Mapbox `OfflineManager` and `TileStore` to download specific hike regions for use without internet.

### D. Cloud Synchronization
- Automatic push to Firebase Realtime Database when a hike is added/edited.
- Background sync for local changes made while offline.

## 5. Suggested React Native Stack
- **Framework**: Expo or React Native CLI
- **Navigation**: `@react-navigation/native` + `stack`
- **Maps**: `@rnmapbox/maps` (Official recommendation for Mapbox)
- **Local DB**: `react-native-sqlite-storage` or `expo-sqlite`
- **Location**: `expo-location` or `react-native-geolocation-service`
- **Networking**: `axios` or `fetch` (for Nominatim API)
- **State Management**: `Context API` or `Redux Toolkit`

## 6. Implementation Notes for Agent
1. **Permissions**: Request `ACCESS_FINE_LOCATION` and `INTERNET` at startup.
2. **Lifecycle**: Ensure MapView is paused/resumed correctly to save battery.
3. **Mapbox Setup**: Requires a Mapbox account and public/secret tokens.
4. **Nominatim**: Add a custom `User-Agent` header to search requests to avoid being blocked.
