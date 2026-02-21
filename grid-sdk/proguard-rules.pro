# GridSdk Library - Internal ProGuard Rules
# These rules are applied during library builds

# Optimize but keep public API
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose

# Preserve all public API
-keep public class com.android.gridsdk.library.** { public *; }

# Allow obfuscation of internal packages
-keep class com.android.gridsdk.library.internal.** { *; }

