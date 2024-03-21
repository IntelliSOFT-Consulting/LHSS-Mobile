plugins {
    id("com.android.application")
    id("kotlin-android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.intellisoft.lhss"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.intellisoft.lhss"
        minSdk = 24
        targetSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["appAuthRedirectScheme"] = applicationId!!
        buildFeatures.buildConfig = true
        versionCode = 4
        versionName = "1.0"
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
    buildFeatures { viewBinding = true }
    compileOptions {
        // Flag to enable support for the new language APIs
        // See https://developer.android.com/studio/write/java8-support
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin { jvmToolchain(11) }
    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/ASL-2.0.txt",
                "META-INF/LGPL-3.0.txt",
                "META-INF/LICENSE.md",
                "META-INF/NOTICE.md",
                "META-INF/sun-jaxb.episode",
                "META-INF/DEPENDENCIES"
            )
        )
    }
}


dependencies {
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("androidx.activity:activity-ktx:1.8.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.10")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("com.google.android.material:material:1.10.0")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("net.openid:appauth:0.11.1")
    implementation("com.auth0.android:jwtdecode:2.0.1")
    implementation("com.google.android.fhir:engine:0.1.0-beta03")
    implementation("com.google.android.fhir:data-capture:1.0.0")

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))

    // Add the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don"t specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")

    //Network
    implementation("com.squareup.retrofit2:retrofit:2.6.3")
    implementation("com.squareup.retrofit2:converter-gson:2.6.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.squareup.retrofit2:converter-moshi:2.6.0")

    implementation ("com.github.joielechong:countrycodepicker:2.4.2")

}
