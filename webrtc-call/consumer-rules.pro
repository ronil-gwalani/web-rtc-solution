# Keep WebRTC classes
-keep class org.webrtc.** { *; }

# Keep Koin classes
-keep class org.koin.** { *; }

# Keep your library classes if they are accessed via reflection or used as entry points
-keep class org.ron.webrtccall.** { *; }
