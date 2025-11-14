# Simple Ding Timer App

A minimal Android app that plays a short "ding" sound at a time you specify. No need to interact with it when it goes off - it plays for 2-3 seconds and stops automatically.

## Features

- Simple time picker interface
- Set a ding sound for a specific time
- Sound plays automatically and stops without user interaction
- Cancel scheduled dings
- Uses device's default notification sound (pleasant chime)

## How to Build APK

### Method 1: Using Android Studio (Recommended)

1. Open Android Studio
2. Click "Open" and select this project folder
3. Wait for Gradle sync to complete
4. Connect your Android phone via USB with USB Debugging enabled OR use an emulator
5. Click the green "Run" button (or press Shift+F10)
6. To create an APK for distribution:
   - Go to `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
   - Find the APK in `app/build/outputs/apk/debug/app-debug.apk`

### Method 2: Using Command Line

```bash
# In the project directory
./gradlew assembleDebug

# Find your APK at:
# app/build/outputs/apk/debug/app-debug.apk
```

## How to Install on Your Phone

1. Copy the `app-debug.apk` file to your phone
2. On your phone, enable "Install from Unknown Sources" in Settings → Security
3. Use a file manager to find and tap the APK file
4. Follow the installation prompts

## How to Use

1. Open the app
2. Use the time picker to select your desired ding time
3. Tap "Set Ding"
4. On Android 12+, you'll be asked to allow exact alarm permission (one-time setup)
5. The app will show a confirmation message
6. At the specified time, you'll hear a ding sound (2-3 seconds)
7. To cancel a scheduled ding, tap "Cancel"

## Technical Notes

- Minimum Android version: Android 7.0 (API 24)
- Uses AlarmManager for precise timing
- Sound plays even if phone is locked (requires WAKE_LOCK permission)
- Uses MediaPlayer with device's default notification sound
- No background music or persistent notification - just a simple ding!

## Permissions Required

- `SCHEDULE_EXACT_ALARM`: To trigger at exact specified time
- `USE_EXACT_ALARM`: For precise alarm scheduling
- `WAKE_LOCK`: To play sound even when phone is in sleep mode

