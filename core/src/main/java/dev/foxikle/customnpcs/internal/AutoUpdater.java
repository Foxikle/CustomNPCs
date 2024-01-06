package dev.foxikle.customnpcs.internal;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

    private final String API_URL = "https://api.github.com/repos/foxikle/customnpcs/releases/latest";
    private final CustomNPCs plugin;
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

    /**
     * Gets the latest version
     * <p>
     * ** May not be the latest version, as this value is cached on server start. **
     * @return the latest (cached) version
     */
    public String getNewestVersion() {
        return newestVersion;
    }
}
