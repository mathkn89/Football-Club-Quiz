# R8 rules for release builds. Libraries (Room, Retrofit, OkHttp, kotlinx.serialization, AdMob,
# Play Billing, WorkManager) ship their own consumer rules; these cover our own code.

# JSON data classes for the club-data sync (Retrofit + kotlinx.serialization looks up their
# generated serializers by type at runtime).
-keep class com.makn.footballquiz.data.remote.dto.** { *; }
-keepclassmembers class com.makn.footballquiz.data.remote.dto.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep line numbers so Play Console crash reports are readable (R8 still obfuscates names;
# upload app/build/outputs/mapping/release/mapping.txt with each release).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
