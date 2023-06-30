package dev.foxikle.customnpcs.listeners;

import dev.foxikle.customnpcs.Action;
import dev.foxikle.customnpcs.CustomNPCs;
import dev.foxikle.customnpcs.NPC;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Listeners implements Listener {
    
    private final CustomNPCs plugin;

    public Listeners(CustomNPCs plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
        if (e.getHand() == EquipmentSlot.HAND) {
            if (e.getRightClicked().getType() == EntityType.PLAYER) {
                Player player = e.getPlayer();
                Player rightClicked = (Player) e.getRightClicked();
                ServerPlayer sp = ((CraftPlayer) rightClicked).getHandle();
                NPC npc;
                try {
                    npc = plugin.getNPCByID(sp.getUUID());
                } catch (IllegalArgumentException ignored){
                    return;
                }
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
        if (plugin.commandWaiting.contains(e.getPlayer())) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.commandWaiting.remove(e.getPlayer());
                Action action = plugin.editingActions.get(e.getPlayer());
                List<String> currentArgs = action.getArgs();
                currentArgs.clear();
                currentArgs.addAll(List.of(e.getMessage().split(" ")));
                e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully set command to be '" + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', e.getMessage()) + ChatColor.RESET + "" + ChatColor.GREEN + "'");
                Bukkit.getScheduler().runTask(plugin, () -> e.getPlayer().openInventory(plugin.menuCores.get(e.getPlayer()).getActionCustomizerMenu(action)));
            });
            e.setCancelled(true);
        } else if (plugin.nameWaiting.contains(e.getPlayer())) {
            plugin.nameWaiting.remove(e.getPlayer());
            plugin.menuCores.get(e.getPlayer()).getNpc().setName(e.getMessage());
            e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully set name to be '" + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', e.getMessage()) + ChatColor.RESET + ChatColor.GREEN + "'");
            Bukkit.getScheduler().runTask(plugin, () -> e.getPlayer().openInventory(plugin.menuCores.get(e.getPlayer()).getMainMenu()));
            e.setCancelled(true);
        } else if (plugin.titleWaiting.contains(e.getPlayer())) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.titleWaiting.remove(e.getPlayer());
                List<String> args = plugin.editingActions.get(e.getPlayer()).getArgsCopy();
                Action action = plugin.editingActions.get(e.getPlayer());
                List<String> currentArgs = action.getArgs();
                currentArgs.clear();
                currentArgs.add(0, args.get(0));
                currentArgs.add(1, args.get(1));
                currentArgs.add(2, args.get(2));
                currentArgs.addAll(List.of(e.getMessage().split(" ")));
                e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully set title to be '" + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', e.getMessage()) + ChatColor.RESET + "" + ChatColor.GREEN + "'");
                Bukkit.getScheduler().runTask(plugin, () -> e.getPlayer().openInventory(plugin.menuCores.get(e.getPlayer()).getActionCustomizerMenu(action)));
            });
            e.setCancelled(true);
        } else if (plugin.messageWaiting.contains(e.getPlayer())) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.messageWaiting.remove(e.getPlayer());
                Action action = plugin.editingActions.get(e.getPlayer());
                List<String> currentArgs = action.getArgs();
                currentArgs.clear();
                currentArgs.addAll(List.of(e.getMessage().split(" ")));
                e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully set message to be '" + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', e.getMessage()) + ChatColor.RESET + "" + ChatColor.GREEN + "'");
                Bukkit.getScheduler().runTask(plugin, () -> e.getPlayer().openInventory(plugin.menuCores.get(e.getPlayer()).getActionCustomizerMenu(action)));
            });
            e.setCancelled(true);
        } else if (plugin.serverWaiting.contains(e.getPlayer())) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.serverWaiting.remove(e.getPlayer());
                Action action = plugin.editingActions.get(e.getPlayer());
                List<String> currentArgs = action.getArgs();
                currentArgs.clear();
                currentArgs.addAll(List.of(e.getMessage().split(" ")));
                e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully set server to be '" + ChatColor.RESET +  e.getMessage() + ChatColor.RESET + "" + ChatColor.GREEN + "'");
                Bukkit.getScheduler().runTask(plugin, () -> e.getPlayer().openInventory(plugin.menuCores.get(e.getPlayer()).getActionCustomizerMenu(action)));
            });
            e.setCancelled(true);
        } else if (plugin.actionbarWaiting.contains(e.getPlayer())) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.actionbarWaiting.remove(e.getPlayer());
                Action action = plugin.editingActions.get(e.getPlayer());
                List<String> currentArgs = action.getArgs();
                currentArgs.clear();
                currentArgs.addAll(List.of(e.getMessage().split(" ")));
                e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully set actiobar to be '" + ChatColor.RESET +  ChatColor.translateAlternateColorCodes('&', e.getMessage()) + ChatColor.RESET + "" + ChatColor.GREEN + "'");
                Bukkit.getScheduler().runTask(plugin, () -> e.getPlayer().openInventory(plugin.menuCores.get(e.getPlayer()).getActionCustomizerMenu(action)));
            });
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent e) {
            for (NPC npc : plugin.getNPCs()) {
                npc.injectPlayer(e.getPlayer());
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (NPC npc : plugin.getNPCs()) {
                    npc.injectPlayer(e.getPlayer());
                }
        }, 10);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        for (NPC npc : plugin.npcs.values()) {
            if(npc.getTarget() != null) return;
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
    public void onEntityMove(EntityMoveEvent e) {
        Entity et = e.getEntity();
        List<Player> players = new ArrayList<>();
        et.getPassengers().forEach(entity1 -> {
            if(entity1 instanceof Player player) players.add(player);
        });
        for (Player player : players) {
            for (NPC npc : plugin.npcs.values()) {
                if(npc.getTarget() != null) return;
                if (getDistance(player.getLocation(), npc.getCurrentLocation()) <= 5) {
                    npc.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftPlayer) player).getHandle(), EntityAnchorArgument.Anchor.EYES);
                } else if (getDistance(e.getFrom(), npc.getCurrentLocation()) >= 48 && getDistance(e.getTo(), npc.getCurrentLocation()) <= 48) {
                    npc.injectPlayer(player);
                }
                if (getDistance(player.getLocation(), npc.getCurrentLocation()) > 5) {
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

    }

    @EventHandler
    public void onVelocity(PlayerVelocityEvent e) {
        Player player = e.getPlayer();
        for (NPC npc : plugin.npcs.values()) {
            if(npc.getTarget() != null) return;
            if (getDistance(e.getPlayer().getLocation(), npc.getCurrentLocation()) <= 5) {
                npc.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftPlayer) player).getHandle(), EntityAnchorArgument.Anchor.EYES);
            } else if (player.getLocation().distance(npc.getCurrentLocation()) >= 48) {
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
    public void followHandler(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        for (NPC npc : plugin.npcs.values()) {
            if(npc.getTarget() == player){
                npc.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftPlayer) player).getHandle(), EntityAnchorArgument.Anchor.EYES);
                if(npc.getCurrentLocation().distance(e.getTo()) >= .5){
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if(e.getTo().distance(player.getLocation()) >= 1)
                            npc.moveTo(new Vec3(e.getTo().x(), e.getTo().y(), e.getTo().z()));
                    }, 30);
                }
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        for (NPC npc : plugin.npcs.values()) {
            if (getDistance(e.getPlayer().getLocation(), npc.getSpawnLoc()) <= 5) {
                npc.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftPlayer) player).getHandle(), EntityAnchorArgument.Anchor.EYES);
            } else if (getDistance(e.getFrom(), npc.getSpawnLoc()) >= 48 && getDistance(e.getTo(), npc.getSpawnLoc()) <= 48) {
                npc.injectPlayer(player);
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        for (NPC npc : plugin.npcs.values()) {
            if(npc.getPlayer().getBukkitEntity().getPlayer() == e.getPlayer()){
                e.setQuitMessage("");
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e){
        for (NPC npc : plugin.npcs.values()) {
            if(e.getRespawnLocation().distance(npc.getCurrentLocation()) <= 48){
                npc.injectPlayer(e.getPlayer());
            }
        }
    }

    private double getDistance(Location loc1, Location loc2) {
        return loc2.distance(loc1);
    }
}
