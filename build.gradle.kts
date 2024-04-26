plugins {
    `java-library`
    `maven-publish`
    id("xyz.jpenilla.run-paper") version "2.2.4"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.inventivetalent.org/repository/public/")
}

dependencies {
    implementation(project(":api"))
    implementation(project(":core"))
    implementation(project(":v1_20_R3", "reobf"))
    implementation(project(":v1_20_R2", "reobf"))
    implementation(project(":v1_20_R1", "reobf"))
}

allprojects {
    group = "dev.foxikle"
    version = "1.6.1"
    description = "CustomNPCs"
}

java.sourceCompatibility = JavaVersion.VERSION_17

publishing {
    repositories {
        maven {
            name = "FoxiklePublicRepository"
            url = uri("https://repo.foxikle.dev/public")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            artifact(tasks["shadowJar"])
        }
    }
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
        options.release.set(17)
    }
    javadoc {
        dependsOn("aggregatedJavadocs")
        (options as StandardJavadocDocletOptions).tags("apiNote:a:API Note:")
        options.encoding = Charsets.UTF_8.name()
        exclude("**/internal/**", "**/versions/**")
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val props = mapOf(
                "name" to project.name,
                "version" to project.version,
                "description" to project.description,
                "apiVersion" to "1.20"
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("org.bstats", "dev.foxikle.dependencies.bstats")
        destinationDirectory.set(file(providers.gradleProperty("plugin_dir").get()))
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

