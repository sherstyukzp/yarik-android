import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.compose)
}

val githubReleaseCertSha256 = providers
    .environmentVariable("BITCHAT_GITHUB_RELEASE_CERT_SHA256")
    .orElse(providers.gradleProperty("BITCHAT_GITHUB_RELEASE_CERT_SHA256"))
    .orElse("")
val normalizedGithubReleaseCertSha256 = githubReleaseCertSha256.get()
    .replace(":", "")
    .trim()
    .lowercase()
require(
    normalizedGithubReleaseCertSha256.isEmpty() ||
        normalizedGithubReleaseCertSha256.matches(Regex("[a-f0-9]{64}"))
) {
    "BITCHAT_GITHUB_RELEASE_CERT_SHA256 must be a SHA-256 certificate fingerprint"
}

android {
    namespace = "com.bitchat.android"
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        buildConfigField("String", "VERSION_NAME", "\"2.0.3\"")
        buildConfigField("int", "VERSION_CODE", "40")
        buildConfigField(
            "String",
            "GITHUB_RELEASE_CERT_SHA256",
            "\"$normalizedGithubReleaseCertSha256\""
        )
        testInstrumentationRunner = "com.bitchat.android.ui.PerformanceTestRunner"
        consumerProguardFiles("proguard-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false
        checkReleaseBuilds = false
    }
}

composeCompiler {
    includeComposeMappingFile.set(false)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)

    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.lifecycle.process)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.accompanist.permissions)

    implementation(libs.zxing.core)
    implementation(libs.mlkit.barcode.scanning)

    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.compose)

    implementation(libs.bundles.cryptography)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.nordic.ble)
    implementation(libs.okhttp)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.nanohttpd)
    implementation(libs.gms.location)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.exifinterface)

    testImplementation(libs.bundles.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
    systemProperty(
        "robolectric.dependency.repo.url",
        "https://repo.maven.apache.org/maven2"
    )
}
