package dev.foxikle.customnpcs.runnables;

import dev.foxikle.customnpcs.CustomNPCs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SoundRunnable extends BukkitRunnable {
    private Player player;

    public SoundRunnable(Player player){
        this.player = player;
    }

    @Override
    public void run() {
        if(!CustomNPCs.getInstance().soundWaiting.contains(player))
            this.cancel();
        player.sendTitle(ChatColor.GOLD + "To set the sound", ChatColor.YELLOW + "Use the /npc setsound <sound> command", 0, 20, 0);
    }
}
