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

package dev.foxikle.customnpcs.internal.storage;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
public interface StorageProvider {
    /**
     * Initializes the storage provider. This could be connecting to a database, creating files, etc.
     */
    CompletableFuture<Void> init(CustomNPCs plugin);

    /**
     * Saves the given byte array to the storage provider. It should overwrite the data.
     *
     * @param json The json array to save
     * @return if the save was successful
     */
    CompletableFuture<Boolean> save(String json);

    /**
     * Loads the saved byte array from the storage provider
     *
     * @return The array of json NPC data, or the loaded data.
     */
    CompletableFuture<String> load();

    /**
     * Shuts down the storage provider. This could close buffers, connections, etc.
     */
    void shutdown();
}
