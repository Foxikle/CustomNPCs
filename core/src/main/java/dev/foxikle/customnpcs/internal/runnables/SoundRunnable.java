package dev.foxikle.customnpcs.internal.runnables;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
/**
 * The runnable for sound text collection
 */
public class SoundRunnable extends BukkitRunnable {

    /**
     * The player to send the title to
     */
    private final Player player;

    /**
     * The main class instance
     */
    private final CustomNPCs plugin;

    /**
     * <p> Creates a runnable for collecting text input for the play sound Action
     * </p>
     * @param plugin The instance to get who's waiting for the title
     * @param player The player to display the title to
     */
    public SoundRunnable(Player player, CustomNPCs plugin){
        this.player = player;
        this.plugin = plugin;
    }

    /**
     * <p> Repeatedly sends a title to the player with instructions for entering the sound.
     * </p>
     */
    @Override
    public void run() {
        if(!plugin.soundWaiting.contains(player))
            this.cancel();
        player.sendTitle(ChatColor.GOLD + "To set the sound", ChatColor.YELLOW + "Use the /npc setsound <sound> command", 0, 20, 0);
    }
}
