# Findly Android App

This is the source code for the Findly Android application.

## Getting Started

To get this project up and running with your own Firebase backend, please follow these steps:

### 1. Clone the Repository

Start by cloning the project to your local machine:

```bash
git clone [https://github.com/YOUR_USERNAME/findly.git](https://github.com/YOUR_USERNAME/findly.git)
cd findly
````

### 2\. Set Up Your Firebase Project

This app uses Firebase for its backend services (Realtime Database and Storage). You'll need to create your own Firebase project to connect the app to:

  - **a.** Go to the [Firebase Console](https://console.firebase.google.com/).

  - **b.** Click "Add project" and follow the prompts to create a new Firebase project.

  - **c.** Once your project is created, click the Android icon to add an Android app to your Firebase project.

  - **d.** **Register your app:**

      * **Android package name:** Use `com.example.findly` (or whatever your app's `applicationId` is defined as in your `app/build.gradle` file).
      * **App nickname:** (Optional, e.g., "Findly Dev Instance")
      * **SHA-1 signing certificate fingerprint:** (Optional, but highly recommended if you plan to use Firebase Authentication methods like Google Sign-In or Phone Auth. You can add this later. Find instructions to get your SHA-1 here: https://www.google.com/search?q=https://firebase.google.com/docs/android/setup%23sha-1-fingerprint).

  - **e.** **Download the `google-services.json` file** when prompted.

  - **f.** Place this downloaded `google-services.json` file directly into your Android project's **`app/`** directory:

    ```
    findly/
    ├── app/
    │   └── google-services.json  <-- Place your downloaded file here
    └── ...
    ```

### 3\. Configure Firebase Security Rules

To ensure your app functions correctly and securely, you **must** configure your Firebase Realtime Database and Cloud Storage security rules in your Firebase Console. These rules define who can read and write data to your backend.

#### For Realtime Database:

Go to your Firebase project, then navigate to **"Realtime Database"** -\> **"Rules"**. Here's a basic example suitable for authenticated users to manage their own data:

```json
{
  "rules": {
    "users": {
      "$userId": {
        ".read": "auth != null && auth.uid == $userId",
        ".write": "auth != null && auth.uid == $userId"
      }
    },
    // Add other database paths and their specific rules as needed by your app.
    // For example, if you have a 'public_items' node that anyone can read:
    // "public_items": {
    //   ".read": "true",
    //   ".write": "auth != null" // Only authenticated users can write
    // },
    // A general rule to deny access to undeclared paths by default
    ".read": "false",
    ".write": "false"
  }
}
```

#### For Cloud Storage:

Go to your Firebase project, then navigate to **"Storage"** -\> **"Rules"**. For example, if your app stores profile pictures in a `profile_pictures` folder, you might set rules like this:

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /profile_pictures/{userId}/{fileName} {
      // Allow authenticated users to upload their own profile picture
      allow write: if request.auth != null && request.auth.uid == userId;
      // Allow anyone to read profile pictures publicly
      allow read;
    }
    // Deny all other read/write access to undeclared paths by default
    match /{allPaths=**} {
      allow read, write: if false;
    }
  }
}
```

**Important:** These are example rules. **Adjust them precisely** to your data structure and the specific security requirements of your app. Improperly configured rules can expose your data\!

### 4\. Build and Run the Project

After placing your `google-services.json` and configuring your Firebase security rules, open the project in Android Studio. You should now be able to build and run the application successfully.

```bash
# You can build the project from your terminal as well
./gradlew build
```