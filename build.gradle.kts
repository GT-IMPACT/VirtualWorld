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
            includeGroupByRegex("space\\.impact\\..+")
        }
        credentials {
            username = System.getenv("MAVEN_USER") ?: "NONE"
            password = System.getenv("MAVEN_PASSWORD") ?: "NONE"
        }
    }
}

dependencies {
//    api("com.github.GTNewHorizons:CodeChickenCore:1.3.11:dev")
//    api("com.github.GTNewHorizons:NotEnoughItems:2.6.19-GTNH:dev")
    api("space.impact:Packet-network:1.1.8.dirty:dev")
    api("space.impact:VisualProspecting:1.3.2:dev")

//    runtimeOnlyNonPublishable("com.github.GTNewHorizons:waila:1.8.1")

    api(fileTree(mapOf("dir" to "libs/", "include" to listOf("*.jar"))))
}
