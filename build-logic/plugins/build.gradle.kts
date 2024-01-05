
plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.gradle.kotlin)
    implementation(libs.gradle.gitversion)
    implementation(libs.gradle.retro)
    implementation(libs.gradle.idea)
    implementation(libs.gradle.buildconfig)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
