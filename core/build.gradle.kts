plugins {
    id("java")
}

repositories {
    mavenCentral()
    mavenLocal()
    //maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.inventivetalent.org/repository/public/")
}

dependencies {
    implementation("org.bstats:bstats-bukkit:3.1.0")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
    //implementation("com.github.coderFlameyosFlow.WoodyMenus:core:2.0.6")
    implementation("me.flame.menus:core:2.0.0")
    implementation("org.mineskin:java-client:1.2.4-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
}

tasks {
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    javadoc {
        (options as StandardJavadocDocletOptions).tags("apiNote:a:API Note:")
        options.encoding = Charsets.UTF_8.name()
        exclude("**/internal/**", "**/versions/**")
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