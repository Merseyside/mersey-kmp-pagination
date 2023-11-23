@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    with(catalogPlugins.plugins) {
        plugin(android.library)
        plugin(kotlin.android)
        id(mersey.android.extension.id())
        id(mersey.kotlin.extension.id())
        plugin(kotlin.kapt)
    }
    `maven-publish-plugin`
}

android {
    namespace = "com.merseyside.pagination.android"
    compileSdk = Application.compileSdk

    defaultConfig {
        minSdk = Application.minSdk
    }
}

kotlinExtension {
    debug = true
    setCompilerArgs(
        "-Xinline-classes",
        "-opt-in=kotlin.RequiresOptIn",
        "-Xskip-prerelease-check",
        "-Xcontext-receivers"
    )
}

val androidLibraries = listOf(
    common.serialization,
    androidLibs.appCompat,
    androidLibs.fragment,
    androidLibs.lifecycleViewModelSavedState
)

val merseyModules = listOf(
    Modules.Android.MerseyLibs.archy,
    Modules.Android.MerseyLibs.utils,
    Modules.Android.MerseyLibs.adapters,
    Modules.Android.MerseyLibs.adaptersCompose
)

val merseyLibs = listOf(
    androidLibs.mersey.archy,
    androidLibs.mersey.utils,
    androidLibs.mersey.adapters,
    androidLibs.mersey.adapters.compose
)

dependencies {
    api(projects.pagination)

    if (isLocalAndroidDependencies()) {
        merseyModules.forEach { module -> implementation(project(module)) }
    } else {
        merseyLibs.forEach { lib -> implementation(lib) }
    }

    androidLibraries.forEach { lib -> implementation(lib) }
    implementation(androidLibs.bundles.navigation)
}