plugins {
    `java-library`
    `maven-publish`
    //id("io.papermc.paperweight.userdev") version "1.5.10"
    id("xyz.jpenilla.run-paper") version "2.2.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenLocal()
    mavenCentral()
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
    version = "1.6-pre3"
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
            println("Artifacts:$artifacts")
            artifact(tasks["jar"]) {
                print(classifier)
                //classifier = null
            }
            from(components["java"])
        }
    }
}

tasks {
    assemble {
        //dependsOn(reobfJar)
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
        options.encoding = Charsets.UTF_8.name()
        exclude("**/internal/**", "**/versions/**");
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

    //reobfJar {
    //    outputJar.set(layout.buildDirectory.file("C:/Users/tscal/Desktop/testserver/plugins/CustomNPCs-${project.version}.jar"))
    //}

    shadowJar {
        archiveClassifier.set("")
        relocate("org.bstats", "dev.foxikle.dependencies.bstats")
    }
}

