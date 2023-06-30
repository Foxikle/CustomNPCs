package dev.foxikle.customnpcs.runnables;

import dev.foxikle.customnpcs.CustomNPCs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class NameRunnable extends BukkitRunnable {
    private final Player player;
    private final CustomNPCs plugin;

    public NameRunnable(Player player, CustomNPCs plugin){
        this.player = player;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if(!plugin.nameWaiting.contains(player))
            this.cancel();
        player.sendTitle(ChatColor.GOLD + "Type NPC name in chat", ChatColor.YELLOW + "Supports & colors.", 0, 20, 0);
    }
}
