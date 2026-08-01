# M-Hike v3.0 — RESUME / Handoff

> Lưu để tiếp tục phiên sau. Tối chỉ cần mở lại, đọc file này rồi chạy tiếp.

## Mục tiêu hiện tại
Triển khai Features G1–G6 (GPS/map, chụp ảnh, tính thời gian, thời tiết, PDF export/share, chấm điểm trail) lên nền v2.0 đã verify (Room/Firebase/M3). Build phải xanh: `.\gradlew.bat assembleDebug --console=plain -q`.

## ĐANG LÀM DỞ (đúng chỗ này để chạy tiếp)
**Todo 2 — G1 Map** đang `in_progress`. Đã xong Todo 1 (`title_hike_map` string, strings.xml dòng 13). Cần tạo 3 file:

1. `app/src/main/java/com/example/m_hikeapp/HikeMapActivity.java`
2. `app/src/main/res/layout/activity_hike_map.xml`
3. `app/src/main/res/drawable/ic_map_marker.xml` (brand `#386A1F`)

Template đã verify kỹ = `HikeDetailActivity.java` (1-90) + `activity_hike_detail.xml`. Wire theo đúng pattern:
- ViewBinding: `ActivityHikeMapBinding.inflate(getLayoutInflater())`
- `HikeRepository.getInstance(this)`
- `getLongExtra(HikeListActivity.EXTRA_HIKE_ID, -1L)`; nếu `-1L` → toast `R.string.error_invalid_hike` + `finish()` + return
- Toolbar: `setSupportActionBar` + `setDisplayHomeAsUpEnabled(true)`, title `R.string.title_hike_map`, nút back → `finish()`
- `onResume` reload qua `repository.getHikeById(id, callback)` — **guard null** (callback có thể trả null, HikeRepository.java:294)
- `SupportMapFragment` + marker màu `#386A1F`, zoom 14
- Empty state "Location not saved" (plan dòng 422)
- `AndroidManifest.xml` đã khai báo `HikeMapActivity` (97-102) — không cần sửa manifest

## Đã xong (build xanh sau từng bước)
- Room v3 + `MIGRATION_2_3` + `.fallbackToDestructiveMigration()`
- Utils: `DurationCalculator`, `WeatherContract`, `GpsUtils`, `ImageUriUtils`
- `PdfReportBuilder` viết lại (2-pass paginated, A4 595×842pt, MARGIN=48, COLOR_HEADER #386A1F)
- `post-ratingValue` build green (chỉ còn warnings)
- Todo 1: string `title_hike_map` = "Trail Map"

## Bước kế tiếp (sau khi xong Todo 2)
3. Chạy build xanh (todo 3).
4. Tạo `sync/FirebaseSyncHelper.java` theo plan §4 — mirror pattern threading/callback của `HikeRepository`.
5. Tích hợp G1–G6 vào UI: `AddHikeActivity`, `HikeDetailActivity`, `HikeListActivity`, `SearchFilterActivity` + adapters.
6. Build final + sửa đến khi xanh.

## Thông tin quan trọng cần nhớ
- Root package: `com.example.m_hikeapp`; build file là `app/build.gradle.kts` (**.kts**)
- `HikeListActivity.EXTRA_HIKE_ID = "extra_hike_id"` (line 41); guard `-1L` như HikeDetailActivity
- Room = single source of truth; Firebase sync best-effort local→cloud, `user_id` + `is_synced`; mọi Firebase op ngoài main thread; Activity KHÔNG bao giờ đụng DAO/AppDatabase trực tiếp (không `allowMainThreadQueries`)
- Firebase URL: `https://m-hike-android-app-default-rtdb.asia-southeast1.firebasedatabase.app` (HikeRepository.java:131)
- `strings.xml`: 109 dòng; screen titles 7-13; labels 16-18; hints 36-41; actions 47-58
- Drawable: 16 file đã có; **`ic_map_marker.xml` chưa tồn tại** (phải tạo)
- Windows shell: dùng `findstr`/`Select-String`, không có `rg`
- Manifest: G1 perms 5-7 (`ACCESS_FINE/COARSE_LOCATION`), CAMERA 10, API-key meta 26-29, FileProvider 32-40

## Spec nhanh G1–G6
- **G1 GPS**: `FusedLocationProviderClient.getLastLocation()` single fix; nút "Use my location" ở Add/Edit; lat/lng `Double`; validate lat `-90..90`, lon `-180..180`
- **G2 Photo**: `MediaStore.ACTION_IMAGE_CAPTURE` + FileProvider; ảnh vào `getExternalFilesDir(Pictures)`
- **G3 Duration**: `estimated_minutes = length_km * 12 * multiplier`, clamp `[1,1440]`, cap 720
- **G4 Weather**: canonical `Sunny, Partly Cloudy, Cloudy, Overcast, Rain, Snow, Wind, Fog, Storm`; notes max 500
- **G5 PDF**: PdfDocument → `getCacheDir()/reports/<hikeId>.pdf` → FileProvider `cache-path` `hike_reports`; `ACTION_SEND` + `EXTRA_STREAM` + `FLAG_GRANT_READ_URI_PERMISSION`
- **G6 Rating**: `RatingBar` stepSize=1.0 integer 1..5 lưu vào `getCustomField1()`; `pdf_rating_format "%1$d / 5"`

## Lệnh build
```
.\gradlew.bat assembleDebug --console=plain -q
```
