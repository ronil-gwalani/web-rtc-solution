# 📞 WebRTC Solution

> A plug-and-play **WebRTC Voice & Video Calling Library** for Android built with **Jetpack Compose**, **Firebase**, **Koin**, and **Google WebRTC**.

Easily integrate **high-quality Voice & Video Calling** into your Android application without dealing with the complexities of WebRTC signaling, notifications, call UI, and lifecycle management.

Simply configure Firebase, add the dependency, register users, and start calling.

---

## ✨ Features

* 📹 High Quality Video Calling
* 📞 Crystal Clear Voice Calling
* ⚡ Firebase Realtime Database Signaling
* 🔥 Firebase Cloud Firestore Integration
* ☁️ Firebase Cloud Messaging (FCM)
* 🎨 Beautiful Jetpack Compose UI
* 📲 Incoming Call Screen
* 📱 Active Call Screen
* 🔔 Background Incoming Calls
* 🔒 Lock Screen Incoming Calls
* 🎤 Mute / Unmute
* 🔄 Camera Switching
* 🔊 Speaker Switching
* 🧩 Koin Dependency Injection
* 📦 Google WebRTC Included
* 🚀 Easy Integration
* 🆓 Completely Free

---

# 📋 Requirements

| Requirement | Version    |
| ----------- | ---------- |
| Min SDK     | 24         |
| Compile SDK | 37         |
| Target SDK  | 37         |
| Kotlin      | 2.4.10     |
| Compose BOM | 2026.06.01 |
| Koin        | 4.2.2      |

---

# 🚀 Integration Flow

```text
Configure Firebase
        ↓
Add Library
        ↓
Configure Gradle
        ↓
Create Koin Module
        ↓
Start Koin
        ↓
Configure Activity
        ↓
Inject CallManager
        ↓
Register User
        ↓
Handle Notification Intents
        ↓
Add WebRtcCallHandler
        ↓
Start Calling 🚀
```

---

# 📦 Step 1 - Configure Firebase

Create a Firebase Project.

Enable the following services:

* Firebase Cloud Messaging
* Firebase Realtime Database
* Cloud Firestore

Download

```text
google-services.json
```

Place it inside

```text
app/
```

---

## Create a Firebase Service Account

Go to

```
Firebase Console
    ↓
Project Settings
    ↓
Service Accounts
    ↓
Generate New Private Key
```

Download

```text
service_account.json
```

Place it inside

```text
app/src/main/res/raw/
```

Directory structure:

```text
app
└── src
    └── main
        └── res
            └── raw
                └── service_account.json
```

> **Note**
>
> The sample project uses open Firebase Security Rules (`true`) for demonstration purposes. Configure proper Firebase Security Rules before using the library in production.

---

# 📥 Step 2 - Add the Library

## Add JitPack

```kotlin
dependencyResolutionManagement {

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}
```

---

## Add Dependency

```kotlin
dependencies {

    implementation("com.github.ronil-gwalani:web-rtc-solution:v1.0.1")

}
```

The library already bundles:

* Google WebRTC
* Firebase
* Jetpack Compose
* Koin
* Lifecycle Components

No additional WebRTC dependency is required.

---

# ⚙️ Step 3 - Configure Gradle

Inside your app module's `android {}` block add:

```kotlin
android {

    packaging {

        resources {

            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"

        }
    }

}
```

This prevents duplicate META-INF resource conflicts during compilation.

---

# 🧩 Step 4 - Create Koin Module

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

---

# 🚀 Step 5 - Start Koin

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

# 📄 Step 6 - Register Application Class

Inside AndroidManifest.xml

```xml
<application
    android:name=".MainApplication"
    android:allowBackup="true"
    ...
/>
```

---

# 📱 Step 7 - Configure MainActivity

Update your launcher activity.

```xml
<activity
    android:name=".MainActivity"
    android:launchMode="singleTop"
    android:showOnLockScreen="true"
    android:showForAllUsers="true" />
```

### Why?

* Prevents multiple Activity instances.
* Properly handles notification clicks.
* Allows incoming call screen on lock screen.
* Ensures call UI behaves correctly.

---

# 💉 Step 8 - Inject CallManager

`CallManager` is the main entry point of the library.

Inject it anywhere using Koin.

### Activity

```kotlin
private val callManager: CallManager by inject()
```

---

