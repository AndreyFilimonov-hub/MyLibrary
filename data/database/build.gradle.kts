import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room3)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.filimonov.mylibrary.data.database"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        androidMain {
            dependencies {
                implementation(libs.koin.android)
            }
        }
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)

                implementation(libs.androidx.room3.runtime)
                implementation(libs.androidx.sqlite.bundled)

                implementation(libs.koin.core)

                implementation(project(":core"))
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room3.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room3.compiler)
    add("kspIosArm64", libs.androidx.room3.compiler)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

