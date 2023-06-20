package dev.foxikle.customnpcs.runnables;

import dev.foxikle.customnpcs.CustomNPCs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionbarRunnable extends BukkitRunnable {
    private Player player;

    public ActionbarRunnable(Player player){
        this.player = player;
    }

    @Override
    public void run() {
        if(!CustomNPCs.getInstance().actionbarWaiting.contains(player))
            this.cancel();
        player.sendTitle(ChatColor.GOLD + "Type the actiobar in chat", ChatColor.YELLOW + "Supports & colors.", 0, 20, 0);
    }
}
