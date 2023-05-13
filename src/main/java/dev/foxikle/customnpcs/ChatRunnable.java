package dev.foxikle.customnpcs;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ChatRunnable extends BukkitRunnable {

    private Player player;

    public ChatRunnable(Player player){
        this.player = player;
    }

    @Override
    public void run() {
        if(!CustomNPCs.getInstance().waiting.contains(player))
            this.cancel();
        player.sendTitle(ChatColor.GOLD + "Type command in chat", ChatColor.RED + "Do not include the slash.", 0, 20, 0);
    }
}
