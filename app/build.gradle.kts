import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.makn.footballquiz"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.makn.footballquiz"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField("String", "DATA_SYNC_BASE_URL", "\"https://mathkn89.github.io/Football-Club-Quiz/data/\"")
        // Delta version already baked into the bundled clubs.db (written by scripts/build_clubs_db.py),
        // so a fresh install syncs only newer deltas instead of replaying older ones over the seed.
        val seedDataVersion = file("seed_data_version.txt").readText().trim().toInt()
        buildConfigField("int", "SEED_DATA_VERSION", seedDataVersion.toString())

        buildConfigField("String", "PRIVACY_POLICY_URL", "\"https://mathkn89.github.io/Football-Club-Quiz/privacy.html\"")
        // Play Console in-app product for the one-time "Remove ads" purchase.
        buildConfigField("String", "REMOVE_ADS_PRODUCT_ID", "\"remove_ads\"")
    }

    androidResources {
        // Lists the bundled languages in Android 13+ Settings > Apps > Language.
        generateLocaleConfig = true
    }

    // Upload key lives in release-signing/ (git-ignored). Without it, release builds are unsigned
    // and Android Studio's "Generate Signed App Bundle" can sign them instead.
    val signingProps = rootProject.file("release-signing/keystore.properties")
        .takeIf { it.exists() }
        ?.let { file -> Properties().apply { file.inputStream().use { load(it) } } }
    signingConfigs {
        if (signingProps != null) {
            create("release") {
                storeFile = rootProject.file("release-signing/${signingProps.getProperty("storeFile")}")
                storePassword = signingProps.getProperty("storePassword")
                keyAlias = signingProps.getProperty("keyAlias")
                keyPassword = signingProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        // AdMob IDs (not secret — they're readable from any published app). Debug builds always use
        // Google's public test IDs so testing can never produce real impressions or clicks, which
        // AdMob treats as invalid traffic.
        debug {
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "ADMOB_BANNER_ID", "\"ca-app-pub-3940256099942544/9214589741\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "ADMOB_REWARDED_ID", "\"ca-app-pub-3940256099942544/5224354917\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfigs.findByName("release")?.let { signingConfig = it }
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3922912607913463~9680233170"
            buildConfigField("String", "ADMOB_BANNER_ID", "\"ca-app-pub-3922912607913463/7752073839\"") // Banner - tab bar
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"ca-app-pub-3922912607913463/7312080975\"") // Interstitial - between rounds
            buildConfigField("String", "ADMOB_REWARDED_ID", "\"ca-app-pub-3922912607913463/1059507125\"") // Reward - survival second chance
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.gms:play-services-ads:24.9.0")
    implementation("com.google.android.ump:user-messaging-platform:3.2.0")
    implementation("com.android.billingclient:billing-ktx:8.3.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-svg:2.6.0")
    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
}
