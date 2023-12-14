package dev.foxikle.customnpcs.internal.listeners;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.actions.conditions.Conditional;
import dev.foxikle.customnpcs.internal.LookAtAnchor;
import dev.foxikle.customnpcs.internal.interfaces.InternalNPC;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

/**
 * The class that deals with misc listeners
 */
public class Listeners implements Listener {
    /**
     * The instance of the main Class
     */
    private final CustomNPCs plugin;

    /**
     * Constructor for generic listners class
     * @param plugin The instance of the main class
     */
    public Listeners(CustomNPCs plugin) {
        this.plugin = plugin;
    }

    /**
     * <p>The npc interaction handler
     * </p>
     * @param e The event callback
     * @since 1.0
     */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
        if (e.getHand() == EquipmentSlot.HAND) {
            if (e.getRightClicked() instanceof Player p) {
                Player player = e.getPlayer();
                if(plugin.getNPCByID(p.getUniqueId()) != null) {
                    InternalNPC npc;
                    try {
                        npc = plugin.getNPCByID(p.getUniqueId());
                    } catch (IllegalArgumentException ignored){
                        return;
                    }
                    if (player.hasPermission("customnpcs.edit") && player.isSneaking()) {
                        player.performCommand("npc edit " + npc.getUniqueID());
                    } else if (npc.getSettings().isInteractable()) {
                        npc.getActions().forEach(action -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.getCommand(e.getPlayer())));
                    }
                }
            }
        }
    }

    /**
     * <p>The handler for text input
     * </p>
     * @param e The event callback
     * @since 1.0
     */
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
            plugin.menuCores.get(e.getPlayer()).getNpc().getSettings().setName(e.getMessage());
            e.getPlayer().sendMessage(Component.text("Successfully set name to be '", NamedTextColor.GREEN).append(plugin.getMiniMessage().deserialize(e.getMessage())).append(Component.text("'", NamedTextColor.GREEN)));
            Bukkit.getScheduler().runTask(plugin, () -> e.getPlayer().openInventory(plugin.menuCores.get(e.getPlayer()).getMainMenu()));
            e.setCancelled(true);
        } else if (plugin.targetWaiting.contains(e.getPlayer())) {

            Conditional conditional = plugin.editingConditionals.get(e.getPlayer());
            if(conditional.getType() == Conditional.Type.NUMERIC) {
                try {
                    Double.parseDouble(e.getMessage());
                } catch (NumberFormatException ignored) {
                    e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cCannot parse the number '&f" + e.getMessage() + "&c'. Please try again."));
                    return;
                }
            }
            plugin.targetWaiting.remove(e.getPlayer());
            conditional.setTargetValue(e.getMessage());
            e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully set target to be '" + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', e.getMessage()) + ChatColor.RESET + ChatColor.GREEN + "'");
            Bukkit.getScheduler().runTask(plugin, () -> e.getPlayer().openInventory(plugin.menuCores.get(e.getPlayer()).getConditionalCustomizerMenu(plugin.editingConditionals.get(e.getPlayer()))));
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
                e.getPlayer().sendMessage(Component.text("Successfully set title to be '", NamedTextColor.GREEN).append(plugin.getMiniMessage().deserialize(e.getMessage())).append(Component.text("'", NamedTextColor.GREEN)));
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
                e.getPlayer().sendMessage(Component.text("Successfully set message to be '", NamedTextColor.GREEN).append(plugin.getMiniMessage().deserialize(e.getMessage())).append(Component.text("'", NamedTextColor.GREEN)));
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
                e.getPlayer().sendMessage(Component.text("Successfully set actionbar to be '", NamedTextColor.GREEN).append(plugin.getMiniMessage().deserialize(e.getMessage())).append(Component.text("'", NamedTextColor.GREEN)));
                Bukkit.getScheduler().runTask(plugin, () -> e.getPlayer().openInventory(plugin.menuCores.get(e.getPlayer()).getActionCustomizerMenu(action)));
            });
            e.setCancelled(true);
        }
    }

    /**
     * <p>The npc injection handler on join
     * </p>
     * @param e The event callback
     * @since 1.3-pre5
     */
    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent e) {
        if(plugin.update && plugin.getConfig().getBoolean("AlertOnUpdate")) {
            if(e.getPlayer().hasPermission("customnpcs.alert")) {
                e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&m----------------&r &6[&e!&6] &b&lCustomNPCs &6[&e!&6]  &2&m----------------\n&r&eA new update (" + plugin.getUpdater().getNewestVersion() + ") is available! I'd appreciate if you updated :) \n -&e&oFoxikle"));
            }
        }
        for (InternalNPC npc : plugin.getNPCs()) {
            npc.injectPlayer(e.getPlayer());
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (InternalNPC npc : plugin.getNPCs()) {
                npc.injectPlayer(e.getPlayer());
            }
        }, 10);
    }

    /**
     * <p>The npc look handler
     * </p>
     * @param e The event callback
     * @since 1.0
     */
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        for (InternalNPC npc : plugin.npcs.values()) {
            if(npc.getTarget() != null) return;
            if(player.getWorld() != npc.getWorld()) return;
            if(npc.getSettings().isTunnelvision()) return;
            if (e.getPlayer().getLocation().distance(npc.getCurrentLocation()) <= 5) {
                npc.lookAt(LookAtAnchor.HEAD, player);
            } else if (e.getFrom().distance(npc.getCurrentLocation()) >= 48 && e.getTo().distance(npc.getCurrentLocation()) <= 48) {
                npc.injectPlayer(player);
            }
            if (e.getPlayer().getLocation().distance(npc.getCurrentLocation()) > 5) {
                Collection<Entity> entities = npc.getWorld().getNearbyEntities(npc.getCurrentLocation(), 2.5, 2.5, 2.5);
                entities.removeIf(entity -> entity.getScoreboardTags().contains("NPC"));
                for (Entity en : entities) {
                    if (en.getType() == EntityType.PLAYER) {
                        npc.lookAt(LookAtAnchor.HEAD, player);
                        return;
                    }
                }
                npc.setYRotation((float) npc.getSettings().getDirection());
            }
        }
    }

    /**
     * <p>The npc interaction handler while mounted on an entity
     * </p>
     * @param e The event callback
     * @since 1.3-pre4
     */
    @EventHandler
    public void onEntityMove(EntityMoveEvent e) {
        Entity et = e.getEntity();
        List<Player> players = new ArrayList<>();
        et.getPassengers().forEach(entity1 -> {
            if(entity1 instanceof Player player) players.add(player);
        });
        for (Player player : players) {
            for (InternalNPC npc : plugin.npcs.values()) {
                if(player.getWorld() != npc.getWorld()) return;
                if(npc.getTarget() != null) return;
                if (player.getLocation().distance(npc.getCurrentLocation()) <= 5) {
                    npc.lookAt(LookAtAnchor.HEAD, player);
                } else if (e.getFrom().distance(npc.getCurrentLocation()) >= 48 && e.getTo().distance(npc.getCurrentLocation()) <= 48) {
                    npc.injectPlayer(player);
                }
                if (player.getLocation().distance(npc.getCurrentLocation()) > 5) {
                    Collection<Entity> entities = npc.getWorld().getNearbyEntities(npc.getCurrentLocation(), 2.5, 2.5, 2.5);
                    entities.removeIf(entity -> entity.getScoreboardTags().contains("NPC"));
                    for (Entity en : entities) {
                        if (en.getType() == EntityType.PLAYER) {
                            npc.lookAt(LookAtAnchor.HEAD, player);
                            return;
                        }
                    }
                    npc.setYRotation((float) npc.getSettings().getDirection());
                }
            }
        }

    }

    /**
     * <p>The npc injection handler on velocity
     * </p>
     * @param e The event callback
     * @since 1.3-pre4
     */
    @EventHandler
    public void onVelocity(PlayerVelocityEvent e) {
        Player player = e.getPlayer();
        for (InternalNPC npc : plugin.npcs.values()) {
            if(player.getWorld() != npc.getWorld()) return;
            if(npc.getTarget() != null) return;
            if (e.getPlayer().getLocation().distance(npc.getCurrentLocation()) <= 5) {
                npc.lookAt(LookAtAnchor.HEAD, player);
            } else if (player.getLocation().distance(npc.getCurrentLocation()) >= 48) {
                npc.injectPlayer(player);
            }
            if (e.getPlayer().getLocation().distance(npc.getCurrentLocation()) > 5) {
                Collection<Entity> entities = npc.getWorld().getNearbyEntities(npc.getCurrentLocation(), 2.5, 2.5, 2.5);
                entities.removeIf(entity -> entity.getScoreboardTags().contains("NPC"));
                for (Entity en : entities) {
                    if (en.getType() == EntityType.PLAYER) {
                        npc.lookAt(LookAtAnchor.HEAD, player);
                        return;
                    }
                }
                npc.setYRotation((float) npc.getSettings().getDirection());
            }
        }
    }

    /**
     * <p>The npc follow handler
     * </p>
     * @param e The event callback
     * @since 1.3-pre2
     */
    @EventHandler
    public void followHandler(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        for (InternalNPC npc : plugin.npcs.values()) {
            if(player.getWorld() != npc.getWorld()) return; //TODO: Make npc travel between dimensions
            if(npc.getTarget() == player){
                npc.lookAt(LookAtAnchor.HEAD, player);
                if(npc.getCurrentLocation().distance(e.getTo()) >= .5){
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if(e.getTo().distance(player.getLocation()) >= 1)
                            npc.moveTo(e.getTo());
                    }, 30);
                }
            }
        }
    }

    /**
     * <p>The npc injection handler
     * </p>
     * @param e The event callback
     * @since 1.0
     */
    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        for (InternalNPC npc : plugin.npcs.values()) {
            if(player.getWorld() != npc.getWorld()) return;
            if (player.getLocation().distance(npc.getSpawnLoc()) <= 5) {
                npc.lookAt(LookAtAnchor.HEAD, player);
            } else if (player.getLocation().distance(npc.getSpawnLoc()) >= 48 && player.getLocation().distance(npc.getSpawnLoc()) <= 48) {
                npc.injectPlayer(player);
            }
        }
    }


    /**
     * Logic for injecting NPCs on world changes
     * @param e Event callback
     */
    @EventHandler
    public void onDimentionChange(PlayerChangedWorldEvent e) {
        for (InternalNPC npc : plugin.npcs.values()) {
            if(e.getPlayer().getWorld() == npc.getWorld()) {
                if(e.getPlayer().getLocation().distance(npc.getCurrentLocation()) <= 48){
                    npc.injectPlayer(e.getPlayer());
                }
            }
        }
    }

    /**
     * <p>The npc leave message handler. Cancles the leave message.
     * </p>
     * @param e The event callback
     * @since 1.0
     */
    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        if(plugin.getNPCByID(e.getPlayer().getUniqueId()) != null)
            e.quitMessage(Component.empty());
    }

    /**
     * <p>The npc interaction handler
     * </p>
     * @param e The event callback
     * @since 1.2
     */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e){
        for (InternalNPC npc : plugin.npcs.values()) {
            if(e.getRespawnLocation().distance(npc.getCurrentLocation()) <= 48){
                npc.injectPlayer(e.getPlayer());
            }
        }
    }
}
