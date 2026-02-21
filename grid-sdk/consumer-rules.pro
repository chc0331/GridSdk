# GridSdk Library - Consumer ProGuard Rules
# These rules are applied to consuming applications when they use this library

# Keep all public API classes and methods
-keep public class com.android.gridsdk.library.** { public *; }

# Keep Compose-related classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Kotlin metadata for reflection
-keep class kotlin.Metadata { *; }

# Preserve annotations
-keepattributes *Annotation*

# Keep source file names and line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Hide internal implementation details from stack traces
-repackageclasses 'com.android.gridsdk.library.internal'

