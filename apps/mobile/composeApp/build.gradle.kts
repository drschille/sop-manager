import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidMultiplatformLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.serialization)
//  alias(libs.plugins.convex)
}

kotlin {
  androidLibrary {
    namespace = "no.designsolutions.sopmanager.composeapp"
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
    implementation(libs.compose.ui.backhandler)
    implementation(libs.compose.components.resources)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.kamel.image)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)

    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.convex.core)
    implementation(libs.androidx.navigation3.runtime)

    testImplementation(libs.kotlin.test)
  }

  sourceSets {
    androidMain.dependencies {
      implementation(libs.ktor.client.okhttp)
    }
    iosMain.dependencies {
      implementation(libs.ktor.client.darwin)
    }
  }
}

//tasks.named("generateConvexSources") { enabled = false }

//project.afterEvaluate {
//  tasks.named("check") {
//    setDependsOn(dependsOn.filterNot { it is Task && it.name == "generateConvexSources" })
//  }
//}

dependencies { androidRuntimeClasspath(libs.compose.uiTooling) }

kotlin { jvmToolchain(21) }

//convex {
//  remote {
//    url = "https://dynamic-fish-493.convex.cloud"
//    key = "dev:dynamic-fish-493|eyJ2MiI6IjI4NzhjNTA2MDE3NjRlMWRhM2E5YzI2ZTY0MjA0MDNiIn0="
//  }
//}
