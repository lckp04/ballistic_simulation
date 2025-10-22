plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.kotlinx.dataframe") version "1.0.0-Beta2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

// For Kandy
dependencies {
    implementation("org.jetbrains.kotlinx:kandy-lets-plot:0.8.0")
}
repositories {
    maven("https://packages.jetbrains.team/maven/p/kds/kotlin-ds-maven")
}
dependencies {
    implementation("org.jetbrains.kotlinx:kotlin-statistics-jvm:0.4.0")
}

// For Dataframe
dependencies {
    implementation("org.jetbrains.kotlinx:dataframe:1.0.0-Beta2")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

