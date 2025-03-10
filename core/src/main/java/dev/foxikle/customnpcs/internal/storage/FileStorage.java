/*
 * Copyright (c) 2025. Foxikle
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

package dev.foxikle.customnpcs.internal.storage;

import dev.foxikle.customnpcs.internal.CustomNPCs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class FileStorage implements StorageProvider {

    public static final File FILE = new File("plugins/CustomNPCs/npcs.dat");

    /**
     * Creates files
     */
    @Override
    public CompletableFuture<Void> init(CustomNPCs plugin) {
        return CompletableFuture.supplyAsync(() -> {
            if (!FILE.exists()) {
                FILE.getParentFile().mkdirs();
                try {
                    FILE.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create local storage file.", e);
                }
            }
            plugin.getLogger().info("Successfully set up File storage!");
            return null;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Boolean> save(byte[] data) {
        return CompletableFuture.supplyAsync(() -> {
            try (FileOutputStream fos = new FileOutputStream(FILE)) {
                fos.write(data);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save local storage file.", e);
            }
            return true;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<byte[]> load() {
        return CompletableFuture.supplyAsync(() -> {
            try (FileInputStream fis = new FileInputStream(FILE)) {
                return fis.readAllBytes();
            } catch (Exception e) {
                throw new RuntimeException("Failed to load local storage file.", e);
            }
        });
    }

    /**
     * Does nothing.
     */
    @Override
    public void shutdown() {
    }
}
