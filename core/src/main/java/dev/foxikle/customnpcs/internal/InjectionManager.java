package dev.foxikle.customnpcs.internal;

import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InjectionManager {
    private final CustomNPCs plugin;
    private final InternalNpc npc;
    private final int INJECTION_DISTANCE;

    private int task;

    ConcurrentHashMap<UUID, Boolean> isVisible = new ConcurrentHashMap<>();

    public InjectionManager(CustomNPCs plugin, InternalNpc npc) {
        this.plugin = plugin;
        this.npc = npc;
        INJECTION_DISTANCE = (int) Math.pow(plugin.getConfig().getInt("InjectionDistance"), 2);
    }

    public void setup(){
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::checkForInjections,0, plugin.getConfig().getInt("InjectionInterval")).getTaskId();
    }

    private void checkForInjections() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if(player.getWorld() != npc.getWorld()) continue;
            double distance = player.getLocation().distanceSquared(npc.getCurrentLocation());
            if (distance > INJECTION_DISTANCE) {
                isVisible.put(player.getUniqueId(), false);
                continue;
            }
            if(distance <= INJECTION_DISTANCE && !isVisible.getOrDefault(player.getUniqueId(), false)){
                npc.injectPlayer(player);
                isVisible.put(player.getUniqueId(), true);
            }
        }
    }

    public void shutDown() {
        Bukkit.getScheduler().cancelTask(task);
    }

}
