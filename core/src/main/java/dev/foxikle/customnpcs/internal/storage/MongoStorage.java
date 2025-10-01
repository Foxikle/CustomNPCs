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

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import org.bson.Document;
import org.bson.types.Binary;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;


public class MongoStorage implements StorageProvider {

    private MongoClientSettings settings = null;
    private String database = null;
    private String document = null;

    /**
     * Initializes the storage provider. This could be connecting to a database, creating files, etc.
     */
    @Override
    public CompletableFuture<Void> init(CustomNPCs plugin) {

        return CompletableFuture.supplyAsync(() -> {
            ServerApi serverApi = ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .build();
            settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(Objects.requireNonNull(plugin.getConfig().getString("storage.mongo.connectionString"))))
                    .serverApi(serverApi)
                    .applyToLoggerSettings(builder -> {
                        builder.maxDocumentLength(1);
                    })
                    .build();
            database = Objects.requireNonNull(plugin.getConfig().getString("storage.mongo.database"));
            document = Objects.requireNonNull(plugin.getConfig().getString("storage.mongo.document"));
            plugin.getLogger().info("Successfully set up MongoDB storage!");
            return null;
        });
    }

    /**
     * Saves the given byte array to the storage provider. It should overwrite the data.
     *
     * @param data The byte array to save
     * @return if the save was successful
     */
    @Override
    public CompletableFuture<Boolean> save(String data) {
        checkState();
        return CompletableFuture.supplyAsync(() -> {
            try (MongoClient client = MongoClients.create(settings)) {
                MongoDatabase db = client.getDatabase(database);

                Document doc = new Document("_id", document).append("data", data);
                // replace it if it exists :)
                db.getCollection("customnpcs").replaceOne(Filters.eq("_id", document), doc, new ReplaceOptions().upsert(true));
                return true;

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Loads the saved byte array from the storage provider
     *
     * @return The array of bytes, or the loaded data.
     */
    @Override
    public CompletableFuture<String> load() {
        checkState();
        return CompletableFuture.supplyAsync(() -> {
            try (MongoClient client = MongoClients.create(settings)) {
                MongoDatabase db = client.getDatabase(database);

                Document doc = db.getCollection("customnpcs").find(Filters.eq("_id", document)).first();
                if (doc == null) return ""; // no data saved
                return doc.getString("data");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Shuts down the storage provider. This could close buffers, connections, etc.
     */
    @Override
    public void shutdown() {
        checkState();
    }

    private void checkState() {
        if (document == null) throw new IllegalStateException("Document is null");
        if (database == null) throw new IllegalStateException("Database is null");
        if (settings == null) throw new IllegalStateException("Mongo settings is not initialized");
    }
}
