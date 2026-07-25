# WebRTC Solution

A plug-and-play **WebRTC Voice & Video Calling Library** for Android built with **Jetpack Compose**, **Firebase**, **Koin**, and **Google WebRTC**.

Stop spending weeks integrating WebRTC. Add a dependency, connect Firebase, register users, and you're ready to make voice and video calls.

---

## ✨ Features

- 📹 High-quality Video Calling
- 📞 Crystal Clear Voice Calling
- ⚡ Firebase Realtime Signaling
- 🎨 Beautiful Jetpack Compose UI
- 📲 Incoming Call Screen
- 🔔 Background Incoming Call Support
- 🔒 Lock Screen Incoming Calls
- 🎤 Mute / Unmute
- 🔄 Camera Switching
- 🔊 Speaker Switching
- ☁️ Firebase Cloud Messaging Support
- 🧩 Koin Dependency Injection
- 📦 Google WebRTC Included
- 🆓 Completely Free

---

## Requirements

| Requirement | Version |
|-------------|---------|
| Min SDK | 24 |
| Compile SDK | 37 |
| Target SDK | 37 |
| Kotlin | 2.4.10 |
| Compose BOM | 2026.06.01 |
| Koin | 4.2.2 |

---

# Installation

## Step 1

Add JitPack.

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

---

## Step 2

Add the dependency.

```kotlin
dependencies {
    implementation("com.github.ronil-gwalani:web-rtc-solution:v1.0.1")
}
```

---

# Firebase Setup

Before using this library you must create a Firebase project.

Enable

- Firebase Cloud Messaging
- Realtime Database
- Cloud Firestore

Download

```
google-services.json
```

and place it inside

```
app/
```

Next, create a Firebase Service Account and download

```
service_account.json
```

Place it inside

```
app/src/main/res/raw/
```

Example:

```
app
 └── src
      └── main
           └── res
                └── raw
                     └── service_account.json
```

> **Note**
>
> The example project uses open Firebase rules (`true`) for simplicity. For production applications, configure secure Firebase Security Rules according to your requirements.

---

# Koin Setup

Create your application module.

```kotlin
val appModule = module {

    single<SecretInputStream> {
        SecretInputStream(
            get(),
            R.raw.service_account
        )
    }

}
```

---

# Start Koin

Create your Application class.

```kotlin
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(
            AppVisibilityTracker
        )

        startKoin {

            androidLogger()

            androidContext(this@MainApplication)

            modules(
                libraryModule,
                appModule
            )
        }
    }
}
```

---

# AndroidManifest

Register your Application class.

```xml
<application
    android:name=".MainApplication"
    ...
/>
```

---

# Handle Notification Intents

Inside your `MainActivity`, call

```kotlin
callManager.handleIntent(intent)
```

inside

```kotlin
onCreate()
```

and

```kotlin
onNewIntent()
```

Example

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    callManager.handleIntent(intent)
}

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)

    callManager.handleIntent(intent)
}
```

---

# Register User

Before making calls every user must register once.

```kotlin
callManager.registerUser(
    userId,
    userName
)
```

You can observe registration state using

```kotlin
callManager.isRegistered
```

---

# Access Current User

The currently registered user information is available as

```kotlin
callManager.userId

callManager.userName
```

---

# Making Calls

Start a Video Call

```kotlin
callManager.startCall(
    targetId,
    false
)
```

Start a Voice Call

```kotlin
callManager.startCall(
    targetId,
    true
)
```

Once the room is created, launch the call UI.

```kotlin
callManager.startCallUI(
    roomId,
    isAudioOnly
)
```

---

# End Call

If you want to manually terminate the call

```kotlin
callManager.endCall()
```

---

# Incoming Calls

Simply place

```kotlin
WebRtcCallHandler()
```

inside your root Compose screen.

Example

```kotlin
WebRtcCallHandler(
    onDecline = {

    },
    onAnswer = {

    },
    onCallEnded = {

    }
)
```

The library automatically handles

- Incoming call UI
- Active call screen
- Call notifications
- Voice calls
- Video calls

---

# Home Screen Example

After successful registration, users can enter another user's unique ID and start either a voice or video call.

```kotlin
HomeScreen(viewModel)
```

The sample implementation included in this repository demonstrates the complete flow.

---

# Permissions

The library already includes all required WebRTC dependencies.

For notification permission (Android 13+), it is recommended that the host application requests notification permission according to its own UX flow.

---

# Included Dependencies

This library already bundles all required dependencies including

- Google WebRTC
- Firebase
- Koin
- Jetpack Compose integrations
- Lifecycle components

No additional WebRTC dependency is required.

---

# Current Features

| Feature | Supported |
|-----------|-----------|
| Voice Call | ✅ |
| Video Call | ✅ |
| Background Incoming Call | ✅ |
| Lock Screen Support | ✅ |
| Camera Switch | ✅ |
| Speaker Switch | ✅ |
| Mute | ✅ |
| Manual End Call | ✅ |
| Compose UI | ✅ |
| Firebase Signaling | ✅ |

---

# Roadmap

Upcoming releases will include

- Custom Call UI
- UI Theme Customization
- Custom Incoming Call Screen
- Custom Ringtones
- Call History
- Connection State Callbacks
- Network Quality Monitoring
- Presence API
- Busy Status
- Group Calling

---

# Example Project

A complete sample application demonstrating the full integration is included in this repository.

It covers

- Firebase setup
- User registration
- Voice calling
- Video calling
- Incoming calls
- Notifications

---

# Troubleshooting

## Incoming calls not working

- Verify Firebase Cloud Messaging is enabled.
- Verify `service_account.json` is placed inside `res/raw`.
- Ensure notification permission is granted.
- Confirm `callManager.handleIntent(intent)` is called in both `onCreate()` and `onNewIntent()`.

---

## User registration fails

- Verify Firebase configuration.
- Verify Firestore and Realtime Database are enabled.
- Check Firebase Security Rules.

---

## Call not connecting

- Verify both users are registered.
- Ensure the target user ID exists.
- Check internet connectivity.

---

# License

Licensed under the MIT License.

---

## ⭐ Support

If this project helps you, consider giving it a ⭐ on GitHub. It helps others discover the library and motivates future development.
