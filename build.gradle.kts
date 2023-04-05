plugins {
    kotlin("jvm") version "1.8.10"
    application
}

group = "com.zorbeytorunoglu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {

    compileOnly("com.github.zorbeytorunoglu:kLib:0.0.4")

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.8.10")

    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")

}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}