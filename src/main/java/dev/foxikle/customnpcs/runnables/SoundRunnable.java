package dev.foxikle.customnpcs.runnables;

import dev.foxikle.customnpcs.CustomNPCs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SoundRunnable extends BukkitRunnable {
    private final Player player;
    private final CustomNPCs plugin;

    public SoundRunnable(Player player, CustomNPCs plugin){
        this.player = player;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if(!plugin.soundWaiting.contains(player))
            this.cancel();
        player.sendTitle(ChatColor.GOLD + "To set the sound", ChatColor.YELLOW + "Use the /npc setsound <sound> command", 0, 20, 0);
    }
}
