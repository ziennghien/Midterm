plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Thêm plugin Google Services để tích hợp Firebase
}

android {
    namespace = "com.example.studentmanagementapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.studentmanagementapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation (libs.appcompat)
    implementation (libs.material)
    implementation (platform(libs.firebase.bom))
    implementation (libs.firebase.firestore)
    implementation (libs.firebase.storage)
    implementation (libs.firebase.auth)
    implementation (libs.recyclerview)
    implementation (libs.constraintlayout)
    implementation (libs.glide)
    annotationProcessor (libs.compiler)
    implementation (libs.opencsv)
    testImplementation (libs.junit)
    androidTestImplementation (libs.ext.junit)
    androidTestImplementation (libs.espresso.core)
    // CircleImageView
    implementation (libs.circleimageview)

// Glide
    implementation (libs.glide.v4142)
    annotationProcessor (libs.compiler.v4142)
}
