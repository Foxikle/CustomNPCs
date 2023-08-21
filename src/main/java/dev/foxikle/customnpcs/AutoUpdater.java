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

    public void checkForUpdates() {
        try {
            String latestVersion = getLatestVersion();

            if (latestVersion != null && !latestVersion.equals(plugin.getDescription().getVersion())) {
                plugin.getLogger().warning("A new version (" + latestVersion + ") is available!");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
        }
    }

    private String getLatestVersion() throws IOException {
        URL apiUrl = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

        try (InputStream responseStream = connection.getInputStream(); Scanner scanner = new Scanner(responseStream)) {
            String responseBody = scanner.useDelimiter("\\A").next();

            JsonParser jsonParser = new JsonParser();
            JsonObject releaseInfo = jsonParser.parse(responseBody).getAsJsonObject();

            return releaseInfo.get("tag_name").getAsString();
        }
    }
}
