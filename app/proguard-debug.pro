# Debug-only ProGuard rules for androidTest compatibility.
#
# The debug build has R8 minification enabled, but instrumented tests (ui-test-junit4)
# reference classes by their original names. R8 strips/renames internal classes,
# Kotlin $DefaultImpls, and project composable functions, causing ClassNotFoundException
# at runtime.
#
# These broad keep rules ensure all necessary classes survive R8 for testing.
# They only affect the debug build — the release build uses proguard-rules.pro only.

# Disable R8 method body optimization (keeps bytecode intact for test APK references)
-dontoptimize

# Keep all project classes (composable functions, sealed classes, etc.)
-keep class org.jellyfin.androidtv.** { *; }

# Keep Kotlin stdlib and coroutines (TestMainDispatcherFactory references)
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Keep Compose framework classes (ViewRootForTest, InfiniteAnimationPolicy, etc.)
-keep class androidx.compose.** { *; }

# Keep Kotlin interface default method implementations
-keep class **$DefaultImpls { *; }
