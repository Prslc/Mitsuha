# Xposed module entry point — referenced by name in java_init.list
-keep class com.example.mitsuha.MainHook { *; }

# Keep all XposedModule subclasses and their members
-keep class * extends io.github.libxposed.api.XposedModule { *; }

