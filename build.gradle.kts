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
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
}

dependencies {

    compileOnly("com.github.zorbeytorunoglu:kLib:0.0.4")

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.8.10")

    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")

    implementation(platform("com.intellectualsites.bom:bom-1.18.x:1.25"))

    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")

    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }

    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.14")

}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}