plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("com.google.gms.google-services") // обязательно для Firebase
    kotlin("kapt") // для Glide
}

android {
    namespace = "com.example.healthyfoodai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.healthyfoodai"
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Firebase BOM — автоматическая синхронизация версий
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-database-ktx")


    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation ("com.github.lzyzsd:circleprogress:1.2.1")

    // AndroidX и Material
    implementation(libs.coreKtx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activityKtx)
    implementation(libs.constraintLayout)

    // Retrofit для API запросов
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp для логирования запросов
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")

    // Навигация
    implementation("androidx.navigation:navigation-fragment-ktx:2.6.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.6.0")

    // Glide для изображений
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.firebaseCrashlyticsBuildtools)
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    // MPAndroidChart для графиков
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")

    // Индикаторы точек (Dots Indicator)
    implementation("com.tbuonomo:dotsindicator:4.3")
    implementation ("com.google.android.material:material:1.11.0") // или выше

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Тестирование
    testImplementation(libs.junit)
    androidTestImplementation(libs.testExtJunit)
    androidTestImplementation(libs.espressoCore)
}
