@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    with(catalogPlugins.plugins) {
        plugin(android.library)
        plugin(kotlin.multiplatform)
        plugin(kotlin.serialization)
        id(mersey.kotlin.extension.id())
        id(mersey.android.extension.id())
        id(cocoapods.id())
    }
}

android {
    namespace = "com.merseyside.sample.mppLibrary"
    compileSdk = Application.compileSdk

    defaultConfig {
        minSdk = Application.minSdk
    }
}

kotlin {
    androidTarget()

    iosArm64()
    iosX64()
    iosSimulatorArm64()

    applyDefaultHierarchyTemplate()

    cocoapods {
        framework {
            summary = "KMM Mersey library"
            homepage = "https://github.com/Merseyside/mersey-kmp-library"
            baseName = "MultiPlatformLibrary"
            version = multiplatformLibs.versions.mersey.kmm.get()
            podfile = project.file("../ios-app/Podfile")

            export(common.mersey.kotlin.ext)
        }
    }
}

kotlinExtension {
    debug = true
}

val multiplatform = listOf(
    multiplatformLibs.koin
)


dependencies {
    commonMainApi(common.mersey.kotlin.ext)
    commonMainImplementation(projects.pagination)

    multiplatform.forEach { lib ->
        commonMainImplementation(lib)
    }
}

