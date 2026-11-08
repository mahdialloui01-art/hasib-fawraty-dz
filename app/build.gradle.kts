plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dz.hasibfawraty.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "dz.hasibfawraty.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.2"
        val admobAppId = System.getenv("ADMOB_APP_ID").orEmpty()
        val admobBannerId = System.getenv("ADMOB_BANNER_AD_UNIT_ID").orEmpty()
        manifestPlaceholders["ADMOB_APP_ID"] = admobAppId
        buildConfigField("String", "ADMOB_APP_ID", "\"${admobAppId}\"")
        buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", "\"${admobBannerId}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // A signed Play upload bundle is produced when the four PLAY_UPLOAD_* secrets
    // are configured in GitHub Actions. Without them, bundleRelease remains unsigned.
    val storeFilePath = System.getenv("PLAY_UPLOAD_KEYSTORE_PATH")
    val storePasswordValue = System.getenv("PLAY_UPLOAD_KEYSTORE_PASSWORD")
    val keyAliasValue = System.getenv("PLAY_UPLOAD_KEY_ALIAS")
    val keyPasswordValue = System.getenv("PLAY_UPLOAD_KEY_PASSWORD")
    if (!storeFilePath.isNullOrBlank() && !storePasswordValue.isNullOrBlank() &&
        !keyAliasValue.isNullOrBlank() && !keyPasswordValue.isNullOrBlank()) {
        signingConfigs {
            create("playRelease") {
                storeFile = file(storeFilePath)
                storePassword = storePasswordValue
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
            }
        }
        buildTypes {
            getByName("debug") {
                manifestPlaceholders["ADMOB_APP_ID"] = "ca-app-pub-3940256099942544~3347511713"
                buildConfigField("String", "ADMOB_APP_ID", "\"ca-app-pub-3940256099942544~3347511713\"")
                buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/9214589741\"")
            }
            getByName("release") {
                signingConfig = signingConfigs.getByName("playRelease")
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            }
        }
    } else {
        buildTypes {
            getByName("debug") {
                manifestPlaceholders["ADMOB_APP_ID"] = "ca-app-pub-3940256099942544~3347511713"
                buildConfigField("String", "ADMOB_APP_ID", "\"ca-app-pub-3940256099942544~3347511713\"")
                buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/9214589741\"")
            }
            getByName("release") {
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            }
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    // Activity Result APIs require Fragment 1.3.0 or newer; pin a current stable version.
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.google.android.libraries.ads.mobile.sdk:ads-mobile-sdk:1.6.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
