import com.android.build.gradle.tasks.PackageAndroidArtifact

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.prslc.mitsuha"

    buildFeatures { buildConfig = true }

    defaultConfig {
        applicationId = "com.prslc.mitsuha"
        compileSdk = 37
        minSdk = 33
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 2
        versionName = "1.2"

        ndk {
            abiFilters.add("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            vcsInfo.include = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources {
            merges += "META-INF/xposed/*"
            excludes += "**"
        }
    }
}

// https://stackoverflow.com/a/77745844
tasks.withType<PackageAndroidArtifact> {
    doFirst { appMetadata.asFile.orNull?.writeText("") }
}

dependencies {
    compileOnly(libs.libxposed.api)
    implementation(libs.libxposed.service)
    implementation(libs.dexkit)
}
