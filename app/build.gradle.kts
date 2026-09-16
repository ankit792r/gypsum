import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.system74.gypsum"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.system74.gypsum"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }

        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.panama.core)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

val sampleAbis = mapOf(
    "arm64-v8a" to "aarch64-linux-android30",
    "x86_64" to "x86_64-linux-android30",
)

tasks.register("compileSampleBinaries") {
    group = "gypsum"
    description = "Cross-compile sample hosted binaries into assets"
    notCompatibleWithConfigurationCache("Reads NDK path from local.properties at execution time")

    val helloSourceFile = layout.projectDirectory.file("src/main/samples/hello/main.c").asFile
    val localPropertiesFile = rootProject.layout.projectDirectory.file("local.properties").asFile
    val outputByAbi = sampleAbis.keys.associateWith { abi ->
        layout.projectDirectory.file("src/main/assets/bundled/hello-cli/bin/$abi/libhello_cli.so").asFile
    }

    inputs.file(helloSourceFile)
    inputs.file(localPropertiesFile)
    outputByAbi.values.forEach { outputs.file(it) }

    doLast {
        val localProps = Properties().apply {
            localPropertiesFile.inputStream().use { load(it) }
        }
        val sdkDir = localProps.getProperty("sdk.dir")
            ?: error("sdk.dir is not set in local.properties")
        val ndkRoot = File("$sdkDir/ndk")
        val ndkDir = ndkRoot.listFiles()
            ?.filter { it.isDirectory }
            ?.maxByOrNull { it.name }
            ?: error("Android NDK not found under $ndkRoot")

        val clang = File("$ndkDir/toolchains/llvm/prebuilt/linux-x86_64/bin/clang")
        if (!clang.exists()) {
            error("NDK clang not found: ${clang.absolutePath}")
        }

        sampleAbis.forEach { (abi, triple) ->
            val outFile = outputByAbi.getValue(abi)
            outFile.parentFile.mkdirs()

            val process = ProcessBuilder(
                clang.absolutePath,
                "--target=$triple",
                "-shared",
                "-fPIC",
                "-O2",
                "-o",
                outFile.absolutePath,
                helloSourceFile.absolutePath,
            ).inheritIO().start()

            val exitCode = process.waitFor()
            if (exitCode != 0) {
                error("Failed to compile sample binary for $abi (exit $exitCode)")
            }
        }
    }
}

tasks.named("preBuild").configure {
    dependsOn("compileSampleBinaries")
}