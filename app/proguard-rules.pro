# Add project specific ProGuard rules here.

# Keep kotlinx.serialization metadata for @Serializable classes.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class **$$serializer {
    *** descriptor;
}
-keepclasseswithmembers,allowshrinking class * {
    @kotlinx.serialization.Serializable <methods>;
}

# Keep the data-contract models intact (serialized to/from JSON).
-keep,includedescriptorclasses class com.latido.app.domain.model.** { *; }
