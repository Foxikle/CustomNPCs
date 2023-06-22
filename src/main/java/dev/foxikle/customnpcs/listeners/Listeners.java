package dev.foxikle.customnpcs.listeners;

import dev.foxikle.customnpcs.Action;
import dev.foxikle.customnpcs.CustomNPCs;
import dev.foxikle.customnpcs.NPC;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Collection;
import java.util.List;

public class Listeners implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
        if (e.getHand() == EquipmentSlot.HAND) {
            if (e.getRightClicked().getType() == EntityType.PLAYER) {
                Player player = e.getPlayer();
                Player rightClicked = (Player) e.getRightClicked();
                ServerPlayer sp = ((CraftPlayer) rightClicked).getHandle();
                NPC npc = CustomNPCs.getInstance().getNPCByID(sp.getUUID());
                if (player.hasPermission("customnpcs.edit") && player.isSneaking()) {
                    player.performCommand("npc edit " + npc.getUUID());
                } else if (npc.isClickable()) {
                    npc.getActions().forEach(action -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.getCommand(e.getPlayer())));
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (CustomNPCs.getInstance().commandWaiting.contains(e.getPlayer())) {
            Bukkit.getScheduler().runTask(CustomNPCs.getInstance(), () -> {
                CustomNPCs.getInstance().commandWaiting.remove(e.getPlayer());
                Action action = CustomNPCs.getInstance().editingActions.get(e.getPlayer());
                List<String> currentArgs = action.getArgs();
                currentArgs.clear();
                currentArgs.addAll(List.of(e.getMessage().split(" ")));
                e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully set command to be '" + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', e.getMessage()) + ChatColor.RESET + "" + ChatColor.GREEN + "'");
                Bukkit.getScheduler().runTask(CustomNPCs.getInstance(), () -> e.getPlayer().openInventory(CustomNPCs.getInstance().menuCores.get(e.getPlayer()).getActionCustomizerMenu(action)));
            });
            e.setCancelled(true);
        } else if (CustomNPCs.getInstance().nameWaiting.contains(e.getPlayer())) {
            CustomNPCs.getInstance().nameWaiting.remove(e.getPlayer());
            CustomNPCs.getInstance().menuCores.get(e.getPlayer()).getNpc().setName(e.getMessage());
            e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully set name to be '" + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', e.getMessage()) + ChatColor.RESET + ChatColor.GREEN + "'");
            Bukkit.getScheduler().runTask(CustomNPCs.getInstance(), () -> e.getPlayer().openInventory(CustomNPCs.getInstance().menuCores.get(e.getPlayer()).getMainMenu()));
            e.setCancelled(true);
        } else if (CustomNPCs.getInstance().titleWaiting.contains(e.getPlayer())) {
            Bukkit.getScheduler().runTask(CustomNPCs.getInstance(), () -> {
                CustomNPCs.getInstance().titleWaiting.remove(e.getPlayer());
                List<String> args = CustomNPCs.getInstance().editingActions.get(e.getPlayer()).getArgsCopy();
                Action action = CustomNPCs.getInstance().editingActions.get(e.getPlayer());
                List<String> currentArgs = action.getArgs();
                currentArgs.clear();
                currentArgs.add(0, args.get(0));
                currentArgs.add(1, args.get(1));
                currentArgs.add(2, args.get(2));
                currentArgs.addAll(List.of(e.getMessage().split(" ")));
                e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully set title to be '" + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', e.getMessage()) + ChatColor.RESET + "" + ChatColor.GREEN + "'");
                Bukkit.getScheduler().runTask(CustomNPCs.getInstance(), () -> e.getPlayer().openInventory(CustomNPCs.getInstance().menuCores.get(e.getPlayer()).getActionCustomizerMenu(action)));
            });
            e.setCancelled(true);
        } else if (CustomNPCs.getInstance().messageWaiting.contains(e.getPlayer())) {
            Bukkit.getScheduler().runTask(CustomNPCs.getInstance(), () -> {
                CustomNPCs.getInstance().messageWaiting.remove(e.getPlayer());
                Action action = CustomNPCs.getInstance().editingActions.get(e.getPlayer());
                List<String> currentArgs = action.getArgs();
                currentArgs.clear();
                currentArgs.addAll(List.of(e.getMessage().split(" ")));
                e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully set message to be '" + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', e.getMessage()) + ChatColor.RESET + "" + ChatColor.GREEN + "'");
                Bukkit.getScheduler().runTask(CustomNPCs.getInstance(), () -> e.getPlayer().openInventory(CustomNPCs.getInstance().menuCores.get(e.getPlayer()).getActionCustomizerMenu(action)));
            });
            e.setCancelled(true);
        } else if (CustomNPCs.getInstance().serverWaiting.contains(e.getPlayer())) {
            Bukkit.getScheduler().runTask(CustomNPCs.getInstance(), () -> {
                CustomNPCs.getInstance().serverWaiting.remove(e.getPlayer());
                Action action = CustomNPCs.getInstance().editingActions.get(e.getPlayer());
                List<String> currentArgs = action.getArgs();
                currentArgs.clear();
                currentArgs.addAll(List.of(e.getMessage().split(" ")));
                e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully set server to be '" + ChatColor.RESET +  e.getMessage() + ChatColor.RESET + "" + ChatColor.GREEN + "'");
                Bukkit.getScheduler().runTask(CustomNPCs.getInstance(), () -> e.getPlayer().openInventory(CustomNPCs.getInstance().menuCores.get(e.getPlayer()).getActionCustomizerMenu(action)));
            });
            e.setCancelled(true);
        } else if (CustomNPCs.getInstance().actionbarWaiting.contains(e.getPlayer())) {
            Bukkit.getScheduler().runTask(CustomNPCs.getInstance(), () -> {
                CustomNPCs.getInstance().actionbarWaiting.remove(e.getPlayer());
                Action action = CustomNPCs.getInstance().editingActions.get(e.getPlayer());
                List<String> currentArgs = action.getArgs();
                currentArgs.clear();
                currentArgs.addAll(List.of(e.getMessage().split(" ")));
                e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully set actiobar to be '" + ChatColor.RESET +  ChatColor.translateAlternateColorCodes('&', e.getMessage()) + ChatColor.RESET + "" + ChatColor.GREEN + "'");
                Bukkit.getScheduler().runTask(CustomNPCs.getInstance(), () -> e.getPlayer().openInventory(CustomNPCs.getInstance().menuCores.get(e.getPlayer()).getActionCustomizerMenu(action)));
            });
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent e) {
            for (NPC npc : CustomNPCs.getInstance().getNPCs()) {
                npc.injectPlayer(e.getPlayer());
            }
            Bukkit.getScheduler().runTaskLater(CustomNPCs.getInstance(), () -> {
                for (NPC npc : CustomNPCs.getInstance().getNPCs()) {
                    npc.injectPlayer(e.getPlayer());
                }
        }, 10);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        for (NPC npc : CustomNPCs.getInstance().npcs.values()) {
            if (getDistance(e.getPlayer().getLocation(), npc.getCurrentLocation()) <= 5) {
                npc.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftPlayer) player).getHandle(), EntityAnchorArgument.Anchor.EYES);
            } else if (getDistance(e.getFrom(), npc.getCurrentLocation()) >= 48 && getDistance(e.getTo(), npc.getCurrentLocation()) <= 48) {
                npc.injectPlayer(player);
            }
            if (getDistance(e.getPlayer().getLocation(), npc.getCurrentLocation()) > 5) {
                Collection<Entity> entities = npc.getWorld().getNearbyEntities(npc.getCurrentLocation(), 2.5, 2.5, 2.5);
                entities.removeIf(entity -> entity.getScoreboardTags().contains("NPC"));
                for (Entity en : entities) {
                    if (en.getType() == EntityType.PLAYER) {
                        Player p = (Player) en;
                        npc.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftPlayer) p).getHandle(), EntityAnchorArgument.Anchor.EYES);
                        return;
                    }
                }
                 npc.setYBodyRot((float) npc.getFacingDirection());
                 npc.setYRot((float) npc.getFacingDirection());
                 npc.setYHeadRot((float) npc.getFacingDirection());
            }
        }
    }
    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        for (NPC npc : CustomNPCs.getInstance().npcs.values()) {
            if (getDistance(e.getPlayer().getLocation(), npc.getSpawnLoc()) <= 5) {
                npc.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftPlayer) player).getHandle(), EntityAnchorArgument.Anchor.EYES);
            } else if (getDistance(e.getFrom(), npc.getSpawnLoc()) >= 48 && getDistance(e.getTo(), npc.getSpawnLoc()) <= 48) {
                npc.injectPlayer(player);
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        for (NPC npc : CustomNPCs.getInstance().npcs.values()) {
            if(npc.getPlayer().getBukkitEntity().getPlayer() == e.getPlayer()){
                e.setQuitMessage("");
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e){
        for (NPC npc : CustomNPCs.getInstance().npcs.values()) {
            if(e.getRespawnLocation().distance(npc.getCurrentLocation()) <= 48){
                npc.injectPlayer(e.getPlayer());
            }
        }
    }

    private double getDistance(Location loc1, Location loc2) {
        return loc2.distance(loc1);
    }
}
