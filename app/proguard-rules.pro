# Kanlist ProGuard Rules

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class com.kanlist.**Hilt* { *; }

# Keep Room entities and DAOs
-keep class com.kanlist.data.local.** { *; }

# Keep Moshi JSON adapters
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class *
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
    @com.squareup.moshi.Json <methods>;
}

# Keep Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }
-keep @kotlinx.serialization.Serializable class *
-keepclassmembers @kotlinx.serialization.Serializable class * { *; }

# Keep Retrofit and OkHttp
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface retrofit2.* { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# Keep Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Keep Material3
-keep class androidx.compose.material3.** { *; }

# Keep Navigation
-keep class androidx.navigation.** { *; }

# Keep Lifecycle
-keep class androidx.lifecycle.** { *; }

# Keep WorkManager
-keep class androidx.work.** { *; }

# Keep Datastore
-keep class androidx.datastore.** { *; }

# Keep Paging
-keep class androidx.paging.** { *; }

# Keep Hilt Work
-keep class androidx.hilt.work.** { *; }

# Keep Accompanist
-keep class com.google.accompanist.** { *; }

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep Java time
-keep class java.time.** { *; }

# Suppress warnings
-dontwarn sun.misc.Unsafe
-dontwarn java.nio.file.*
-dontwarn java.lang.module.*