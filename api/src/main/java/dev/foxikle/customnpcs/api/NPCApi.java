package dev.foxikle.customnpcs.api;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

/**
 * A class providing an interface using non NMS objects to use NPCs
 */
public class NPCApi {
    /**
     * A static instance of the plugin for API use.
     */
    protected static CustomNPCs plugin = JavaPlugin.getPlugin(CustomNPCs.class);

    /**
     * Initiailizes the API
     *
     * @deprecated since it's no longer necessary
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.8")
    public static void initialize() {
        plugin = JavaPlugin.getPlugin(CustomNPCs.class);
    }

    /**
     * Gets the NPC object by ID.
     *
     * @param uuid the id of the NPC
     * @return the NPC object associated with the NPC
     * @throws NullPointerException     if the specified UUID is null
     * @throws IllegalArgumentException if an NPC doesn't exist by that UUID
     */
    public static NPC getNPC(UUID uuid) throws NullPointerException, IllegalArgumentException {
        return new NPC(plugin.getNPCByID(uuid));
    }
}
