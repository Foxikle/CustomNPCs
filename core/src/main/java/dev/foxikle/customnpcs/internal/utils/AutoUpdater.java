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

package dev.foxikle.customnpcs.internal.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

/**
 * A class to handle notifying the user if an update is available.
 */
public class AutoUpdater {

    private final CustomNPCs plugin;
    @Getter
    private String newestVersion = "UNKNOWN";

    /**
     * The constructor for the updater class
     * @param plugin the main plugin instance
     */

    public AutoUpdater(CustomNPCs plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks the GitHub repository to see if there is a newer release.
     * @return if there is an update available
     */
    public boolean checkForUpdates() {
        try {
            String remoteVersion = getRemoteVersion();
            boolean updateAvailable = needUpdate(plugin.getPluginMeta().getVersion(), remoteVersion);

            if (updateAvailable) {
                plugin.getLogger().warning("A new version (" + remoteVersion + ") is available!");
                newestVersion = remoteVersion;
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
        }
        return false;
    }

    private String getRemoteVersion() throws IOException {
        String API_URL = "https://api.github.com/repos/foxikle/customnpcs/releases/latest";
        URL apiUrl = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

        try (InputStream responseStream = connection.getInputStream(); Scanner scanner = new Scanner(responseStream)) {
            String responseBody = scanner.useDelimiter("\\A").next();

            JsonParser jsonParser = new JsonParser();
            JsonObject releaseInfo = jsonParser.parse(responseBody).getAsJsonObject();

            if (isPreRelease(releaseInfo)) {
                return "";
            }
            return releaseInfo.get("tag_name").getAsString().replace("v", "");
        }
    }

    private boolean isPreRelease(JsonObject releaseInfo) {
        return releaseInfo.has("prerelease") && releaseInfo.get("prerelease").getAsBoolean();
    }

    private boolean needUpdate(String current, String remote) {
        if(Objects.equals(remote, "")) return false;
        String[] currentParts = current.split("\\-")[0].split("\\.");
        String[] remoteParts = remote.split("\\-")[0].split("\\.");

        int maxLength = Math.max(currentParts.length, remoteParts.length);

        for (int i = 0; i < maxLength; i++) {
            int currentVersion = (i < currentParts.length) ? getVersionPart(currentParts[i]) : 0;
            int remoteVersion = (i < remoteParts.length) ? getVersionPart(remoteParts[i]) : 0;

            if (currentVersion < remoteVersion) {
                return true; // current is lower, needs update
            } else if (currentVersion > remoteVersion) {
                return false; // greater version
            }
        }

        return false; // equal versions
    }

    private static int getVersionPart(String versionPart) {
        try {
            return Integer.parseInt(versionPart);
        } catch (NumberFormatException e) {
            // Handle non-numeric version parts, like "prex"
            return 0;
        }
    }
}
