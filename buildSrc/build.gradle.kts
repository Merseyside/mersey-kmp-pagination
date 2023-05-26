plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenLocal()
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    with(catalogGradle) {
        implementation(moko.mobileMultiplatform)
        implementation(mersey.gradlePlugins)
        implementation(android.gradle)
        implementation(kotlin.gradle)
        implementation(kotlin.serialization)
        implementation(nexusPublish)
        implementation(maven.publish.plugin)
    }
}