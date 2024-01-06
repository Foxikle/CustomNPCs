plugins {
    id("java")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    implementation("org.bstats:bstats-bukkit:3.0.2")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
}

tasks {
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
        filesMatching("plugin.yml") {
            expand("version" to version)
        }
    }

    jar {
        archiveClassifier = "core"
    }
}