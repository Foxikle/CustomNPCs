plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.5.12"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(mapOf("path" to ":api")))
    compileOnly(project(":core"))
    paperweight.paperDevBundle("1.20.3-R0.1-SNAPSHOT")
}

tasks {
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    jar {
        archiveClassifier = "v1_20_R3"
    }
}