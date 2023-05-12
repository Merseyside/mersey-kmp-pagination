allprojects {
    plugins.withId("org.gradle.maven-publish") {
        group = "io.github.merseyside"
        version = multiplatformLibs.versions.mersey.pagination.get()
    }
}

buildscript { // disable pod install tasks until find a solution
    repositories {
        gradlePluginPortal()
    }

    if (!isBuildIos()) {
        with(project.gradle.startParameter.excludedTaskNames) {
            add("podImport")
            add("podInstall")
            add("podGenIOS")
//            add("podSetupBuildReachabilityIphoneos")
//            add("podSetupBuildReachabilityIphonesimulator")
//            add("podBuildReachabilityIphoneos")
//            add("podBuildReachabilityIphonesimulator")
//            add("cinteropReachabilityIosX64")
//            add("cinteropReachabilityIosSimulatorArm64")
//            add("cinteropReachabilityIosArm64")
        }
    }
}

subprojects {
    gradle.taskGraph.whenReady {

        if (this@subprojects.name == "pagination-mersey-adapters") {
            tasks.matching { it.name == "javaDocReleaseGeneration" }.configureEach {
                // See: https://youtrack.jetbrains.com/issue/KTIJ-19005/JDK-17-PermittedSubclasses-requires-ASM9-exception-multiple-times-per-second-during-analysis
                enabled = false
            }
        }
    }
}

tasks.register("clean", Delete::class).configure {
    group = "build"
    delete(rootProject.buildDir)
}