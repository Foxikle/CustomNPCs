package dev.foxikle.customnpcs.runnables;

import dev.foxikle.customnpcs.CustomNPCs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionbarRunnable extends BukkitRunnable {
    private final Player player;
    private final CustomNPCs plugin;
    
    public ActionbarRunnable(Player player, CustomNPCs plugin){
        this.player = player;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if(!plugin.actionbarWaiting.contains(player))
            this.cancel();
        player.sendTitle(ChatColor.GOLD + "Type the actiobar in chat", ChatColor.YELLOW + "Supports & colors.", 0, 20, 0);
    }
}
