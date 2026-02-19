plugins {
    kotlin("jvm") version "2.1.10"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    `maven-publish`
}

group = "de.c4vxl"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // Paper API
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")

    // Command API
    implementation("dev.jorel:commandapi-paper-shade:11.1.0")
    implementation("dev.jorel:commandapi-kotlin-paper:11.1.0")
}

kotlin {
    jvmToolchain(21)
}

java {
    withSourcesJar()
    withJavadocJar()
}

// Publishing
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "$group"
            artifactId = "gamemanagementapi"
            version = "1.0.0"
        }
    }

    repositories {
        maven(layout.buildDirectory.dir("repo"))
    }
}

// Mojang mapped
paperweight {
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

// Plugin config
bukkit {
    name = "GameManager"
    description = "GameManager provides an API for easily managing multiple games on one server"
    main = "$group.gamemanager.GameManager"
    version = "1.0.0"
    apiVersion = "1.14" // 1.14+

    authors = listOf("c4vxl")
    website = "https://gma.c4vxl.de/"

    libraries = listOf(
        "org.jetbrains.kotlin:kotlin-stdlib:2.1.10",
        "dev.jorel:commandapi-paper-shade:11.1.0",
        "dev.jorel:commandapi-kotlin-paper:11.1.0"
    )
}