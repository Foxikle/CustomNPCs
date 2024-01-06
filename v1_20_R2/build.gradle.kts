plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.5.11"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":api"))
    compileOnly(project(":core"))
    paperweight.paperDevBundle("1.20.2-R0.1-SNAPSHOT")
}

tasks {
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    jar {
        archiveClassifier = "v1_20_R2"
    }
}