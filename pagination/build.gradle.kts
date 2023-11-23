@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    with(catalogPlugins.plugins) {
        plugin(android.library)
        plugin(kotlin.multiplatform)
        id(mersey.android.extension.id())
        id(mersey.kotlin.extension.id())
        plugin(kotlin.serialization)
        plugin(kotlin.kapt)
        //id(cocoapods.id())
    }
    `maven-publish-plugin`
}

android {
    namespace = "com.merseyside.pagination"
    compileSdk = Application.compileSdk

    defaultConfig {
        minSdk = Application.minSdk
    }
}

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
        publishLibraryVariantsGroupedByFlavor = true
    }

    iosArm64()
    iosX64()
    iosSimulatorArm64()

    applyDefaultHierarchyTemplate()

//    cocoapods {
//
//        framework {
//            summary = "A Kotlin multiplatform mobile library with useful utils"
//            homepage = "https://github.com/Merseyside/mersey-kmp-library/tree/master/utils-core"
//
//            version = multiplatformLibs.versions.mersey.kmm.get()
//        }
//
//        // https://github.com/tonymillion/Reachability
//        pod("Reachability") {
//            version = "3.2"
//        }
//    }
}

kotlinExtension {
    debug = true
    setCompilerArgs(
        "-Xcontext-receivers",
        "-Xinline-classes",
        "-Xskip-prerelease-check",
        "-opt-in=kotlin.RequiresOptIn"
    )
}

val commonLibs = listOf(
    common.serialization,
    common.mersey.time
)

val android = listOf(
    androidLibs.androidx.core,
    androidLibs.recyclerView,
    androidLibs.lifecycleLiveDataKtx
)

val merseyLibs = listOf(
    androidLibs.mersey.utils
)

val merseyMultiplatform = listOf(
    multiplatformLibs.mersey.utils
)

dependencies {
    if (isLocalKotlinExtLibrary()) {
        commonMainApi(project(Modules.MultiPlatform.MerseyLibs.kotlinExt))
    } else {
        commonMainApi(common.mersey.kotlin.ext)
    }
    commonMainApi(multiplatformLibs.bundles.moko.mvvm)
    commonLibs.forEach { commonMainImplementation(it) }
    merseyMultiplatform.forEach { lib -> commonMainImplementation(lib) }

    android.forEach { lib -> implementation(lib) }

    if (isLocalAndroidDependencies()) {
        implementation(project(Modules.Android.MerseyLibs.utils))
    } else {
        merseyLibs.forEach { lib -> implementation(lib) }
    }
}