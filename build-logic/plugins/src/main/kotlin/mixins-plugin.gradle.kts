import com.gtnewhorizons.retrofuturagradle.shadow.com.gtnewhorizons.retrofuturagradle.fg12shadow.net.md_5.specialsource.util.FileLocator.getFile

plugins {
    id("base-plugin")
}

val modName: String by extra
val modId: String by extra
val modGroup: String by extra

val coreModClass: String? by extra
val accessTransformersFile: String? by extra
val useMixins: String by extra
val forceUseMixins: String? by extra

val mixinPlugin: String by extra
val mixinsPackage: String by extra
val mixingConfigRefMap = "mixins.$modId.refmap.json"
val mixinTmpDir = buildFile.path + File.separator + "tmp" + File.separator + "mixins"
val refMap = mixinTmpDir + File.separator + mixingConfigRefMap

tasks.processResources.configure {
    if (useMixins.toBoolean()) {
        from(refMap)
        dependsOn(tasks.compileJava)
    }
}

tasks.register("generateAssets") {
    onlyIf { useMixins.toBoolean() }
    doLast {
        val mixinConfigFile = File("src/main/resources/mixins.$modId.json")
        if (mixinConfigFile.canRead()) mixinConfigFile.createNewFile()
        if (!mixinConfigFile.exists()) {
            var mixinPluginLine = ""
            if (mixinPlugin.isNotEmpty()) {
                mixinPluginLine += "\n  \"plugin\": \"${modGroup}.${mixinPlugin}\", "
            }
            mixinConfigFile.writeText(
                """{
  "required": true,
  "minVersion": "0.8.5-GTNH",
  "package": "${modGroup}.${mixinsPackage}",${mixinPluginLine}
  "refmap": "$mixingConfigRefMap",
  "target": "@env(DEFAULT)",
  "compatibilityLevel": "JAVA_8",
  "mixins": [],
  "client": [],
  "server": []
}
"""
            )
        }
    }
}

fun getManifestAttributes(): Map<String, Any> {
    val manifestAttributes = mutableMapOf<String, Any>()
    if (coreModClass != null || useMixins.toBoolean()) {
        manifestAttributes["FMLCorePluginContainsFMLMod"] = true
    }
    if (coreModClass != null) {
        manifestAttributes["FMLCorePlugin"] = "$modGroup.$coreModClass"
    }
    if (useMixins.toBoolean()) {
        manifestAttributes["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
        manifestAttributes["MixinConfigs"] = "mixins.$modId.json"
        manifestAttributes["ForceLoadAsMod"] = true
    }
    if (accessTransformersFile != null) {
        manifestAttributes["FMLAT"] = accessTransformersFile.toString()
    }
    return manifestAttributes
}

tasks.register("generateAt") {
    if (accessTransformersFile != null) {
        for (atFile in accessTransformersFile!!.split(" ")) {
            val targetFile: String = "src/main/resources/META-INF/" + atFile.trim()
            val file = getFile(targetFile)
            if (!file.exists()) {
                throw GradleException("Could not resolve \"accessTransformersFile\"! Could not find $targetFile")
            }
            tasks.deobfuscateMergedJarToSrg.get().accessTransformerFiles.from(targetFile)
            tasks.srgifyBinpatchedJar.get().accessTransformerFiles.from(targetFile)
        }
    } else {
        var atsFound = false
        sourceSets.main.get().resources.files.forEach {
            if (it.name.lowercase().endsWith("_at.cfg")) {
                atsFound = true
                tasks.deobfuscateMergedJarToSrg.get().accessTransformerFiles.from(it)
                tasks.srgifyBinpatchedJar.get().accessTransformerFiles.from(it)
            }
        }
        if (atsFound) {
            logger.warn("Found and added access transformers in the resources folder, please configure gradle.properties to explicitly mention them by name")
        }
    }
}

tasks.register("devJar", Jar::class) {
    from(sourceSets["main"].output)
    archiveClassifier.set("dev")
    manifest { attributes(getManifestAttributes()) }
    dependsOn(tasks["generateAssets"])
    dependsOn(tasks["generateAt"])
}

tasks.named<Jar>("jar").configure {
    manifest { attributes(getManifestAttributes()) }
    dependsOn(tasks["devJar"])
}

val mixinProviderVersion = "0.1.14"
val mixinProviderSpecNoClassifer = "com.github.LegacyModdingMC.UniMixins:unimixins-mixin-1.7.10:${mixinProviderVersion}"

dependencies {
    if (useMixins.toBoolean()) {
        annotationProcessor("org.ow2.asm:asm-debug-all:5.0.3")
        annotationProcessor("com.google.guava:guava:24.1.1-jre")
        annotationProcessor("com.google.code.gson:gson:2.8.6")
        kapt(mixinProviderSpecNoClassifer)
        kapt("com.github.LegacyModdingMC.UniMixins:unimixins-gtnhmixins-1.7.10:$mixinProviderVersion")
        implementation(modUtils.enableMixins(mixinProviderSpecNoClassifer, mixingConfigRefMap))
        implementation("com.github.LegacyModdingMC.UniMixins:unimixins-gtnhmixins-1.7.10:$mixinProviderVersion:dev")
        implementation("com.github.LegacyModdingMC.UniMixins:unimixins-spongemixins-1.7.10:$mixinProviderVersion:dev")
        implementation(mixinProviderSpecNoClassifer)
    } else if (forceUseMixins.toBoolean()) {
        runtimeOnlyNonPublishable(mixinProviderSpecNoClassifer)
    }
}
