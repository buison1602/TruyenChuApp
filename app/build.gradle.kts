// build.gradle.kts (hoặc build.gradle nếu bạn dùng Groovy)

// Thêm import này ở đầu file nếu là file .kts
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

// Đọc API Key từ local.properties
val properties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    properties.load(localPropertiesFile.inputStream())
}


android {
    namespace = "com.example.truyenchu"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.truyenchu"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ĐẶT DÒNG NÀY VÀO ĐÚNG VỊ TRÍ BÊN TRONG defaultConfig
        buildConfigField("String", "GEMINI_API_KEY", "\"${properties.getProperty("GEMINI_API_KEY", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
}

// KHỐI DEPENDENCIES ĐÃ ĐƯỢC DỌN DẸP SẠCH SẼ
dependencies {

    // AndroidX & UI
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Firebase (sử dụng BoM - Bill of Materials để quản lý phiên bản)
    // Chỉ cần khai báo platform một lần
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")

    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Google AI
    implementation("com.google.ai.client.generativeai:generativeai:0.6.0")
    // Thêm thư viện Gson để xử lý JSON, rất cần cho code GeminiHelper
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.room.runtime.android)


    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.firebase:firebase-firestore")

    implementation ("androidx.navigation:navigation-fragment:2.9.0")
    implementation ("androidx.navigation:navigation-ui:2.9.0")

    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")

    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")
}