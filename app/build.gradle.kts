plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
}

<<<<<<< HEAD
=======
val interpretationUrl = providers.gradleProperty("phoneBookInterpretationUrl")

fun quotedBuildConfigValue(value: String): String =
    "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

>>>>>>> 568a8069af337ae66c759e6f78eeaa2da2c870b9
android {
    namespace = "com.y219.phonebook"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.y219.phonebook"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
<<<<<<< HEAD
        release {
=======
        debug {
            buildConfigField(
                "String", "INTERPRETATION_URL",
                quotedBuildConfigValue(interpretationUrl.getOrElse("http://10.0.2.2:8787/interpret"))
            )
        }
        release {
            buildConfigField(
                "String", "INTERPRETATION_URL", quotedBuildConfigValue(interpretationUrl.getOrElse(""))
            )
>>>>>>> 568a8069af337ae66c759e6f78eeaa2da2c870b9
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
<<<<<<< HEAD
=======
    buildFeatures {
        buildConfig = true
    }
>>>>>>> 568a8069af337ae66c759e6f78eeaa2da2c870b9
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
ksp {
    arg("room.schemaLocation", layout.projectDirectory.dir("schemas").asFile.absolutePath)
}
<<<<<<< HEAD

=======
>>>>>>> 568a8069af337ae66c759e6f78eeaa2da2c870b9
dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
<<<<<<< HEAD
    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation("androidx.lifecycle:lifecycle-runtime:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.10.0")

}
=======
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
}
>>>>>>> 568a8069af337ae66c759e6f78eeaa2da2c870b9
