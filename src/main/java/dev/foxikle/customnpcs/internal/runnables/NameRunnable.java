package dev.foxikle.customnpcs.internal.runnables;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * The runnable for Name text collection
 */
public class NameRunnable extends BukkitRunnable {
    private final Player player;
    private final CustomNPCs plugin;

    /**
     * <p> Creates a runnable for collecting text input for the npc name
     * </p>
     * @param plugin The instance to get who's waiting for the title
     * @param player The player to display the title to
     */
    public NameRunnable(Player player, CustomNPCs plugin){
        this.player = player;
        this.plugin = plugin;
    }

    /**
     * <p> Repeatedly sends a title to the player with instructions for entering text
     * </p>
     */
    @Override
    public void run() {
        if(!plugin.nameWaiting.contains(player))
            this.cancel();
        player.sendTitle(ChatColor.GOLD + "Type NPC name in chat", ChatColor.YELLOW + "Supports & colors.", 0, 20, 0);
    }
}