### Fragment

```kotlin
private val callManager: CallManager by inject()
```

---

### Compose

```kotlin
val callManager: CallManager = koinInject()
```

---

### ViewModel

```kotlin
class HomeViewModel(
    private val callManager: CallManager
) : ViewModel()
```

`CallManager` provides:

* User Registration
* Voice Calling
* Video Calling
* End Call
* User Information
* Notification Handling

---

# 👤 Step 9 - Register User

Every user must register before making or receiving calls.

```kotlin
callManager.registerUser(
    userId,
    userName
)
```

Observe registration state.

```kotlin
callManager.isRegistered
```

Retrieve current user.

```kotlin
callManager.userId

callManager.userName
```

Example

```kotlin
val isRegistered by callManager
    .isRegistered
    .collectAsState(initial = null)

if (isRegistered == false) {

    callManager.registerUser(
        userId,
        userName
    )

}
```

---

# 🔔 Step 10 - Handle Notification Intents

Inside MainActivity call

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

# ☎️ Step 11 - Add WebRtcCallHandler

Place the handler near the root of your Compose hierarchy.

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

The library automatically manages

* Incoming Calls
* Active Call UI
* Notifications
* Voice Calls
* Video Calls

---

# 📞 Step 12 - Start Calling

Once users are registered, start calls using their unique User ID.

Typical ViewModel implementation:

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

## Video Call

```kotlin
startCall(
    targetUserId = "john123",
    isAudioOnly = false
)
```

---

## Voice Call

```kotlin
startCall(
    targetUserId = "john123",
    isAudioOnly = true
)
```

---

## Parameters

| Parameter    | Description                               |
| ------------ | ----------------------------------------- |
| targetUserId | Unique ID of the registered user.         |
| isAudioOnly  | `true` = Voice Call, `false` = Video Call |

---

# ❌ End Call

End an active call manually.

```kotlin
callManager.endCall()
```

---

# 📚 Public API

| API                                | Description                   |
| ---------------------------------- | ----------------------------- |
| `registerUser(userId, userName)`   | Register a user with Firebase |
| `startCall(targetId, isAudioOnly)` | Creates a new call            |
| `startCallUI(roomId, isAudioOnly)` | Opens the library call UI     |
| `endCall()`                        | Ends the current call         |
| `handleIntent(intent)`             | Handles notification intents  |
| `userId`                           | Returns current user ID       |
| `userName`                         | Returns current user name     |
| `isRegistered`                     | Registration status Flow      |

---

# 📱 Sample Application

A fully working sample application is available here:

**GitHub Repository**

https://github.com/ronil-gwalani/Calling-Sample-App

The sample demonstrates:

* Firebase Setup
* Koin Setup
* User Registration
* Home Screen
* Voice Calling
* Video Calling
* Incoming Calls
* Notifications
* Lock Screen Support
* Camera Switching
* Speaker Switching
* Complete Call Lifecycle

---

# 📸 Screenshots

> Coming Soon

---

# 🎥 Demo

> Coming Soon

---

# ❓ Troubleshooting

## Incoming Calls Not Working

✔ Verify Firebase Cloud Messaging is enabled.

✔ Verify `service_account.json` exists inside `res/raw`.

✔ Grant Notification Permission on Android 13+.

✔ Call

```kotlin
callManager.handleIntent(intent)
```

inside both

* `onCreate()`
* `onNewIntent()`

---

## Registration Failed

Verify

* Firebase Configuration
* Firestore Enabled
* Realtime Database Enabled
* Firebase Rules

---

## Call Not Connecting

Verify

* Both users are registered.
* Target user exists.
* Internet connection.
* Firebase is configured correctly.

---

# 🛣️ Roadmap

Future releases will include:

* 🎨 Custom Call UI
* 🌙 Theme Support
* 🎵 Custom Ringtones
* 📜 Call History
* 📶 Connection State Callbacks
* 📊 Network Quality Monitoring
* 👤 User Presence API
* 🚫 Busy Status
* 👥 Group Calling

---

# 🤝 Contributing

Contributions, issues, and feature requests are welcome.

Feel free to fork the project and submit a Pull Request.

---

# ⭐ Support

If this library helped you, please consider giving the repository a **⭐ Star**.

Your support helps improve the library and motivates future development.

---

# 📄 License

This project is licensed under the MIT License.
