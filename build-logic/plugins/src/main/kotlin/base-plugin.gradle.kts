import com.gtnewhorizons.retrofuturagradle.MinecraftExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.kotlin

plugins {
    id("com.gtnewhorizons.retrofuturagradle")
    id("org.jetbrains.gradle.plugin.idea-ext")
    id("com.github.gmazzo.buildconfig")
    id("java-library")
    kotlin("jvm")
    kotlin("kapt")
}

configure<MinecraftExtension> {
    commonMinecraft(project)
}


val modId: String by extra
val modName: String by extra
val modGroup: String by extra
val modAssets: String by extra

buildConfig {
    packageName(modGroup)
    buildConfigField("String", "MODID", "\"${modId}\"")
    buildConfigField("String", "MODNAME", "\"${modName}\"")
    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("String", "GROUPNAME", "\"${modGroup}\"")
    buildConfigField("String", "ASSETS", "\"${modAssets}\"")
    useKotlinOutput { topLevelConstants = true }
}

repositories {
    mavenCentral()
    maven("https://maven.accident.space/repository/maven-public/")
    maven {
        // RetroFuturaGradle
        name = "GTNH Maven"
        url = uri("http://jenkins.usrv.eu:8081/nexus/content/groups/public/")
        isAllowInsecureProtocol = true
        mavenContent {
            includeGroup("com.gtnewhorizons")
            includeGroup("com.gtnewhorizons.retrofuturagradle")
        }
    }
    maven("https://maven.minecraftforge.net")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://plugins.gradle.org/m2/")
    mavenLocal()
}

kotlin {
    sourceSets {
        main.get().kotlin.srcDirs("build/generated/ksp/main/kotlin")
        main.get().kotlin.srcDirs("build/generated/ksp/main/kotlin")
    }
}

pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
    kotlin {
        jvmToolchain(8)
    }
    val disabledKotlinTaskList = listOf(
        "kaptGenerateStubsMcLauncherKotlin",
        "kaptGenerateStubsPatchedMcKotlin",
        "kaptGenerateStubsInjectedTagsKotlin",
        "compileMcLauncherKotlin",
        "compilePatchedMcKotlin",
        "compileInjectedTagsKotlin",
        "kaptMcLauncherKotlin",
        "kaptPatchedMcKotlin",
        "kaptInjectedTagsKotlin",
        "kspMcLauncherKotlin",
        "kspPatchedMcKotlin",
        "kspInjectedTagsKotlin",
    )

    tasks.configureEach {
        if (name in disabledKotlinTaskList) {
            enabled = false
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        vendor.set(JvmVendorSpec.AZUL)
    }
    withSourcesJar()
}

tasks.processResources.configure {
    val projVersion = project.version.toString()
    inputs.property("version", projVersion)

    filesMatching("mcmod.info") {
        expand(mapOf("modVersion" to projVersion))
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
        inheritOutputDirs = true

        sourceDirs = sourceDirs + file("build/generated/ksp/main/kotlin")
        generatedSourceDirs = generatedSourceDirs + file("build/generated/ksp/main/kotlin") + file("build/generated/ksp/test/kotlin")
    }
}
