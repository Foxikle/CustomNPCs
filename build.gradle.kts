/*
 * Copyright (c) 2024-2026. Foxikle
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

plugins {
    `java-library`
    id("com.vanniktech.maven.publish") version "0.37.0"
    `maven-publish`
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("io.github.goooler.shadow") version "8.1.8"
    id("io.papermc.paperweight.userdev") version "2.0.0-SNAPSHOT" apply false
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.inventivetalent.org/repository/public/")
    maven("https://repo.foxikle.dev/flameyos")
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":v26_2_R1", configuration = "default"))
    implementation(project(":v26_1_R1", configuration = "default"))
    implementation(project(":v1_21_R6", configuration = "default"))
    implementation(project(":v1_21_R5", configuration = "default"))
    implementation(project(":v1_21_R4", configuration = "default"))
    implementation(project(":v1_21_R3", configuration = "default"))
    implementation(project(":v1_21_R2", configuration = "default"))
    implementation(project(":v1_21_R1", configuration = "default"))
    implementation(project(":v1_21_R0", configuration = "default"))
    implementation(project(":v1_20_R4", configuration = "default"))
}

var pluginVersion = "1.8.0"

allprojects {
    group = "dev.foxikle"
    version = pluginVersion
    description = "CustomNPCs"
}

java.sourceCompatibility = JavaVersion.VERSION_25

val javadocJar = tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allJava)
}

mavenPublishing {
    coordinates("dev.foxikle", "customnpcs", version as String?)

    pom {
        name.set("CustomNPCs")
        description.set("The API for a powerful, configurable NPC plugin for Paper servers.")
        inceptionYear.set("2023")
        url.set("https://github.com/Foxikle/CustomNPCs/")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://raw.githubusercontent.com/Foxikle/CustomNPCs/refs/heads/master/LICENSE")
                distribution.set("https://raw.githubusercontent.com/Foxikle/CustomNPCs/refs/heads/master/LICENSE")
            }
        }
        developers {
            developer {
                id.set("foxikle")
                email.set("foxikle@cytonic.net")
                name.set("Foxikle")
                url.set("https://foxikle.dev")
            }
        }
        scm {
            url.set("https://github.com/Foxikle/CustomNPCs")
            connection.set("scm:git:git://github.com/Foxikle/CustomNPCs.git")
            developerConnection.set("scm:git:ssh://git@github.com/Foxikle/CustomNPCs.git")
        }
    }

    publishToMavenCentral()
    signAllPublications()
}

tasks {

    assemble {
        dependsOn(shadowJar)
    }

    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release = 25
    }
    javadoc {
        source = sourceSets["main"].allSource
        dependsOn("aggregatedJavadocs")
        (options as StandardJavadocDocletOptions).tags("apiNote:a:API Note:")
        options.encoding = Charsets.UTF_8.name()
        options.memberLevel = JavadocMemberLevel.PUBLIC
        exclude("**/internal/**", "**/versions/**")
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val props = mapOf(
            "name" to project.name,
            "version" to pluginVersion,
            "description" to project.description,
            "apiVersion" to "1.20"
        )
        inputs.properties(props)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        archiveClassifier.set("all")
        // This is used to place the file into a test server's plugin directory.
        destinationDirectory.set(
            file(
                providers.gradleProperty("plugin_dir").orElse(destinationDirectory.get().toString())
            )
        )
    }
}

tasks.register<Javadoc>("aggregatedJavadocs") {
    description = "Generate javadocs from all child projects as if it was a single project"
    group = "Documentation"
    title = "${project.name} $version API"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    (options as StandardJavadocDocletOptions).addStringOption("sourcepath", "")

    subprojects.forEach { proj ->
        proj.tasks.withType<Javadoc>().forEach { javadocTask ->
            source += javadocTask.source
            classpath += javadocTask.classpath
            excludes += javadocTask.excludes
            includes += javadocTask.includes
        }
    }
}
