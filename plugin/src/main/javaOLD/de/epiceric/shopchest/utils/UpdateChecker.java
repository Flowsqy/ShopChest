package de.epiceric.shopchest.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.epiceric.shopchest.ShopChest;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker {

    private ShopChest plugin;
    private String version;
    private String link;

    public UpdateChecker(ShopChest plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if an update is needed
     *
     * @return {@link UpdateCheckerResult#TRUE} if an update is available,
     *         {@link UpdateCheckerResult#FALSE} if no update is needed or
     *         {@link UpdateCheckerResult#ERROR} if an error occurred
     */
    public UpdateCheckerResult check() {
        try {
            plugin.getDebugLogger().debug("Checking for updates...");

            URL url = new URL("https://api.spiget.org/v2/resources/11431/versions?size=1&page=1&sort=-releaseDate");
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("User-Agent", "ShopChest/UpdateChecker");

            InputStreamReader reader = new InputStreamReader(conn.getInputStream());
            JsonElement element = JsonParser.parseReader(reader);

            if (element.isJsonArray()) {
                JsonObject result = element.getAsJsonArray().get(0).getAsJsonObject();
                String id = result.get("id").getAsString();
                version = result.get("name").getAsString();
                link = "https://www.spigotmc.org/resources/shopchest.11431/download?version=" + id;
            } else {
                plugin.getDebugLogger().debug("Failed to check for updates");
                plugin.getDebugLogger().debug("Result: " + element);
                return UpdateCheckerResult.ERROR;
            }

            if (compareVersion(version) == 1) {
                plugin.getDebugLogger().debug("No update found");
                return UpdateCheckerResult.FALSE;
            } else {
                plugin.getDebugLogger().debug("Update found: " + version);
                return UpdateCheckerResult.TRUE;
            }

        } catch (Exception e) {
            plugin.getDebugLogger().debug("Failed to check for updates");
            plugin.getDebugLogger().debug(e);
            return UpdateCheckerResult.ERROR;
        }
    }

    private int compareVersion(String version) {
        String[] t = plugin.getDescription().getVersion().split("\\-")[0].split("\\.");
        String[] o = version.split("\\-")[0].split("\\.");
        int[] t1 = new int[t.length];
        int[] o1 = new int[o.length];
        for (int i = 0; i < t.length; i++) {
            t1[i] = Integer.parseInt(t[i]);
        }
        for (int i = 0; i < o.length; i++) {
            o1[i] = Integer.parseInt(o[i]);
        }
        final int maxLength = Math.max(t1.length, o1.length);
        for (int i = 0; i < maxLength; i++) {
            final int left = i < t1.length ? t1[i] : 0;
            final int right = i < o1.length ? o1[i] : 0;
            if (left != right) {
                return left < right ? -1 : 1;
            }
        }
        return 0;
    }

    /**
     * @return Latest Version or <b>null</b> if no update is available
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return Download Link of the latest version of <b>null</b> if no update is available
     */
    public String getLink() {
        return link;
    }

    public enum UpdateCheckerResult {
        TRUE,
        FALSE,
        ERROR
    }


}
