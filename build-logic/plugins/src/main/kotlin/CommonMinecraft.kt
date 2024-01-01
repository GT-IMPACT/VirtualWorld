@file:Suppress("unused")

import com.gtnewhorizons.retrofuturagradle.MinecraftExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.*

fun MinecraftExtension.commonMinecraft(project: Project) {
    minecraftSettings()

    val useMixins: String by project.extra
    if (useMixins.toBoolean())
        extraTweakClasses.add("org.spongepowered.asm.launch.MixinTweaker")

    val injectMCGenerics: String? by project.extra
    injectMissingGenerics.set(injectMCGenerics.toBoolean())

    project.registerRuntimeOnlyNonPublishable()
}

private fun MinecraftExtension.minecraftSettings() {
    mcVersion.set("1.7.10")
    username.set("Developer")
    groupsToExcludeFromAutoReobfMapping.addAll("com.diffplug", "com.diffplug.durian", "net.industrial-craft")
}

fun Project.registerRuntimeOnlyNonPublishable() {

    val runtimeOnlyNonPublishable: Configuration by configurations.creating {
        description = "Runtime only dependencies that are not published alongside the jar"
        isCanBeConsumed = false
        isCanBeResolved = false
    }

    listOf(configurations["runtimeClasspath"], configurations["testRuntimeClasspath"]).forEach {
        it.extendsFrom(runtimeOnlyNonPublishable)
    }
}

fun DependencyHandler.runtimeOnlyNonPublishable(dependencyNotation: Any) =
    add("runtimeOnlyNonPublishable", dependencyNotation)

fun DependencyHandler.implementation(dependencyNotation: Any) =
    add("implementation", dependencyNotation)

fun DependencyHandler.api(dependencyNotation: Any) =
    add("api", dependencyNotation)

fun DependencyHandler.projectImplementation(depName: String) =
    add("implementation", (project(mapOf("path" to depName))))

fun DependencyHandler.annotationProcessor(dependencyNotation: Any) =
    add("annotationProcessor", dependencyNotation)

fun DependencyHandler.kapt(dependencyNotation: Any) =
    add("kapt", dependencyNotation)

val Project.libs: LibrariesForLibs
    get() = the<LibrariesForLibs>()

val Project.sourceSets: SourceSetContainer
    get() = the<SourceSetContainer>()