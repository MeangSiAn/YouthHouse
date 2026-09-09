plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

/**
 * VCS에 올리지 않는 프로퍼티 파일에서 값 하나를 읽는다.
 *
 * `providers.fileContents`를 거쳐야 파일 내용이 빌드 입력으로 추적되어
 * Configuration Cache가 깨지지 않는다. 파일이 없으면 빈 문자열이다.
 */
fun secretProperty(fileName: String, key: String): String = providers
    .fileContents(rootProject.layout.projectDirectory.file(fileName))
    .asText
    .map { text ->
        text.lineSequence()
            .map(String::trim)
            .firstOrNull { it.startsWith("$key=") }
            ?.substringAfter("=")
            ?.trim()
            .orEmpty()
    }
    .getOrElse("")

// 공공데이터포털 인증키는 더 이상 앱에 들어가지 않는다.
// 자체 백엔드(data.mosstis.com)가 대신 호출하므로 키는 서버에만 있다.

// 업로드 키스토어. keystore.properties(= .gitignore 대상)에 아래 네 항목을 둔다.
//   storeFile=/절대/경로/youthhouse-upload.jks   (~ 로 시작해도 된다)
//   storePassword=...
//   keyAlias=...
//   keyPassword=...
// 키스토어 파일 자체는 리포지토리 밖에 두고 별도로 백업한다. 잃어버리면 앱을 업데이트할 수 없다.
val userHome: String = providers.systemProperty("user.home").getOrElse("")
val releaseStorePath: String = secretProperty("keystore.properties", "storeFile")
    .replaceFirst(Regex("^~"), userHome)

// 파일 존재 여부(.exists())로 판단하지 않는다. 그 호출은 Configuration Cache의 입력으로
// 추적되지 않아서, 캐시가 "키스토어 없음"으로 굳으면 키를 나중에 넣어도 계속 debug 키로
// 서명된다. 무엇보다 경로에 오타가 났을 때 조용히 debug로 내려가 버리는 것이 가장 나쁘다.
// keystore.properties에 경로가 적혀 있으면 "서명할 의도가 있다"로 보고,
// 그 경로가 틀렸으면 서명 단계에서 요란하게 실패하도록 둔다.
val hasReleaseKeystore: Boolean = releaseStorePath.isNotBlank()

android {
    namespace = "com.ams.youthhouse"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.ams.youthhouse"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 자체 백엔드(공고·단지·실거래). local.properties에 MOSSTIS_API_KEY=... 로 둔다.
        buildConfigField("String", "MOSSTIS_BASE_URL", "\"https://data.mosstis.com/\"")
        buildConfigField(
            "String",
            "MOSSTIS_API_KEY",
            "\"${secretProperty("local.properties", "MOSSTIS_API_KEY")}\"",
        )
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = file(releaseStorePath)
                storePassword = secretProperty("keystore.properties", "storePassword")
                keyAlias = secretProperty("keystore.properties", "keyAlias")
                keyPassword = secretProperty("keystore.properties", "keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // 같은 기기에 release와 나란히 깔린다. 검증할 때 한쪽을 지우지 않아도 되고,
            // 무엇보다 "지금 보고 있는 게 어느 빌드인지" 헷갈리지 않는다.
            // 앱 이름은 app/src/debug/res 에서 "(D)"를 붙여 구분한다.
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            // R8 코드 축소·난독화·리소스 축소.
            //
            // AGP 9의 새 `optimization { enable = true }` DSL은 android.r8.gradual.support
            // 실험 플래그를 요구한다. 배포 빌드를 실험 플래그 위에 올릴 이유가 없어
            // 안정 경로인 isMinifyEnabled를 쓴다.
            //
            // 라이브러리들이 consumer keep 규칙을 함께 배포하므로 프로젝트 규칙은
            // 실제로 깨지는 지점에만 proguard-rules.pro에 추가한다.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            signingConfig = if (hasReleaseKeystore) {
                signingConfigs.getByName("release")
            } else {
                // 업로드 키스토어가 아직 없을 때의 폴백. R8이 켜진 릴리즈 빌드를
                // 실기기에 설치해 검증하려면 어떤 키로든 서명은 돼 있어야 한다.
                // Play는 debug 키로 서명된 번들을 거부하므로 이대로 배포될 위험은 없다.
                logger.warn(
                    "keystore.properties가 없어 release를 debug 키로 서명합니다. " +
                        "Play 업로드용 번들이 아닙니다.",
                )
                signingConfigs.getByName("debug")
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

hilt {
    enableAggregatingTask = true
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Compose 전용 앱이라 paging-runtime(recyclerview·livedata 동반) 대신 common + compose만 쓴다.
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
