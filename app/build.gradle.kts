plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("kotlinx-serialization")
    id("kotlin-kapt")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "com.kanlist"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kanlist"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
    }

    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["composeCompilerVersion"] as String
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-Xopt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-Xopt-in=androidx.lifecycle.ExperimentalLifecycleApi"
        )
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,license.txt,LICENSE,NOTICE,notice.txt}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:${rootProject.extra["coreKtxVersion"]}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${rootProject.extra["lifecycleVersion"]}")
    implementation("androidx.activity:activity-compose:${rootProject.extra["activityVersion"]}")
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Material 3 / Material You
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material3:material3-window-size-class:1.2.1")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:${rootProject.extra["composeBomVersion"]}"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.runtime:runtime-rxjava2")

    // Navigation
    implementation("androidx.navigation:navigation-compose:${rootProject.extra["navigationVersion"]}")
    implementation("androidx.navigation:navigation-fragment-ktx:${rootProject.extra["navigationVersion"]}")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:${rootProject.extra["hiltVersion"]}")
    kapt("com.google.dagger:hilt-compiler:${rootProject.extra["hiltVersion"]}")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.1.0")

    // Room Database
    implementation("androidx.room:room-runtime:${rootProject.extra["roomVersion"]}")
    implementation("androidx.room:room-ktx:${rootProject.extra["roomVersion"]}")
    kapt("androidx.room:room-compiler:${rootProject.extra["roomVersion"]}")

    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:${rootProject.extra["datastoreVersion"]}")
    implementation("androidx.datastore:datastore-core:${rootProject.extra["datastoreVersion"]}")

    // Coroutines & Flow
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${rootProject.extra["coroutinesVersion"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${rootProject.extra["coroutinesVersion"]}")

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:${rootProject.extra["retrofitVersion"]}")
    implementation("com.squareup.retrofit2:converter-moshi:${rootProject.extra["retrofitVersion"]}")
    implementation("com.squareup.okhttp3:okhttp:${rootProject.extra["okhttpVersion"]}")
    implementation("com.squareup.okhttp3:logging-interceptor:${rootProject.extra["okhttpVersion"]}")

    // Moshi JSON
    implementation("com.squareup.moshi:moshi:${rootProject.extra["moshiVersion"]}")
    implementation("com.squareup.moshi:moshi-kotlin:${rootProject.extra["moshiVersion"]}")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:${rootProject.extra["moshiVersion"]}")

    // Coil Image Loading
    implementation("io.coil-kt:coil-compose:${rootProject.extra["coilVersion"]}")
    implementation("io.coil-kt:coil-gif:${rootProject.extra["coilVersion"]}")
    implementation("io.coil-kt:coil-svg:${rootProject.extra["coilVersion"]}")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${rootProject.extra["serializationVersion"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${rootProject.extra["serializationVersion"]}")

    // Paging 3
    implementation("androidx.paging:paging-compose:${rootProject.extra["pagingVersion"]}")
    implementation("androidx.paging:paging-runtime-ktx:${rootProject.extra["pagingVersion"]}")

    // WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:${rootProject.extra["workManagerVersion"]}")

    // Accompanist (Permissions, System UI Controller, etc.)
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    implementation("com.google.accompanist:accompanist-drawablepainter:0.32.0")
    implementation("com.google.accompanist:accompanist-flowlayout:0.32.0")

    // Material Date Picker
    implementation("com.google.android.material:material:1.12.0")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Haptic Feedback
    // implementation("androidx.hapticfeedback:hapticfeedback:1.0.0-alpha02")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${rootProject.extra["coroutinesVersion"]}")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.0")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest:1.6.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.0")
    debugImplementation("androidx.compose.ui:ui-tooling-data:1.6.0")

    // KSP for Room (optional, using KAPT for now)
    // implementation("com.google.devtools.ksp:symbol-processing-api:1.9.20-1.0.13")
}

ktlint {
    version.set("1.1.1")
    output = ktlintOutput.Console
    reporters {
        plain {}
        checkstyle {}
    }
}

detekt {
    config = files("../detekt.yml")
    buildUponDefaultConfig = true
    parallel = true
}