package dev.foxikle.customnpcs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AutoUpdater {

    private final String API_URL = "https://api.github.com/repos/foxikle/customnpcs/releases/latest";
    private final CustomNPCs plugin;

    public AutoUpdater(CustomNPCs plugin) {
        this.plugin = plugin;
    }

    public boolean checkForUpdates() {
        try {
            String currentVersion = plugin.getDescription().getVersion();
            String latestVersion = getLatestVersion();

            if (latestVersion != null && !isNewerPreRelease(currentVersion, latestVersion)) {
                plugin.getLogger().warning("A new version (" + latestVersion + ") is available!");
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
        }
        return false;
    }

    private String getLatestVersion() throws IOException {
        URL apiUrl = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

        try (InputStream responseStream = connection.getInputStream(); Scanner scanner = new Scanner(responseStream)) {
            String responseBody = scanner.useDelimiter("\\A").next();

            JsonParser jsonParser = new JsonParser();
            JsonObject releaseInfo = jsonParser.parse(responseBody).getAsJsonObject();

            if (isPreRelease(releaseInfo)) {
                return null;
            }

            return releaseInfo.get("tag_name").getAsString().replace("v", "");
        }
    }

    private boolean isNewerPreRelease(String currentVersion, String latestVersion) {
        String[] currentParts = currentVersion.split("-");
        String[] latestParts = latestVersion.split("-");

        // Compare versions without pre-release identifiers
        int comparison = currentParts[0].compareTo(latestParts[0]);
        if (comparison > 0) {
            return true;
        } else if (comparison < 0) {
            return false;
        }

        // Versions are equal, check pre-release identifiers
        if (currentParts.length > 1 && latestParts.length > 1) {
            return currentParts[1].compareTo(latestParts[1]) >= 0;
        }

        // No pre-release identifiers, versions are equal
        return false;
    }

    private boolean isPreRelease(JsonObject releaseInfo) {
        return releaseInfo.has("prerelease") && releaseInfo.get("prerelease").getAsBoolean();
    }
}
