package dev.foxikle.customnpcs.internal.runnables;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class UrlRunnable extends BukkitRunnable {
    private final Player player;
    private final CustomNPCs plugin;

    /**
     * <p> Creates a runnable for collecting text input for the npc skin texture from a URL
     * </p>
     * @param plugin The instance to get who's waiting for the title
     * @param player The player to display the title to
     */
    public UrlRunnable(Player player, CustomNPCs plugin){
        this.player = player;
        this.plugin = plugin;
    }

    /**
     * <p> Repeatedly sends a title to the player with instructions for entering text
     * </p>
     */
    @Override
    public void run() {
        if(!plugin.urlWaiting.contains(player))
            this.cancel();
        player.sendTitle(ChatColor.GOLD + "Type the texture URL in chat", "", 0, 20, 0);
    }
}
