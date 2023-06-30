package dev.foxikle.customnpcs.runnables;

import dev.foxikle.customnpcs.CustomNPCs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MessageRunnable extends BukkitRunnable {

    private Player player;

    public MessageRunnable(Player player){
        this.player = player;
    }

    @Override
    public void run() {
        if(!CustomNPCs.getInstance().messageWaiting.contains(player))
            this.cancel();
        player.sendTitle(ChatColor.GOLD + "Type message in chat", ChatColor.YELLOW + "Supports & colors.", 0, 20, 0);
    }
}
