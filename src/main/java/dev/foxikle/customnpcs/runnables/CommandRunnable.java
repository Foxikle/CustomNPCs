package dev.foxikle.customnpcs.runnables;

import dev.foxikle.customnpcs.CustomNPCs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandRunnable extends BukkitRunnable {

    private Player player;

    public CommandRunnable(Player player){
        this.player = player;
    }

    @Override
    public void run() {
        if(!CustomNPCs.getInstance().commandWaiting.contains(player))
            this.cancel();
        player.sendTitle(ChatColor.GOLD + "Type command in chat", ChatColor.RED + "Do not include the slash.", 0, 20, 0);
    }
}
