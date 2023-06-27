package dev.foxikle.customnpcs.runnables;

import dev.foxikle.customnpcs.CustomNPCs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ServerRunnable extends BukkitRunnable {
    private final Player player;
    private final CustomNPCs plugin;

    public ServerRunnable(Player player, CustomNPCs plugin){
        this.player = player;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if(!plugin.serverWaiting.contains(player))
            this.cancel();
        player.sendTitle(ChatColor.GOLD + "Type the name of the server in chat", ChatColor.YELLOW + "Note: It should be EXACTLY what is the the bungeecord config.", 0, 20, 0);
    }
}
