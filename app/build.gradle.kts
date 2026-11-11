plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
}

val interpretationUrl = providers.gradleProperty("phoneBookInterpretationUrl")

fun quotedBuildConfigValue(value: String): String =
    "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

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
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
ksp {
    arg("room.schemaLocation", layout.projectDirectory.dir("schemas").asFile.absolutePath)
}
dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
}
