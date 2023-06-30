package dev.foxikle.customnpcs.runnables;

import dev.foxikle.customnpcs.CustomNPCs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TitleRunnable extends BukkitRunnable {

    private final Player player;
    private final CustomNPCs plugin;

    public TitleRunnable(Player player, CustomNPCs plugin){
        this.player = player;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if(!plugin.titleWaiting.contains(player))
            this.cancel();
        player.sendTitle(ChatColor.GOLD + "Type title in chat", ChatColor.YELLOW + "Supports & colors.", 0, 20, 0);
    }
}
