package dev.foxikle.customnpcs.runnables;

import dev.foxikle.customnpcs.CustomNPCs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class NameRunnable extends BukkitRunnable {
    private Player player;

    public NameRunnable(Player player){
        this.player = player;
    }

    @Override
    public void run() {
        if(!CustomNPCs.getInstance().commandWaiting.contains(player))
            this.cancel();
        player.sendTitle(ChatColor.GOLD + "Type NPC name in chat", ChatColor.RED + "Supports & ChatColors.", 0, 20, 0);
    }
}
