plugins {
    alias(libs.plugins.buildconfig)
    id("minecraft")
}

repositories {
    maven("https://maven.accident.space/repository/maven-public/")
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://cursemaven.com")
    mavenLocal()
}


dependencies {
    implementation("com.github.GTNewHorizons:CodeChickenLib:1.1.10:dev")
    implementation("com.github.GTNewHorizons:CodeChickenCore:1.1.13:dev")
    implementation("com.github.GTNewHorizons:NotEnoughItems:2.4.13-GTNH:dev")
    implementation("space.impact:packet_network:1.1.3")
    implementation("com.github.GT-IMPACT:VisualProspecting:1.3.0") { isTransitive = false }
    api("space.impact:forgelin:2.0.+") { isChanging = true }
    api("curse.maven:journeymap-32274:4500659")
}

val modId: String by extra

tasks.runClient.configure {
    extraArgs.addAll("--mixin", "mixins.$modId.json")
}
