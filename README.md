# NEXUS Messenger

An Android messaging app project built with Kotlin and Jetpack Compose.

## Planned technology

- Kotlin and Jetpack Compose for the Android application
- Firebase Authentication for account registration and sign-in
- Cloud Firestore for user profiles, conversations, and messages
- GitHub Actions for automated Android builds

## Project status

The repository is being set up. Android source files, Firebase configuration, security rules, and the build workflow still need to be added.

Never commit private keys, service-account files, or other secrets. Configure Firebase with your own project credentials before running the app.

## Build

After the Android Gradle project is added, build a debug APK with:

```bash
./gradlew assembleDebug
```
