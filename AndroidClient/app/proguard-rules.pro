# Add ProGuard / R8 rules required for libraries used in the app
# Keep Firebase and related classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep OkHttp and Retrofit models annotated with @SerializedName
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep model classes used by reflection
-keep class com.example.aivideoclient.model.** { *; }

# Keep ExoPlayer classes
-keep class com.google.android.exoplayer2.** { *; }

# Keep any classes annotated with @Keep
-keep @androidx.annotation.Keep class * { *; }
-dontwarn okio.**
-dontwarn javax.annotation.**
