package dev.foxikle.customnpcs.internal.runnables;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.Utils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class FacingDirectionRunnable extends BukkitRunnable {

    private final CustomNPCs plugin;
    private final Player player;

    public FacingDirectionRunnable(CustomNPCs plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void go(){
        runTaskTimer(plugin, 0, 10);
    }

    @Override
    public void run() {
        if(!plugin.facingWaiting.contains(player)) cancel();
        player.sendTitle(Utils.style("&6Look where you want the NPC to"), Utils.style("&eThen type 'confirm' in chat."), 0, 20, 0);
    }
}
