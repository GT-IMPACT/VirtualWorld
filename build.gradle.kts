plugins {
    alias(libs.plugins.buildconfig)
    groovy
    id("minecraft")
    id("publish")
}

repositories {
    maven("https://maven.accident.space/repository/maven-public/")
    maven("https://jitpack.io")
    mavenCentral()
    mavenLocal()
}

val modId: String by extra
val modName: String by extra
val modGroup: String by extra
val modAssets: String by extra

buildConfig {
    packageName("space.impact.$modId")
    buildConfigField("String", "MODID", "\"${modId}\"")
    buildConfigField("String", "MODNAME", "\"${modName}\"")
    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("String", "GROUPNAME", "\"${modGroup}\"")
    buildConfigField("String", "ASSETS", "\"${modAssets}\"")
    useKotlinOutput { topLevelConstants = true }
}

dependencies {
    implementation("com.github.GTNewHorizons:NotEnoughItems:2.3.53-GTNH:dev")
    api("space.impact:forgelin:2.0.+") { isChanging = true }
}

apply(from = "runConf.gradle")