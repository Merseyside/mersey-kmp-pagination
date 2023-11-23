allprojects {
    plugins.withId("org.gradle.maven-publish") {
        group = "io.github.merseyside"
        version = multiplatformLibs.versions.mersey.pagination.get()
    }

    task("testClasses").doLast {
        println("This is a dummy testClasses task")
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
    delete(rootProject.layout.buildDirectory)
}