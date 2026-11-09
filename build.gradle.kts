buildscript {
    val composeCompilerVersion = "1.5.13"
    val composeBomVersion = "2024.06.00"
    val kotlinVersion = "1.9.23"
    val hiltVersion = "2.48.1"
    val roomVersion = "2.6.1"
    val coroutinesVersion = "1.8.0"
    val lifecycleVersion = "2.7.0"
    val navigationVersion = "2.7.7"
    val retrofitVersion = "2.11.0"
    val okhttpVersion = "4.12.0"
    val moshiVersion = "1.15.1"
    val coilVersion = "2.6.0"
    val materialVersion = "1.3.0-alpha01"
    val activityVersion = "1.9.0"
    val coreKtxVersion = "1.12.0"
    val pagingVersion = "3.2.1"
    val datastoreVersion = "1.0.0"
    val workManagerVersion = "2.9.0"
    val serializationVersion = "1.6.2"
    val ktlintVersion = "1.1.1"
    val detektVersion = "1.23.5"

    extra["composeCompilerVersion"] = composeCompilerVersion
    extra["composeBomVersion"] = composeBomVersion
    extra["kotlinVersion"] = kotlinVersion
    extra["hiltVersion"] = hiltVersion
    extra["roomVersion"] = roomVersion
    extra["coroutinesVersion"] = coroutinesVersion
    extra["lifecycleVersion"] = lifecycleVersion
    extra["navigationVersion"] = navigationVersion
    extra["retrofitVersion"] = retrofitVersion
    extra["okhttpVersion"] = okhttpVersion
    extra["moshiVersion"] = moshiVersion
    extra["coilVersion"] = coilVersion
    extra["materialVersion"] = materialVersion
    extra["activityVersion"] = activityVersion
    extra["coreKtxVersion"] = coreKtxVersion
    extra["pagingVersion"] = pagingVersion
    extra["datastoreVersion"] = datastoreVersion
    extra["workManagerVersion"] = workManagerVersion
    extra["serializationVersion"] = serializationVersion

    dependencies {
        classpath("com.android.tools.build:gradle:8.4.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.google.dagger:hilt-android-gradle-plugin:$hiltVersion")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:$ktlintVersion")
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:$detektVersion")
    }
}

plugins {
    id("com.android.application") version "8.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("com.google.dagger.hilt.android") version "2.48.1" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23" apply false
    id("org.jlleitschuh.gradle.ktlint") version "$ktlintVersion" apply false
    id("io.gitlab.arturbosch.detekt") version "$detektVersion" apply false
}