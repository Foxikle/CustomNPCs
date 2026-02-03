/*
 * Copyright (c) 2024-2025. Foxikle
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
    id("java")
    id("io.freefair.lombok") version "9.0.0"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.inventivetalent.org/repository/public/")
}

dependencies {
    compileOnly("com.github.mqzn:Lotus:1.6.0")
    compileOnly("org.bstats:bstats-bukkit:3.1.0")
    compileOnly("me.clip:placeholderapi:2.12.1")
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("org.mineskin:java-client:3.0.6")
    compileOnly("org.mineskin:java-client-jsoup:3.0.6")
    compileOnly("dev.velix:imperat-bukkit:1.9.7")
    compileOnly("dev.velix:imperat-core:1.9.7")
}

val generateClassloader = tasks.register("generateClassloader") {

    val outputDir = file("$projectDir/build/generated/sources/classloader")
    val packageDir = File(outputDir, "dev/foxikle/customnpcs/internal/utils")
    val classloaderFile = File(packageDir, "GeneratedClassloader.java")

    doLast {
        packageDir.mkdirs()
        outputDir.mkdirs()
        classloaderFile.createNewFile()

        val deps = configurations.getByName("compileOnly")
            .dependencies
            .filterIsInstance<ModuleDependency>()
            .filter { it.group != "io.papermc.paper" && it.group != "me.clip" }
            .map { "${it.group}:${it.name}:${it.version}" }

        classloaderFile.writeText(
            """
            package dev.foxikle.customnpcs.internal.utils;
            
            import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
            import io.papermc.paper.plugin.loader.PluginLoader;
            import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
            import org.eclipse.aether.artifact.DefaultArtifact;
            import org.eclipse.aether.graph.Dependency;
            import org.eclipse.aether.repository.RemoteRepository;
            import org.jetbrains.annotations.NotNull;

            public class GeneratedClassloader implements PluginLoader {

                @Override
                public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
                    System.setProperty("bstats.relocatecheck", "false");
                    MavenLibraryResolver resolver = new MavenLibraryResolver();
                    resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://maven-central.storage-download.googleapis.com/maven2/").build()); // shut up paper 
                    resolver.addRepository(new RemoteRepository.Builder("inventivetalent", "default", "https://repo.inventivetalent.org/repository/public/").build());
                    resolver.addRepository(new RemoteRepository.Builder("foxikle", "default", "https://repo.foxikle.dev/public").build());
                    resolver.addRepository(new RemoteRepository.Builder("jitpack", "default", "https://jitpack.io").build());
                    
                    ${deps.joinToString("\n                    ") { "resolver.addDependency(new Dependency(new DefaultArtifact(\"$it\"), null));" }}

                    classpathBuilder.addLibrary(resolver);
                }
            }
            """.trimIndent()
        )

        println("Generated GeneratedClassloader.java at $classloaderFile")
    }
}

tasks.compileJava {
    dependsOn(generateClassloader)
    source(generateClassloader.map { layout.buildDirectory.dir("generated/sources/classloader").get() })
}

tasks {
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    javadoc {
        (options as StandardJavadocDocletOptions).tags("apiNote:a:API Note:")
        options.encoding = Charsets.UTF_8.name()
        exclude("**/internal/**", "**/versions/**")
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
        filesMatching("paper-plugin.yml") {
            expand("version" to version)
        }
    }

    compileJava {
        options.release = 17
    }

    jar {
        archiveClassifier = "core"
    }
}