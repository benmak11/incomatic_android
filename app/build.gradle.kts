import com.github.triplet.gradle.androidpublisher.ReleaseStatus
import com.github.triplet.gradle.androidpublisher.ResolutionStrategy
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.play.publisher)
}

// Runtime config lives in gitignored local.properties, mirroring incomatic
// (iOS)'s Config/Secrets.xcconfig — never checked into VCS.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

// Release/publishing config. CI sets these as environment variables; locally
// they live in the same gitignored local.properties as the runtime config
// above. Environment wins so CI never depends on a file being present.
fun envOrLocal(key: String): String? =
    System.getenv(key)?.takeIf { it.isNotBlank() }
        ?: localProperties.getProperty(key)?.takeIf { it.isNotBlank() }

// GitHub secrets can't hold binaries, so CI base64-decodes the keystore to a
// file first and passes its path here — Gradle only ever deals in paths.
val releaseKeystoreFile = envOrLocal("RELEASE_KEYSTORE_FILE")?.let(::file)
val releaseKeystorePassword = envOrLocal("RELEASE_KEYSTORE_PASSWORD")
val releaseKeyAlias = envOrLocal("RELEASE_KEY_ALIAS")
val releaseKeyPassword = envOrLocal("RELEASE_KEY_PASSWORD")

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

// The git tag is the single source of truth for versionName, mirroring
// incomatic (iOS). The publish workflow passes the tag explicitly; locally we
// read the newest tag, falling back to a placeholder in a checkout that has
// none yet — this repo stays untagged until its first release.
val latestGitTag: String? = runCatching {
    providers.exec {
        commandLine("git", "describe", "--tags", "--abbrev=0")
        isIgnoreExitValue = true
    }.standardOutput.asText.get().trim()
}.getOrNull()?.takeIf { it.isNotBlank() }

val explicitVersionName = envOrLocal("VERSION_NAME")?.removePrefix("v")

// Fail rather than fall back. iOS spent four releases stamping a stale
// MARKETING_VERSION because its equivalent path degraded silently; a malformed
// tag should stop the build, not ship under the wrong number.
if (explicitVersionName != null && !Regex("""\d+\.\d+\.\d+""").matches(explicitVersionName)) {
    error("VERSION_NAME must be a semantic version like 1.2.3, got '$explicitVersionName'.")
}

val appVersionName = explicitVersionName ?: latestGitTag?.removePrefix("v") ?: "0.0.0-dev"

// Play publishing credentials. Absent locally and on PR builds, where the
// publish tasks are never invoked — GPP only needs them when one actually runs.
val playCredentialsFile = envOrLocal("PLAY_SERVICE_ACCOUNT_JSON_FILE")?.let(::file)
val playTrack = envOrLocal("PLAY_TRACK") ?: "internal"

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
        // Placeholder floor only. Play is the source of truth for versionCode
        // (see the play block below); this value is what gets used for the very
        // first upload, when there is nothing in Play to increment from.
        versionCode = 1
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // envOrLocal, not localProperties directly: CI has no local.properties,
        // and a published build with an empty API base URL or client id would
        // install fine and then fail every request at runtime.
        buildConfigField(
            "String", "API_BASE_URL_PROD",
            "\"${envOrLocal("API_BASE_URL_PROD") ?: ""}\"",
        )
        buildConfigField(
            "boolean", "USE_LOCAL_BACKEND",
            envOrLocal("USE_LOCAL_BACKEND") ?: "false",
        )
        buildConfigField(
            "String", "GOOGLE_WEB_CLIENT_ID",
            "\"${envOrLocal("GOOGLE_WEB_CLIENT_ID") ?: ""}\"",
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

play {
    if (playCredentialsFile == null) {
        // No credentials means local dev or a PR build. GPP has to be switched
        // off entirely rather than just left unconfigured: its AUTO versionCode
        // resolution hooks into assembleRelease and would fail the build trying
        // to reach Play for a version it can't ask about.
        enabled.set(false)
    } else {
        serviceAccountCredentials.set(playCredentialsFile)
        // Play owns versionCode: AUTO reads the highest already uploaded and
        // increments from it. Never derive it from CI run numbers — those reset
        // when a workflow is renamed, and Play's rejection of a reused
        // versionCode is permanent for that number.
        resolutionStrategy.set(ResolutionStrategy.AUTO)
        defaultToAppBundles.set(true)
        track.set(playTrack)
        releaseStatus.set(ReleaseStatus.COMPLETED)
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
