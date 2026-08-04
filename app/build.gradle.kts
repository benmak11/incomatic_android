import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Runtime config lives in gitignored local.properties, mirroring incomatic
// (iOS)'s Config/Secrets.xcconfig — never checked into VCS.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

// Release signing material. CI sets these as environment variables; locally
// they live in the same gitignored local.properties as the runtime config
// above. Environment wins so CI never depends on a file being present.
fun signingValue(key: String): String? =
    System.getenv(key)?.takeIf { it.isNotBlank() }
        ?: localProperties.getProperty(key)?.takeIf { it.isNotBlank() }

// GitHub secrets can't hold binaries, so CI base64-decodes the keystore to a
// file first and passes its path here — Gradle only ever deals in paths.
val releaseKeystoreFile = signingValue("RELEASE_KEYSTORE_FILE")?.let(::file)
val releaseKeystorePassword = signingValue("RELEASE_KEYSTORE_PASSWORD")
val releaseKeyAlias = signingValue("RELEASE_KEY_ALIAS")
val releaseKeyPassword = signingValue("RELEASE_KEY_PASSWORD")

// Nothing configured at all (a contributor's checkout, a PR build with no
// secrets) leaves the release build unsigned rather than failing it — same
// behavior as before this block existed.
val releaseSigningValues = listOf(
    releaseKeystoreFile?.path,
    releaseKeystorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
)
val hasReleaseSigning = releaseSigningValues.all { it != null } &&
    releaseKeystoreFile?.exists() == true

// A partially configured keystore is always a mistake — a typo'd secret name
// would otherwise publish an unsigned artifact and fail confusingly at upload
// time instead of here.
if (!hasReleaseSigning && releaseSigningValues.any { it != null }) {
    val problems = buildList {
        if (releaseKeystoreFile == null) add("RELEASE_KEYSTORE_FILE is not set")
        if (releaseKeystorePassword == null) add("RELEASE_KEYSTORE_PASSWORD is not set")
        if (releaseKeyAlias == null) add("RELEASE_KEY_ALIAS is not set")
        if (releaseKeyPassword == null) add("RELEASE_KEY_PASSWORD is not set")
        if (releaseKeystoreFile?.exists() == false) {
            add("no keystore at ${releaseKeystoreFile.path}")
        }
    }
    error(
        "Release signing is misconfigured: ${problems.joinToString("; ")}. " +
            "Set all four in local.properties or the environment, or none of " +
            "them to build unsigned.",
    )
}

android {
    namespace = "com.makusha.incomatic"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.makusha.incomatic"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String", "API_BASE_URL_PROD",
            "\"${localProperties.getProperty("API_BASE_URL_PROD", "")}\"",
        )
        buildConfigField(
            "boolean", "USE_LOCAL_BACKEND",
            localProperties.getProperty("USE_LOCAL_BACKEND", "false"),
        )
        buildConfigField(
            "String", "GOOGLE_WEB_CLIENT_ID",
            "\"${localProperties.getProperty("GOOGLE_WEB_CLIENT_ID", "")}\"",
        )
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseKeystoreFile
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                // Signature scheme versions are left to AGP's minSdk-derived
                // defaults: at minSdk 26 every install target supports v2, so
                // v1 (JAR signing) is correctly skipped.
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
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
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    // Not debugImplementation: ApiClient references this class from main
    // source, gating its *use* behind BuildConfig.DEBUG at runtime instead —
    // debugImplementation would break assembleRelease with an unresolved
    // reference since the class wouldn't be on the release classpath.
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.security.crypto)
    implementation(libs.play.review.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
