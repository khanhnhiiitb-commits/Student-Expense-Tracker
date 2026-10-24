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
    implementation(libs.room.common)
    implementation(libs.room.runtime)
    implementation(libs.testng)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    annotationProcessor(libs.room.compiler)
    implementation("org.mindrot:jbcrypt:0.4")
    testImplementation("junit:junit:4.13.2")
}