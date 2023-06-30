package dev.foxikle.customnpcs.runnables;

import dev.foxikle.customnpcs.CustomNPCs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * The runnable for ActionBar text collection
 */
public class ActionbarRunnable extends BukkitRunnable {

    private final Player player;
    private final CustomNPCs plugin;

    /**
     * <p> Creates a runnable for collecting text input for the actionbar Action
     * </p>
     * @param plugin The instance to get who's waiting for the title
     * @param player The player to display the title to
     */
    public ActionbarRunnable(Player player, CustomNPCs plugin){
        this.player = player;
        this.plugin = plugin;
    }

    /**
     * <p> Repeatedly sends a title to the player with instructions for entering text
     * </p>
     */
    @Override
    public void run() {
        if(!plugin.actionbarWaiting.contains(player))
            this.cancel();
        player.sendTitle(ChatColor.GOLD + "Type the actiobar in chat", ChatColor.YELLOW + "Supports & colors.", 0, 20, 0);
    }
}
