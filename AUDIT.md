# M-Hike Hybrid Architecture Audit

Audit of the M-Hike Android app (`com.example.m_hikeapp`) against the hybrid-architecture
spec in `IMPLEMENT.md` (Room offline cache + Firebase Auth + Realtime Database sync).
Focus: user isolation and sync correctness.

- Date: 2026-08-01
- Build: `.\gradlew.bat assembleDebug` passes (Room compile-time query checks clean;
  `app-debug.apk` 8.78 MB produced).

---

## Scope verified as conforming

- Room is the offline-first local cache; `HikeRepository` is the single data gateway
  (no activity touches `HikeDao`/`AppDatabase` directly).
- Single-threaded `ExecutorService` + `Handler(mainLooper)`; no `allowMainThreadQueries()`.
- All `OperationCallback`/`HikeListCallback`/`HikeCallback` results delivered on the main thread.
- ViewBinding enabled; minSdk 26 / targetSdk 36; Room schema v2 with
  `.fallbackToDestructiveMigration()`.
- `google-services.json`: `package_name` matches the appId
  (`com.example.m_hikeapp`); project `m-hike-android-app`;
  RTDB `https://m-hike-android-app-default-rtdb.asia-southeast1.firebasedatabase.app`.
- Dynamic multi-criteria filter uses parameterised `SimpleSQLiteQuery` (no string
  concatenation into SQL).

---

## Findings

### 1. HIGH — Signed-out fallback `"anonymous"` breaks the user-isolation contract

- Location: `HikeRepository.getCurrentUserId()` (`HikeRepository.java:119-124`),
  used by `getFirebaseRef()` (`:126-132`).
- Problem: when no Firebase user is signed in, the repository returns `"anonymous"`,
  so sync addresses `/users/anonymous/hikes`. The documented RTDB rules
  (`IMPLEMENT.md:55-56`, `$uid === auth.uid`) reject these writes, so cloud sync
  silently fails. If the rules are ever relaxed, every signed-out user shares one
  node → cross-user data mixing.
- Mitigation in practice: `HikeListActivity` redirects to `LoginActivity` when
  signed out, so the app flow keeps a user signed in. The repository-level fallback
  remains a latent isolation hole.

### 2. HIGH — Unsynced retry writes other users' rows under the current user's node

- Location: `HikeRepository.getAllHikes()` (`:262-275`) + `syncHikeToFirebase()`
  (`:134-146`) + `HikeDao.getUnsynced()`.
- Problem: `getAllHikes` displays `hikeDao.getByUser(currentUserId)` but retries
  **all** `hikeDao.getUnsynced()` rows, which are not user-scoped in the DAO. Each
  retry writes via `getFirebaseRef()` = the *current* user's path. On a shared
  device, user B signing in can push user A's unsynced rows into B's Firebase node
  → cross-user contamination in the cloud.

### 3. MEDIUM — `deleteAllHikes` wipes other users' local data

- Location: `HikeRepository.deleteAllHikes()` (`:240-250`).
- Problem: `hikeDao.deleteAll()` removes every Room row for any `user_id`, but only
  the *current* user's Firebase node is removed. On a shared device, "delete all"
  as user B permanently destroys user A's local-only unsynced rows while leaving
  A's cloud copy orphaned — asymmetric delete.

### 4. LOW — Numeric local IDs used as Firebase keys (spec deviation)

- Location: `Hike.id` = `@PrimaryKey(autoGenerate = true) long`; Firebase child key
  = `String.valueOf(hike.getId())` (`HikeRepository.java:136,225`).
- Spec: `IMPLEMENT.md:104` recommends UUID string keys so local IDs match cloud keys.
- Problem: with a reinstall or `.fallbackToDestructiveMigration()`, autoincrement
  resets to 1 → `setValue(1)` overwrites the previous hike at cloud key `"1"` for
  that user, losing the prior node and corrupting the pairing.

---

## Fix status (2026-08-01)

1. **APPLIED** — Unsynced retry scoped to the current user:
   `HikeDao.getUnsyncedByUser(userId)` added; `getAllHikes` retries
   `hikeDao.getUnsyncedByUser(currentUserId)` instead of `getUnsynced()`.
2. **APPLIED** — Delete-all scoped to the current user:
   `HikeDao.deleteByUser(userId)` added; `deleteAllHikes` uses
   `hikeDao.deleteByUser(getCurrentUserId())` and null-checks the Firebase ref.
3. **APPLIED** — `"anonymous"` fallback removed: `getCurrentUserId()` returns
   `null` when signed out; `getFirebaseRef()` returns `null` when the userId is
   null; `syncHikeToFirebase` and `deleteHike` null-guard the ref.
4. **SKIPPED (by decision)** — Multi-device sync not in scope; numeric IDs remain
   as Firebase child keys.

Verified: `.\gradlew.bat assembleDebug` passes after the changes (Room
compile-time checks the new `@Query` methods).
