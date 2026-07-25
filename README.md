# 📞 WebRTC Solution

A plug-and-play **WebRTC Voice & Video Calling Library** for Android built with **Jetpack Compose**, **Firebase**, **Koin**, and **Google WebRTC**.

This library eliminates the complexity of integrating WebRTC from scratch. Simply configure Firebase, add the dependency, register your users, and start making high-quality voice and video calls.

---

## ✨ Features

* 📹 High-quality Video Calling
* 📞 Crystal Clear Voice Calling
* ⚡ Firebase Realtime Database Signaling
* 🔥 Firebase Cloud Firestore Integration
* 🎨 Built entirely with Jetpack Compose
* 📲 Beautiful Incoming Call UI
* 📱 Active Call Screen Included
* 🔔 Background Incoming Call Support
* 🔒 Lock Screen Incoming Call Support
* 🎤 Mute / Unmute
* 🔄 Camera Switching
* 🔊 Speaker Switching
* ☁️ Firebase Cloud Messaging Support
* 🧩 Koin Dependency Injection
* 📦 Google WebRTC Included
* 🚀 Easy Integration
* 🆓 Completely Free

---

# Requirements

| Requirement | Version    |
| ----------- | ---------- |
| Min SDK     | 24         |
| Compile SDK | 37         |
| Target SDK  | 37         |
| Kotlin      | 2.4.10     |
| Compose BOM | 2026.06.01 |
| Koin        | 4.2.2      |

---

# Integration Flow

The recommended integration flow is:

```
1. Configure Firebase
        ↓
2. Add Dependency
        ↓
3. Create Koin Module
        ↓
4. Start Koin
        ↓
5. Inject CallManager
        ↓
6. Register User
        ↓
7. Handle Notification Intents
        ↓
8. Add WebRtcCallHandler
        ↓
9. Start Calling 🚀
```

---

# Step 1 - Configure Firebase

Create a Firebase project and enable the following services:

* Firebase Cloud Messaging (FCM)
* Realtime Database
* Cloud Firestore

Download

```
google-services.json
```

and place it inside

```
app/
```

Next, create a Firebase Service Account.

Download

```
service_account.json
```

Place it inside

```
app/src/main/res/raw/
```

Example

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
> The sample project uses open Firebase Security Rules (`true`) for demonstration purposes. Configure secure rules before deploying your application to production.

---

# Step 2 - Add the Library

Add **JitPack** to your project.

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

Now add the dependency.

```kotlin
dependencies {
    implementation("com.github.ronil-gwalani:web-rtc-solution:v1.0.1")
}
```

The library already bundles all required dependencies including Google WebRTC, Firebase, Koin, and Compose integrations.

No additional WebRTC dependency is required.

---

# Step 3 - Create the Koin Module

Create an application module.

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

This allows the library to access your Firebase Service Account securely.

---

# Step 4 - Start Koin

Create your `Application` class.

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

# Step 5 - Register the Application Class

Update your `AndroidManifest.xml`

```xml
<application
    android:name=".MainApplication"
    android:allowBackup="true"
    ...
/>
```

---

# Step 6 - Inject the CallManager

`CallManager` is the main entry point of the library.

Inject it using Koin.

### Activity

```kotlin
private val callManager: CallManager by inject()
```

### Fragment

```kotlin
private val callManager: CallManager by inject()
```

### Compose

```kotlin
val callManager: CallManager = koinInject()
```

### ViewModel Constructor

```kotlin
class HomeViewModel(
    private val callManager: CallManager
) : ViewModel()
```

Once injected, the `CallManager` provides everything needed to interact with the library.

* User Registration
* Voice Calls
* Video Calls
* End Call
* Notification Handling
* User Information

---

# Step 7 - Handle Notification Intents

To properly handle incoming calls launched from notifications, call:

```kotlin
callManager.handleIntent(intent)
```

inside both

* `onCreate()`
* `onNewIntent()`

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

# Step 8 - Register a User

Every user must register once before making or receiving calls.

```kotlin
callManager.registerUser(
    userId,
    userName
)
```

Observe registration state

```kotlin
callManager.isRegistered
```

Retrieve current user information

```kotlin
callManager.userId

callManager.userName
```

