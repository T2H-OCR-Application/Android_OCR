# ML Kit
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text.** { *; }

# OpenCV
-keep class org.opencv.** { *; }
-keepclassmembers class org.opencv.** {
    native <methods>;
}

# Firebase & Play Services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class kotlinx.serialization.** { *; }

# Google Drive API
-keep class com.google.api.** { *; }
-keep class com.google.auth.** { *; }
