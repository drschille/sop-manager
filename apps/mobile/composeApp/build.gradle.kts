import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.convex)
}

kotlin {
    androidLibrary {
        namespace = "no.designsolutions.timetracker.composeapp"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions { jvmTarget = JvmTarget.JVM_21 }
        androidResources { enable = true }
        withHostTest { isIncludeAndroidResources = true }
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        implementation(libs.compose.runtime)
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
        implementation(libs.compose.ui)
        implementation(libs.compose.components.resources)
        implementation(libs.compose.uiToolingPreview)
        implementation(libs.androidx.lifecycle.viewmodelCompose)
        implementation(libs.androidx.lifecycle.runtimeCompose)

        implementation(libs.kotlinx.datetime)

        testImplementation(libs.kotlin.test)

        implementation(libs.kotlinx.serialization.json)
        implementation(libs.kotlinx.serialization.core)
        implementation(libs.convex.core)
        implementation(libs.haze)
    }
}

dependencies { androidRuntimeClasspath(libs.compose.uiTooling) }

kotlin {
    jvmToolchain(21)
}

tasks.named("generateConvexSources") {
    enabled = false
}

convex {
    remote {
        url = "https://combative-mule-878.convex.cloud"
        key = "dev:combative-mule-878|eyJ2MiI6IjQzYjU0NmNhMzdlNTQ5MDBiODEzNmU0OGYxZGY1M2M4In0="
    }
}
