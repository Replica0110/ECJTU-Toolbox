import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone.getDefault

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    kotlin("plugin.serialization")
}
fun getVersionCode(): Int {
    val cmd = "git rev-list HEAD --first-parent --count"
    val process = Runtime.getRuntime().exec(cmd)
    return try {
        process.waitFor()
        process.inputStream.bufferedReader().readText().trim().toInt()
    } catch (e: Exception) {
        e.printStackTrace()
        0
    } finally {
        process.destroy()
    }
}
android {
    namespace = "com.lonx.ecjtutoolbox"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lonx.ecjtutoolbox"
        minSdk = 26
        targetSdk = 34
        versionCode = getVersionCode()
        versionName = "1.0.0"
        val buildTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").apply {
            timeZone = getDefault()
        }.format(Date())

        // 设置输出文件名
        applicationVariants.all {
            val variant = this
            variant.outputs
                .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                .forEach { output ->
                    val outputFileName = "ECJTU-Toolbox-${variant.versionName}.apk"
                    println("OutputFileName: $outputFileName")
                    output.outputFileName = outputFileName
                }
        }

        buildConfigField("String", "BUILD_TIME", "\"$buildTime\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            versionNameSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation(libs.koin.android)
    implementation(libs.slimber)
    implementation(libs.brv)
    implementation(libs.persistentcookiejar)
    implementation(libs.statusbar)
    implementation(libs.engine)
    implementation(libs.jsoup.jsoup)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}