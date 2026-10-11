import settings.getVersionMod

plugins {
    alias(libs.plugins.setup.minecraft)
    alias(libs.plugins.setup.publish)
    id(libs.plugins.buildconfig.get().pluginId)
}

val modId: String by extra
val modName: String by extra
val modGroup: String by extra
val modAdapter: String by extra

extra.set("modVersion",getVersionMod())

buildConfig {
    packageName(modGroup)
    buildConfigField("String", "MODID", "\"${modId}\"")
    buildConfigField("String", "MODNAME", "\"${modName}\"")
    buildConfigField("String", "VERSION", "\"${getVersionMod()}\"")
    buildConfigField("String", "GROUPNAME", "\"${modGroup}\"")
    buildConfigField("String", "MODADAPTER", "\"${modAdapter}\"")
    buildConfigField("String", "ASSETS", "\"virtualores\"")
    useKotlinOutput { topLevelConstants = true }
}

repositories {
    maven("https://maven.accident.space/repository/maven-public/") {
        mavenContent {
            includeGroup("space.impact")
            includeGroup("com.github.GTNewHorizons")
            includeGroupByRegex("space\\.impact\\..+")
        }
        credentials {
            username = System.getenv("MAVEN_USER") ?: "NONE"
            password = System.getenv("MAVEN_PASSWORD") ?: "NONE"
        }
    }
}

dependencies {
    api("com.github.GTNewHorizons:CodeChickenCore:1.4.22:dev")
    api("com.github.GTNewHorizons:NotEnoughItems:2.8.158-GTNH:dev")
    api("space.impact:Packet-network:1.1.8.dirty:dev")
    api("space.impact:VisualProspecting:1.3.2:dev")


    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${libs.versions.kotlin.get()}")

    runtimeOnlyNonPublishable("com.github.GTNewHorizons:waila:1.7.3:dev")
    runtimeOnlyNonPublishable(modUtils.deobfuscate("curse.maven:journeymap-32274:4500659"))
}
