# R8 rules for the release build. Keep this file NARROW: every blanket rule here
# lowers the obfuscation figure that Google Play measures (it wants at least 25 %).
# Add a rule only for something that reflection or serialization really reaches, and
# say why. Libraries that ship their own consumer rules (Hilt, Room, Retrofit, OkHttp,
# WorkManager, DataStore, Compose, Navigation) need nothing here, and manifest
# components (activity, service, receivers, application) are kept by AGP itself.

# Stack traces in Play Console / Crashlytics are readable only through mapping.txt, so
# keep file and line information and hide the real file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Retrofit and Gson read generic types (Response<MonthlyCalendarResponse>, List<PrayerData>)
# and annotations (@GET, @Path, @Query) off class files at runtime. Retrofit's own rules
# repeat this; it is stated here too because it is the first thing that breaks.
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault

# Gson 2.10 (pulled in by converter-gson 2.11) ships no consumer rules. These are the
# ones from Gson's own documentation; the app has no custom TypeAdapter or TypeToken
# subclass, so this is only the safety net for the library's internal use.
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn sun.misc.**

# The aladhan.com JSON is built into these Kotlin classes by Gson, by reflection and by
# FIELD NAME (there is no @SerializedName). Two things have to hold, hence two rules:
#  1. The classes themselves must stay. R8 sees no `new PrayerTimesResponse()` anywhere
#     (Gson allocates them reflectively), decides the classes are dead and deletes them;
#     Retrofit then hands back a LinkedTreeMap and the first `response.body()` throws
#     ClassCastException. This was found on an emulator, not by unit tests.
#     allowobfuscation: the class NAME may still be renamed, Gson does not need it.
#  2. The field names must not be renamed or removed.
# Every class Gson can reach from PrayerTimesResponse and MonthlyCalendarResponse is
# listed. If a model is added to data/Models.kt or data/PrayerTimesApi.kt, add it here.
-keep,allowobfuscation class af.namazia.app.data.PrayerTimesResponse
-keep,allowobfuscation class af.namazia.app.data.MonthlyCalendarResponse
-keep,allowobfuscation class af.namazia.app.data.PrayerData
-keep,allowobfuscation class af.namazia.app.data.Timings
-keep,allowobfuscation class af.namazia.app.data.DateInfo
-keep,allowobfuscation class af.namazia.app.data.HijriDate
-keep,allowobfuscation class af.namazia.app.data.HijriMonth
-keep,allowobfuscation class af.namazia.app.data.GregorianDate
-keep,allowobfuscation class af.namazia.app.data.GregorianMonth
-keep,allowobfuscation class af.namazia.app.data.Weekday
-keep,allowobfuscation class af.namazia.app.data.MetaInfo
-keepclassmembers class af.namazia.app.data.PrayerTimesResponse { <fields>; }
-keepclassmembers class af.namazia.app.data.MonthlyCalendarResponse { <fields>; }
-keepclassmembers class af.namazia.app.data.PrayerData { <fields>; }
-keepclassmembers class af.namazia.app.data.Timings { <fields>; }
-keepclassmembers class af.namazia.app.data.DateInfo { <fields>; }
-keepclassmembers class af.namazia.app.data.HijriDate { <fields>; }
-keepclassmembers class af.namazia.app.data.HijriMonth { <fields>; }
-keepclassmembers class af.namazia.app.data.GregorianDate { <fields>; }
-keepclassmembers class af.namazia.app.data.GregorianMonth { <fields>; }
-keepclassmembers class af.namazia.app.data.Weekday { <fields>; }
-keepclassmembers class af.namazia.app.data.MetaInfo { <fields>; }
