plugins {
    kotlin("jvm") version "1.9.23"
    id("io.papermc.paperweight.userdev") version "1.7.2"
    id("xyz.jpenilla.run-paper") version "2.3.0" // Adds runServer tasks for testing
}

group = "de.c4vxl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
    implementation("dev.jorel", "commandapi-bukkit-shade", "9.5.3")
    implementation("dev.jorel", "commandapi-bukkit-kotlin", "9.5.3")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "21"
    }
    jar {
        archiveFileName.set("GameManager.jar")
    }

    val copyJar by creating(Copy::class) {
        dependsOn(jar)
        from(file("build/libs/GameManager.jar"))
        into(file("run/plugins/"))
    }

    build {
        dependsOn(copyJar)
    }
}