/*
 * Copyright (c) 2024. Foxikle
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.foxikle.customnpcs.internal;

import dev.foxikle.customnpcs.api.events.NpcInjectEvent;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InjectionManager {
    private final CustomNPCs plugin;
    private final InternalNpc npc;
    private final int INJECTION_DISTANCE;
    ConcurrentHashMap<UUID, Boolean> isVisible = new ConcurrentHashMap<>();
    private int task;

    public InjectionManager(CustomNPCs plugin, InternalNpc npc) {
        this.plugin = plugin;
        this.npc = npc;
        INJECTION_DISTANCE = (int) Math.pow(plugin.getConfig().getInt("InjectionDistance"), 2);
    }

    public void setup() {
        if (task != -1) shutDown();
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::checkForInjections, 0, plugin.getConfig().getInt("InjectionInterval")).getTaskId();
    }

    private void checkForInjections() {

        List<UUID> toRemove = new ArrayList<>(isVisible.keySet());

        for (Player player : Bukkit.getOnlinePlayers()) {

            // check for offline players
            if (isVisible.containsKey(player.getUniqueId())) {
                toRemove.remove(player.getUniqueId());
            }

            if (player.getWorld() != npc.getCurrentLocation().getWorld()) {
                if (plugin.isDebug()) {
                    plugin.getLogger().info(String.format("[DEBUG] Removing %s from %s's injection handler as they are in a different world.", player.getName(), npc.getSettings().getName()));
                }
                isVisible.remove(player.getUniqueId());
                continue;
            }

            double distance = player.getLocation().distanceSquared(npc.getCurrentLocation());
            if (distance > INJECTION_DISTANCE) {
                if (plugin.isDebug()) {
                    plugin.getLogger().info(String.format("[DEBUG] Tried to inject %s with %s, but they are too far away! (Distance^2: %f )", player.getName(), npc.getSettings().getName(), distance));
                }
                isVisible.put(player.getUniqueId(), false);
                continue;
            }

            if (distance <= INJECTION_DISTANCE && !isVisible.getOrDefault(player.getUniqueId(), false)) {
                NpcInjectEvent injectEvent = new NpcInjectEvent(player, npc, distance);
                Bukkit.getServer().getPluginManager().callEvent(injectEvent);
                if (injectEvent.isCancelled()) continue;
                npc.injectPlayer(player);
                isVisible.put(player.getUniqueId(), true);
            }
        }

        for (UUID uuid : toRemove) {
            if (plugin.isDebug()) {
                plugin.getLogger().info(String.format("[DEBUG] Removing %s from %s's injection handler! (likley offline)", uuid.toString(), npc.getSettings().getName()));
            }
            isVisible.remove(uuid);
        }
    }

    public void shutDown() {
        Bukkit.getScheduler().cancelTask(task);
        task = -1;
    }

}
