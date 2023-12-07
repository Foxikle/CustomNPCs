package dev.foxikle.customnpcs.api;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * A class providing an interface using non NMS objects to use NPCs
 */
public class NPCApi {
    /**
     * A static instance of the plugin for API use.
     * The `NPCApi#initialize()` method must be called before using it.
     */
    protected static CustomNPCs plugin = null;

    /**
     * Initiailizes the API
     */
    public static void initialize() {
        plugin = JavaPlugin.getPlugin(CustomNPCs.class);
    }

    /**
     * Gets the NPC object by ID.
     * @param uuid the id of the NPC
     * @return the NPC object associated with the NPC
     * @throws NullPointerException if the specified UUID is null
     * @throws IllegalArgumentException if an NPC doesn't exist by that UUID
     */
    public static dev.foxikle.customnpcs.api.NPC getNPC(UUID uuid) throws NullPointerException, IllegalArgumentException {
        return new dev.foxikle.customnpcs.api.NPC(plugin.getNPCByID(uuid));
    }
}
