plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.6.0"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("me.clip:placeholderapi:2.11.5")
    implementation(project(":api"))
    compileOnly(project(":core"))
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
}

tasks {
    java {
        toolchain.languageVersion = JavaLanguageVersion.of(21)
    }

    compileJava {
        options.release = 17
    }

    jar {
        archiveClassifier = "v1_20_R1"
    }
}