Example

```kotlin
val isRegistered by callManager.isRegistered.collectAsState(initial = null)

if (isRegistered == false) {

    callManager.registerUser(
        userId,
        userName
    )

}
```

---

# Step 9 - Add the Call Handler

Place `WebRtcCallHandler` near the root of your Compose hierarchy.

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

The library automatically manages:

* Incoming Call Screen
* Active Call Screen
* Voice Calls
* Video Calls
* Call Notifications

---

# Step 10 - Start Calling

After registration, users can call each other using their unique user ID.

A typical implementation looks like this.

```kotlin
class HomeViewModel(
    private val callManager: CallManager
) : ViewModel() {

    var targetId by mutableStateOf("")

    private var _isStartingCall by mutableStateOf(false)
    val isStartingCall = _isStartingCall

    fun startCall(
        targetUserId: String,
        isAudioOnly: Boolean,
    ) {

        _isStartingCall = true

        viewModelScope.launch {

            _isStartingCall = false

            callManager.startCall(
                targetUserId,
                isAudioOnly
            )
                .onSuccess { roomId ->

                    callManager.startCallUI(
                        roomId,
                        isAudioOnly
                    )
                }
        }
    }
}
```

---

## Start a Video Call

```kotlin
startCall(
    targetUserId = "john123",
    isAudioOnly = false
)
```

---

## Start a Voice Call

```kotlin
startCall(
    targetUserId = "john123",
    isAudioOnly = true
)
```

---

## Parameters

| Parameter      | Description                                        |
| -------------- | -------------------------------------------------- |
| `targetUserId` | Unique ID of the registered user you want to call. |
| `isAudioOnly`  | `true` for Voice Call, `false` for Video Call.     |

---

# End a Call

If you need to terminate the call manually based on your application logic, simply call

```kotlin
callManager.endCall()
```

---

# Sample Home Screen

After successful registration, display your home screen where users can enter another user's unique ID and initiate a voice or video call.

```kotlin
HomeScreen(viewModel)
```

A complete implementation is included in the sample application.

---

# Permissions

The library already includes all required WebRTC dependencies.

The host application should request notification permission (`POST_NOTIFICATIONS`) on Android 13+ according to its own UX flow.

---

# Current Features

| Feature                    | Status |
| -------------------------- | ------ |
| Voice Calling              | ✅      |
| Video Calling              | ✅      |
| Background Incoming Calls  | ✅      |
| Lock Screen Incoming Calls | ✅      |
| Camera Switching           | ✅      |
| Speaker Switching          | ✅      |
| Mute / Unmute              | ✅      |
| Manual End Call            | ✅      |
| Firebase Signaling         | ✅      |
| Jetpack Compose UI         | ✅      |
| Firebase Notifications     | ✅      |
| Koin Integration           | ✅      |

---

# Example Project

A fully working sample application is included in this repository.

It demonstrates:

* Firebase Setup
* User Registration
* Home Screen
* Voice Calling
* Video Calling
* Incoming Calls
* Notifications
* Call Lifecycle

---

# Troubleshooting

## Incoming Calls Not Working

* Verify Firebase Cloud Messaging is enabled.
* Verify `service_account.json` is inside `res/raw`.
* Ensure notification permission is granted.
* Verify `callManager.handleIntent(intent)` is called in both `onCreate()` and `onNewIntent()`.

---

## Registration Failed

* Verify Firebase configuration.
* Verify Realtime Database is enabled.
* Verify Firestore is enabled.
* Verify Firebase Security Rules.

---

## Calls Not Connecting

* Verify both users are registered.
* Verify the target user exists.
* Check internet connectivity.
* Verify Firebase services are correctly configured.

---

# Roadmap

Upcoming releases will include:

* 🎨 Fully Customizable Call UI
* 🌙 Theme Customization
* 🎵 Custom Ringtones
* 📜 Call History API
* 📶 Connection State Callbacks
* 📊 Network Quality Monitoring
* 👤 User Presence
* 🚫 Busy Status
* 👥 Group Calling

---

# License

This project is licensed under the MIT License.

---

# Support

If this library helped you, please consider giving the repository a ⭐ on GitHub.

It helps others discover the project and motivates future development.
