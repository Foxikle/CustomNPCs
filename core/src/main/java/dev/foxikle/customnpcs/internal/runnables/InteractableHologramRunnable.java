package dev.foxikle.customnpcs.internal.runnables;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * The runnable for Message text collection
 */
public class InteractableHologramRunnable extends BukkitRunnable {

    private final Player player;
    private final CustomNPCs plugin;

    /**
     * <p> Creates a runnable for collecting text input for the send message Action
     * </p>
     * @param plugin The instance to get who's waiting for the title
     * @param player The player to display the title to
     */
    public InteractableHologramRunnable(Player player, CustomNPCs plugin){
        this.player = player;
        this.plugin = plugin;
    }

    /**
     * <p> Repeatedly sends a title to the player with instructions for entering text
     * </p>
     */
    @Override
    public void run() {
        if(!plugin.hologramWaiting.contains(player)) this.cancel();
        player.sendTitle(Utils.style("&6Type hologram text in chat"), Utils.style("&eUsing MiniMessage format"), 0, 20, 0);
    }
}
