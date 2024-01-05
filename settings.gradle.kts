@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        maven("https://maven.accident.space/repository/maven-public/")
        maven("http://jenkins.usrv.eu:8081/nexus/content/groups/public/") {
            isAllowInsecureProtocol = true
        }
        maven("https://maven.minecraftforge.net")
        maven("https://plugins.gradle.org/m2/")
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.accident.space/repository/maven-public/")
        maven("http://jenkins.usrv.eu:8081/nexus/content/groups/public/") {
            isAllowInsecureProtocol = true
        }
        maven("https://maven.minecraftforge.net")
        maven("https://plugins.gradle.org/m2/")
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "VirtualWorld"
