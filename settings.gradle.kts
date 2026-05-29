pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }

    // >>> SUNTIKKAN BLOK FORCE RESOLUTION DI SINI <<<
    components {
        all {
            allVariants {
                withDependencies {
                    @Suppress("UNUSED_VARIABLE")
                    val metadataJvm = "org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0"

                    // Jika ada library (seperti Hilt) yang meminta kotlinx-metadata-jvm,
                    // paksa ganti versinya ke 0.9.0 yang sudah support Kotlin 2.0+
                    if (any { it.group == "org.jetbrains.kotlinx" && it.name == "kotlinx-metadata-jvm" }) {
                        removeAll { it.group == "org.jetbrains.kotlinx" && it.name == "kotlinx-metadata-jvm" }
                        add(metadataJvm)
                    }
                }
            }
        }
    }
}

rootProject.name = "insole"
include(":app")