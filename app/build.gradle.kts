plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "vn.edu.ueh.nhitang31241023902.student_expense_tracker"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "vn.edu.ueh.nhitang31241023902.student_expense_tracker"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // Room (Java)
    implementation(libs.room.common)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Mã hóa mật khẩu
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("androidx.concurrent:concurrent-futures:1.2.0")
    // Biểu đồ
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Test
    testImplementation(libs.junit)
    testImplementation("org.hamcrest:hamcrest:3.0")
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}

configurations.all {
    resolutionStrategy {
        exclude(group = "org.hamcrest", module = "hamcrest-core")
    }
}