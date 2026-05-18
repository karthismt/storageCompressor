# Storage Doctor ProGuard Rules
-keepattributes *Annotation*
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
