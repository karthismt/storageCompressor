# Storage Doctor

Save storage safely with verified compression.

## Overview

Storage Doctor is a smart Android storage optimization app that safely compresses old photos and videos in the background to save device storage without data loss.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Database:** Room
- **Background:** WorkManager
- **Image Loading:** Coil
- **Architecture:** MVVM

## Project Structure

```
com.storagedoctor/
├── ui/
│   ├── screens/        # Compose screens
│   ├── components/     # Reusable UI components
│   ├── navigation/     # Navigation setup
│   └── theme/          # Colors, Typography, Theme
├── worker/             # WorkManager workers
├── compression/        # Image compression engine
├── database/           # Room database, entities, DAOs
├── repository/         # Data repository layer
├── scheduler/          # WorkManager scheduling
└── util/               # Utility functions
```

## Build & Run

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- Kotlin 1.9.22

### Steps

1. Open the project in Android Studio:
   ```
   File → Open → Select C:\karthik\StorageDoctor
   ```

2. Sync Gradle (Android Studio will prompt automatically)

3. Run on device/emulator:
   - Select a device with API 26+ (Android 8.0+)
   - Click Run (▶)

### Build APK

```bash
./gradlew assembleDebug
```

APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK

```bash
./gradlew assembleRelease
```

## MVP Features (v1.0)

- [x] Storage scan via MediaStore
- [x] JPG → WebP compression
- [x] PNG → WebP Lossless compression
- [x] Safe replacement workflow (backup → compress → verify → replace)
- [x] Batch compression with pause/resume
- [x] Background scheduling (WorkManager)
- [x] Compression reports
- [x] Restore original files
- [x] Configurable media age threshold
- [x] Configurable backup retention

## Compression Safety

1. Read original file
2. Create temporary compressed file
3. Verify compressed file (dimensions, size, opens correctly)
4. Replace original only after success
5. Keep restore backup for configurable days

## Background Constraints

Compression only runs when:
- Phone is charging
- Battery > 40%
- Device is idle

## Permissions

- `READ_MEDIA_IMAGES` — Scan photos
- `MANAGE_EXTERNAL_STORAGE` — Replace files in-place
- `FOREGROUND_SERVICE` — Long-running compression
- `POST_NOTIFICATIONS` — Progress updates
- `RECEIVE_BOOT_COMPLETED` — Reschedule after reboot
