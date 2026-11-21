# Firebase setup

For real accounts and cross-device messaging:

1. Create a Firebase project.
2. Register an Android app with package name `com.nexus.messenger`.
3. Download `google-services.json` into `app/google-services.json`.
4. Enable Email/Password in Firebase Authentication.
5. Create a Cloud Firestore database.
6. Publish restrictive Firestore rules so only authenticated chat participants can read/write messages.

The Google Services plugin is applied only when the JSON file exists. Without it, the app builds in local demo mode; demo messages are not shared across devices.

Never commit service-account private keys. Treat google-services.json as project configuration and review Firebase security rules before production use.
