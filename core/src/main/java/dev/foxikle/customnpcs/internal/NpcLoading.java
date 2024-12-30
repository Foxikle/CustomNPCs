/*
 * Copyright (c) 2024. Foxikle
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

package dev.foxikle.customnpcs.internal;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class NpcLoading implements PluginLoader {
    /**
     * Called by the server to allows plugins to configure the runtime classpath that the plugin is run on.
     * This allows plugin loaders to configure dependencies for the plugin where jars can be downloaded or
     * provided during runtime.
     *
     * @param classpathBuilder a mutable classpath builder that may be used to register custom runtime dependencies
     *                         for the plugin the loader was registered for.
     */
    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        System.setProperty("bstats.relocatecheck", "false");
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder("foxikle", "default", "https://repo.foxikle.dev/public").build());
        resolver.addRepository(new RemoteRepository.Builder("inventivetalent", "default", "https://repo.inventivetalent.org/repository/public/").build());
        resolver.addRepository(new RemoteRepository.Builder("jitpack", "default", "https://jitpack.io").build());
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build());

        resolver.addDependency(new Dependency(new DefaultArtifact("org.mineskin:java-client:1.2.4-SNAPSHOT"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.bstats:bstats-bukkit:3.1.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.github.Mqzn:Lotus:1.1.7"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("dev.velix:imperat-core:1.3.2"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("dev.velix:imperat-bukkit:1.3.2"), null));

        classpathBuilder.addLibrary(resolver);
    }
}
