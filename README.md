# NEXUS Messenger

Android messenger starter built with Kotlin, Jetpack Compose, Firebase Authentication, and Cloud Firestore.

- Compose sign-in and registration screens
- Username profile creation and username lookup
- One-to-one chat creation and Firestore message listeners
- Local demo mode without Firebase
- GitHub Actions workflow to build and upload a debug APK

## Online setup
Follow [FIREBASE_SETUP.md](FIREBASE_SETUP.md) and add your own `app/google-services.json`. Without Firebase configuration, demo users and messages are local to the device and are not shared.

## Build
With JDK 17 and Gradle installed: `gradle assembleDebug`.
APK output: `app/build/outputs/apk/debug/app-debug.apk`.
