plugins {
    id("java")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.inventivetalent.org/repository/public/")
}

dependencies {
    implementation("org.bstats:bstats-bukkit:3.0.2")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
    implementation("com.github.coderFlameyosFlow.WoodyMenus:core:2.0.6")
    implementation("org.mineskin:java-client:1.2.4-SNAPSHOT")
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