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
    }
}

dependencies {
    implementation("com.github.GTNewHorizons:CodeChickenLib:1.1.10:dev")
    api("com.github.GTNewHorizons:CodeChickenCore:1.1.13:dev")
    api("com.github.GTNewHorizons:NotEnoughItems:2.4.13-GTNH:dev")
    api("space.impact:Packet-Network:1.1.8:dev")
    api("space.impact:VisualProspecting:1.3.2:dev")

    runtimeOnlyNonPublishable("net.industrial-craft:industrialcraft-2:2.2.828-experimental")
    runtimeOnlyNonPublishable("com.github.GTNewHorizons:waila:1.6.0")
}
