# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.communauto.tools.data.model.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
