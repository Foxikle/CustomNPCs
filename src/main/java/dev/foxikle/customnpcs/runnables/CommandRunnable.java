package dev.foxikle.customnpcs.runnables;

import dev.foxikle.customnpcs.CustomNPCs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandRunnable extends BukkitRunnable {

    private final Player player;
    private final CustomNPCs plugin;
    
    public CommandRunnable(Player player, CustomNPCs plugin){
        this.player = player;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if(!plugin.commandWaiting.contains(player))
            this.cancel();
        player.sendTitle(ChatColor.GOLD + "Type command in chat", ChatColor.RED + "Do not include the slash.", 0, 20, 0);
    }
}
