plugins {
    alias(libs.plugins.buildconfig)
    id("minecraft")
}

repositories {
    maven("https://maven.accident.space/repository/maven-public/")
    maven("https://jitpack.io")
    mavenCentral()
    mavenLocal()
}


dependencies {
    implementation("com.github.GTNewHorizons:CodeChickenLib:1.1.10:dev")
    implementation("com.github.GTNewHorizons:CodeChickenCore:1.1.13:dev")
    implementation("com.github.GTNewHorizons:NotEnoughItems:2.4.13-GTNH:dev")
    api("space.impact:forgelin:2.0.+") { isChanging = true }
    implementation(fileTree(mapOf("dir" to "libs/", "include" to listOf("*.jar"))))
}

val modId: String by extra

tasks.runClient.configure {
    extraArgs.addAll("--mixin", "mixins.$modId.json")
}
