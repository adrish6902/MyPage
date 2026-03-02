# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-dontwarn org.apache.poi.**
-dontwarn org.apache.commons.**
-dontwarn com.zaxxer.sparsebits.**
-dontwarn org.openxmlformats.**
-dontwarn org.etsi.uri.**
-dontwarn net.sf.saxon.**
-dontwarn org.apache.xml.security.**
-dontwarn org.apache.jcp.xml.dsig.internal.**
-dontwarn javax.xml.stream.**
-dontwarn java.awt.**
-dontwarn com.microsoft.schemas.**
-dontwarn javax.xml.namespace.QName
-dontwarn com.sun.org.apache.xerces.internal.dom.DocumentImpl
-dontwarn com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver
-dontwarn org.apache.xmlbeans.**

# Keep metadata for Kotlin
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Keep generic signatures (required for Gson TypeToken)
-keepattributes Signature
-keepattributes *Annotation*

# Keep Gson model classes
-keep class com.example.mypage.model.** { *; }

# Keep Gson core
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * extends com.google.gson.JsonSerializer
-keep class * extends com.google.gson.JsonDeserializer

# Keep TypeToken subclasses (fixes TypeToken crash)
-keep class * extends com.google.gson.reflect.TypeToken

