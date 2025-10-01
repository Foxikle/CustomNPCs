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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.foxikle.customnpcs.internal.CustomNPCs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@SuppressWarnings("SqlNoDataSourceInspection")
public class MysqlStorage implements StorageProvider {

    private HikariConfig config = new HikariConfig();
    private String tableName;

    /**
     * Initializes the storage provider. This could be connecting to a database, creating files, etc.
     *
     * @param plugin the plugin instance for fetching config values
     */
    @Override
    @SuppressWarnings("SqlSourceToSinkFlow")
    public CompletableFuture<Void> init(CustomNPCs plugin) {
        config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + plugin.getConfig().getString("storage.mysql.hostname") + ":" + plugin.getConfig().getString("storage.mysql.port") + "/" + plugin.getConfig().getString("storage.mysql.database"));
        config.setUsername(plugin.getConfig().getString("storage.mysql.username"));
        config.setPassword(plugin.getConfig().getString("storage.mysql.password"));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(3000);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        tableName = getSafeTableName(plugin.getConfig().getString("storage.mysql.table"));

        return CompletableFuture.supplyAsync(() -> {
            try (HikariDataSource dataSource = new HikariDataSource(config)) {
                try (Connection connection = dataSource.getConnection()) {
                    PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS npc_data (id VARCHAR(255) PRIMARY KEY, data LONGTEXT)");
                    statement.executeUpdate();
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to create table", e);
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create database connection", e);
                throw new RuntimeException(e);
            }
            plugin.getLogger().info("Successfully set up MySQL storage!");
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
        return CompletableFuture.supplyAsync(() -> {
            try (HikariDataSource dataSource = new HikariDataSource(config)) {
                try (Connection connection = dataSource.getConnection()) {

                    // Prepare the SQL INSERT statement
                    String sql = "INSERT INTO npc_data (id, data) VALUES (?, ?) ON DUPLICATE KEY UPDATE data = VALUES(data)";
                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                        statement.setString(1, tableName);  // Set the id parameter
                        statement.setString(2, data); // Set the data (MEDIUMBLOB)

                        // Execute the insert query
                        statement.executeUpdate();
                        return true;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Database insertion failed", e);
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
        return CompletableFuture.supplyAsync(() -> {
            try (HikariDataSource dataSource = new HikariDataSource(config)) {
                try (Connection connection = dataSource.getConnection()) {

                    // Prepare the SQL statement
                    String sql = "SELECT data FROM npc_data WHERE id = ?";
                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                        statement.setString(1, tableName);  // Set the id parameter

                        // Execute the  query
                        ResultSet rs = statement.executeQuery();
                        String data = "";
                        while (rs.next()) {
                            // should only run once, as primary keys are unique.
                            data = rs.getString("data");
                        }
                        return data;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Database insertion failed", e);
            }
        });
    }

    /**
     * Shuts down the storage provider. This could close buffers, connections, etc.
     */
    @Override
    public void shutdown() {

    }

    private String getSafeTableName(String tableName) {
        if (tableName == null || !tableName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid table name: " + tableName + ". Only alphanumeric and underscores are allowed.");
        }
        return tableName;
    }
}
