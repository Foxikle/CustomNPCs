plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("me.clip:placeholderapi:2.11.5")
    implementation(project(mapOf("path" to ":api")))
    compileOnly(project(":core"))
    paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")
}

tasks {
    java {
        toolchain.languageVersion = JavaLanguageVersion.of(21)
    }

    compileJava {
        options.release = 21
    }

    jar {
        archiveClassifier = "v1_21_R0"
    }
}