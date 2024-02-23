package dev.foxikle.customnpcs.internal.listeners;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.actions.conditions.Conditional;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.LookAtAnchor;
import dev.foxikle.customnpcs.internal.Utils;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.menu.MenuCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * The class that deals with misc listeners
 */
public class Listeners implements Listener {

    /**
     * Player Movement Data that keeps track of old movements to replace PlayerMoveEvent
     * @since 1.6.0
     */
    private static final ConcurrentMap<UUID, MovementData> playerMovementData = new ConcurrentHashMap<>();

    // Helper Constants
    // since 1.6.0
    private static final int FIVE_BLOCKS = 25;
    private static final int FIFTY_BLOCKS = 2500; // 50 * 50
    private static final int FOURTY_BLOCKS = 2304; // 48 * 48
    private static final double HALF_BLOCK = 0.25;

    // Writing Constants
    // since 1.6.0
    private static final BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    private static final String SHOULD_UPDATE_MESSAGE =
            Utils.style("&2&m----------------&r &6[&e!&6] &b&lCustomNPCs &6[&e!&6]  &2&m----------------\n&r&eA new update is available! I'd appreciate if you updated :) \n -&e&oFoxikle");

    private static final ConsoleCommandSender CONSOLE_SENDER = Bukkit.getConsoleSender();

    private static final Pattern PATTERN = Pattern.compile(" ");

    /**
     * The instance of the main Class
     */
    private final CustomNPCs plugin;

    // Executors for better handling of async scheduling than that bukkit scheduler
    private ScheduledExecutorService service;

    /**
     * Constructor for generic listners class
     *
     * @param plugin The instance of the main class
     */
    public Listeners(CustomNPCs plugin) {
        this.plugin = plugin;
    }

    public void start() {
        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> Bukkit.getOnlinePlayers().forEach(this::actionPlayerMovement), 1000, 220, TimeUnit.MILLISECONDS);
    }


    public void stop() {
        service.shutdown();
        CompletableFuture.runAsync(() -> {
            try {
                if (!service.awaitTermination(2, TimeUnit.SECONDS)) {
                    service.shutdownNow();
                }
            } catch (InterruptedException e) {
                service.shutdownNow();
                Thread.currentThread().interrupt();
            }
            plugin.getLogger().info("ScheduledExecutorService successfully shut down!");
            canStart = true;
        });
    }

    private void actionPlayerMovement(Player player) {
        final Location location = player.getLocation();
        final World world = player.getWorld();

        final UUID uuid = player.getUniqueId();
        for (InternalNpc npc : plugin.npcs.values()) {
            if (npc.getTarget() != null) continue;

            World npcWorld = npc.getWorld();
            if (world != npcWorld) continue;
            if (npc.getSettings().isTunnelvision()) continue;
            processPlayerMovement(player, npc, world, npcWorld, location, uuid);
        }
    }

    private void processPlayerMovement(final Player player, final InternalNpc npc, final World world, final World npcWorld, final Location location, final UUID uuid) {
        final Location npcLocation = npc.getCurrentLocation();
        MovementData oldMovementData; // difference in order of initialization in if/else statement
        MovementData movementData = playerMovementData.get(uuid);
        final double distanceSquared = location.distanceSquared(npcLocation);
        if (movementData == null) {
            playerMovementData.put(uuid, new MovementData(uuid, location, distanceSquared));
            movementData = playerMovementData.get(uuid);
            oldMovementData = movementData;
        } else {
            oldMovementData = movementData;
            movementData.setLastLocation(location);
            movementData.setDistanceSquared(distanceSquared);
        }
        trackFromTo(player, npc, movementData, oldMovementData);
        if (distanceSquared > FIVE_BLOCKS) {
            SCHEDULER.runTask(plugin, () -> {
                Collection<Entity> entities = npcWorld.getNearbyEntities(npc.getCurrentLocation(), 2.5, 2.5, 2.5);
                entities.removeIf(entity -> entity.getScoreboardTags().contains("NPC"));
                for (Entity en : entities) {
                    if (!(en instanceof Player p)) continue;
                    npc.lookAt(LookAtAnchor.HEAD, p);
                    return;
                }
                npc.setYRotation((float) npc.getSettings().getDirection());
            });
        }
    }

    private void trackFromTo(Player player, InternalNpc npc, MovementData data, MovementData oldData) {
        if (data.distanceSquared <= FIVE_BLOCKS) {
            npc.lookAt(LookAtAnchor.HEAD, player);
        } else if (oldData.distanceSquared >= FOURTY_BLOCKS && data.distanceSquared <= FIFTY_BLOCKS) {
            npc.injectPlayer(player);
        }
    }

    /**
     * <p>The npc interaction handler
     * </p>
     *
     * @param e The event callback
     * @since 1.0
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();

        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getRightClicked().getType() != EntityType.PLAYER) return;
        Player rightClicked = (Player) e.getRightClicked();

        if (plugin.getNPCByID(rightClicked.getUniqueId()) == null) return;

        InternalNpc npc;
        UUID uuid = rightClicked.getUniqueId();

        try {
            npc = plugin.getNPCByID(uuid);
        } catch (IllegalArgumentException ignored) {
            return;
        }

        if (player.hasPermission("customnpcs.edit") && player.isSneaking()) {
            player.performCommand("npc edit " + uuid);
        } else if (npc.getSettings().isInteractable()) {
            npc.getActions().forEach(action -> Bukkit.dispatchCommand(CONSOLE_SENDER, action.getCommand(player)));
        }
    }

    /**
     * The handler for text input
     * @param e The event callback
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        String message = e.getMessage();
        MenuCore core = plugin.menuCores.get(player);
        if (plugin.commandWaiting.contains(player)) {
            plugin.commandWaiting.remove(player);
            Action action = plugin.editingActions.get(player);
            List<String> currentArgs = action.getArgs();
            currentArgs.clear();
            currentArgs.addAll(List.of(PATTERN.split(message)));
            player.sendMessage(ChatColor.GREEN + "Successfully set command to be '" + ChatColor.RESET + Utils.style(message) + ChatColor.RESET + "" + ChatColor.GREEN + "'");
            SCHEDULER.runTask(plugin, () -> core.getActionCustomizerMenu(action).open(player));
        } else if (plugin.nameWaiting.contains(player)) {
            plugin.nameWaiting.remove(player);
            core.getNpc().getSettings().setName(message);
            player.sendMessage(Component.text("Successfully set name to be '", NamedTextColor.GREEN).append(plugin.getMiniMessage().deserialize(message)).append(Component.text("'", NamedTextColor.GREEN)));
            SCHEDULER.runTask(plugin, () -> core.getMainMenu().open(player));
        } else if (plugin.targetWaiting.contains(player)) {

            Conditional conditional = plugin.editingConditionals.get(player);
            if (conditional.getType() == Conditional.Type.NUMERIC) {
                try {
                    Double.parseDouble(message);
                } catch (NumberFormatException ignored) {
                    player.sendMessage(Utils.style("&cCannot parse the number '&f" + message + "&c'. Please try again."));
                    return;
                }
            }
            plugin.targetWaiting.remove(player);
            conditional.setTargetValue(message);
            player.sendMessage(Utils.style("&aSuccessfully set target to be '&r" + message + "&a'"));
            SCHEDULER.runTask(plugin, () -> core.getConditionalCustomizerMenu(plugin.editingConditionals.get(player)).open(player));
        } else if (plugin.titleWaiting.contains(player)) {
            plugin.titleWaiting.remove(player);
            List<String> args = plugin.editingActions.get(player).getArgsCopy();
            Action action = plugin.editingActions.get(player);
            List<String> currentArgs = action.getArgs();
            currentArgs.clear();
            currentArgs.add(0, args.get(0));
            currentArgs.add(1, args.get(1));
            currentArgs.add(2, args.get(2));
            currentArgs.addAll(List.of(PATTERN.split(message)));
            player.sendMessage(Component.text("Successfully set title to be '", NamedTextColor.GREEN).append(plugin.getMiniMessage().deserialize(message)).append(Component.text("'", NamedTextColor.GREEN)));
            SCHEDULER.runTask(plugin, () -> core.getActionCustomizerMenu(action).open(player));
        } else if (plugin.messageWaiting.contains(player)) {
            plugin.messageWaiting.remove(player);
            Action action = plugin.editingActions.get(player);
            List<String> currentArgs = action.getArgs();
            currentArgs.clear();
            currentArgs.addAll(List.of(PATTERN.split(message)));
            player.sendMessage(Component.text("Successfully set message to be '", NamedTextColor.GREEN).append(plugin.getMiniMessage().deserialize(message)).append(Component.text("'", NamedTextColor.GREEN)));
            SCHEDULER.runTask(plugin, () -> core.getActionCustomizerMenu(action).open(player));
        } else if (plugin.serverWaiting.contains(player)) {
            plugin.serverWaiting.remove(player);
            Action action = plugin.editingActions.get(player);
            List<String> currentArgs = action.getArgs();
            currentArgs.clear();
            currentArgs.addAll(List.of(PATTERN.split(message)));
            player.sendMessage(ChatColor.GREEN + "Successfully set server to be '" + ChatColor.RESET + message + ChatColor.RESET + "" + ChatColor.GREEN + "'");
            SCHEDULER.runTask(plugin, () -> core.getActionCustomizerMenu(action).open(player));
        } else if (plugin.actionbarWaiting.contains(player)) {
            plugin.actionbarWaiting.remove(player);
            Action action = plugin.editingActions.get(player);
            List<String> currentArgs = action.getArgs();
            currentArgs.clear();
            currentArgs.addAll(List.of(PATTERN.split(message)));
            player.sendMessage(Component.text("Successfully set actionbar to be '", NamedTextColor.GREEN).append(plugin.getMiniMessage().deserialize(message)).append(Component.text("'", NamedTextColor.GREEN)));
            SCHEDULER.runTask(plugin, () -> core.getActionCustomizerMenu(action).open(player));
        } else if (plugin.playernameWating.contains(player)) {
            if(message.equalsIgnoreCase("quit") ||
                    message.equalsIgnoreCase("exit")||
                    message.equalsIgnoreCase("stop")||
                    message.equalsIgnoreCase("cancel")) {
                plugin.urlWaiting.remove(player);
                SCHEDULER.runTask(plugin, () -> core.getSkinMenu().open(player));
                e.setCancelled(true);
                return;
            }
            // this runs on an async thread, so there isn't any need to do this async :)
            player.sendMessage("§e§oAttempting to fetch " + message + "'s skin from Mojang's API. This may take a moment!");
            String name = e.getMessage();
            try {
                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
                InputStreamReader reader = new InputStreamReader(url.openStream());
                String uuid = new JsonParser().parse(reader).getAsJsonObject().get("id").getAsString();
                reader.close();

                URL url2 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
                reader = new InputStreamReader(url2.openStream());

                JsonObject property = new JsonParser().parse(reader).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
                String value = property.get("value").getAsString();
                String signature = property.get("signature").getAsString();
                core.getNpc().getSettings().setSkinData(signature, value, name + "'s skin (imported via player name)");
            } catch (Exception ignored) {
                player.sendMessage(ChatColor.RED + "There was an error parsing " + name + "'s skin? Does this player exist?");
                e.setCancelled(true);
                return;
            }
            plugin.playernameWating.remove(player);
            player.sendMessage(ChatColor.GREEN + "Successfully set NPC's skin to " + name + "'s skin!");
            SCHEDULER.runTask(plugin, () -> core.getSkinMenu().open(player));
        } else if (plugin.urlWaiting.contains(player)) {
            if(message.equalsIgnoreCase("quit") ||
                    message.equalsIgnoreCase("exit")||
                    message.equalsIgnoreCase("stop")||
                    message.equalsIgnoreCase("cancel")) {
                plugin.urlWaiting.remove(player);
                SCHEDULER.runTask(plugin, () -> core.getSkinMenu().open(player));
                e.setCancelled(true);
                return;
            }
            e.setCancelled(true);
            player.sendMessage("§e§oAttempting to fetch the skin from a URL. This may take a moment!");
            try {
                URL url = new URL(message);
                plugin.MINESKIN_CLIENT.generateUrl(url.toString()).whenComplete((skin, throwable) -> {
                    if(throwable != null) {
                        player.sendMessage(ChatColor.RED + "An error occured whilst parsing this skin.");
                        return;
                    }
                    core.getNpc().getSettings().setSkinData(skin.data.texture.signature, skin.data.texture.value, "A skin imported via a URL");
                    plugin.urlWaiting.remove(player);
                    player.sendMessage(ChatColor.GREEN + "Successfully set NPC's skin from " + message);
                    SCHEDULER.runTask(plugin, () -> core.getSkinMenu().open(player));
                });
            } catch (Exception ex) {
                player.sendMessage(ChatColor.RED + "An error occured whilst parsing NPC skin. Is this URL valid?");
            }
        } else return;

        e.setCancelled(true);
    }

    /**
     * <p>The npc injection handler on join
     * </p>
     *
     * @param e The event callback
     * @since 1.3-pre5
     */
    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (plugin.update && plugin.getConfig().getBoolean("AlertOnUpdate") && player.hasPermission("customnpcs.alert")) {
            player.sendMessage(SHOULD_UPDATE_MESSAGE);
        }
        List<InternalNpc> npcs = plugin.getNPCs();
        for (InternalNpc npc : npcs) npc.injectPlayer(player);
        SCHEDULER.runTaskLater(plugin, () -> npcs.forEach(npc -> npc.injectPlayer(player)), 10);
    }

    /**
     * <p>The npc interaction handler while mounted on an entity
     * </p>
     * @param e The event callback
     * @since 1.3-pre4
     */
     
     /*
    @EventHandler
    public void onEntityMove(EntityMoveEvent e) {
        final Entity et = e.getEntity();
        List<Player> players = new ArrayList<>();
		et.getPassengers().forEach(entity1 -> {
			if(entity1 instanceof Player player) players.add(player);
		});
        executorService.submit(() -> players.forEach(this::actionPlayerMovement));
    }
    */

    /**
     * <p>The npc injection handler on velocity
     * </p>
     *
     * @param e The event callback
     * @since 1.3-pre4
     */
    @EventHandler
    public void onVelocity(PlayerVelocityEvent e) {
        actionPlayerMovement(e.getPlayer());
    }

    /**
     * <p>The npc follow handler
     * TODO: Replace with proper Pathfinding.
     * </p>
     *
     * @param e The event callback
     * @since 1.3-pre2
     */
    @EventHandler
    public void followHandler(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        World world = player.getWorld();
        Location location = player.getLocation();
        Location to = e.getTo();
        for (InternalNpc npc : plugin.npcs.values()) {
            if (world != npc.getWorld()) continue; //TODO: Make npc travel between dimensions
            if (npc.getTarget() != player) continue;
            npc.lookAt(LookAtAnchor.HEAD, player);
            if (npc.getCurrentLocation().distanceSquared(to) >= HALF_BLOCK) {
                SCHEDULER.runTaskLater(plugin, () -> {
                    if (to.distanceSquared(location) >= 1) npc.moveTo(to);
                }, 30);
            }
        }
    }

    /**
     * <p>The npc injection handler
     * </p>
     *
     * @param e The event callback
     * @since 1.0
     */
    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        Location location = player.getLocation();
        World world = player.getWorld();
        for (InternalNpc npc : plugin.npcs.values()) {
            Location spawnLocation = npc.getSpawnLoc();
            if (world != npc.getWorld()) return;

            double distanceSquared = location.distanceSquared(spawnLocation);
            if (distanceSquared <= FIVE_BLOCKS) {
                npc.lookAt(LookAtAnchor.HEAD, player);
            } else if (distanceSquared >= FOURTY_BLOCKS && distanceSquared <= FIFTY_BLOCKS) {
                npc.injectPlayer(player);
            }
        }
    }


    /**
     * Logic for injecting NPCs on world changes
     *
     * @param e Event callback
     */
    @EventHandler
    public void onDimentionChange(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        Location location = player.getLocation();
        World world = player.getWorld();
        for (InternalNpc npc : plugin.npcs.values()) {
            if (world != npc.getWorld()) continue;
            if (location.distanceSquared(npc.getCurrentLocation()) <= FOURTY_BLOCKS) npc.injectPlayer(player);
        }
    }

    /**
     * <p>The npc leave message handler. Cancels the leave message.
     * </p>
     *
     * @param e The event callback
     * @since 1.0
     */
    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        for (InternalNpc npc : plugin.npcs.values()) {
            if (npc.getUniqueID() != player.getUniqueId()) continue;
            e.quitMessage(Component.empty());
        }
        plugin.commandWaiting.remove(player);
        plugin.nameWaiting.remove(player);
        plugin.targetWaiting.remove(player);
        plugin.titleWaiting.remove(player);
        plugin.messageWaiting.remove(player);
        plugin.serverWaiting.remove(player);
        plugin.actionbarWaiting.remove(player);
        plugin.urlWaiting.remove(player);
        plugin.playernameWating.remove(player);
    }

    /**
     * <p>The npc interaction handler
     * </p>
     *
     * @param e The event callback
     * @since 1.2
     */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        Location respawnLocation = e.getRespawnLocation();
        for (InternalNpc npc : plugin.npcs.values()) {
            if (!(respawnLocation.distanceSquared(npc.getCurrentLocation()) <= FOURTY_BLOCKS)) continue;
            npc.injectPlayer(player);
        }
    }

    private static class MovementData {
        private final UUID uniqueId;
        private Location lastLocation;
        private double distanceSquared;

        MovementData(UUID uniqueId, Location lastLocation, double distanceSquared) {
            this.uniqueId = uniqueId;
            this.lastLocation = lastLocation;
            this.distanceSquared = distanceSquared;
        }

        public UUID getUniqueId() {
            return uniqueId;
        }

        public Location getLastLocation() {
            return lastLocation;
        }

        public void setLastLocation(Location location) {
            this.lastLocation = location;
        }

        public double getDistanceSquared() {
            return distanceSquared;
        }

        public void setDistanceSquared(double distanceSquared) {
            this.distanceSquared = distanceSquared;
        }

        public MovementData copy() {
            return new MovementData(uniqueId, lastLocation, distanceSquared);
        }
    }
}
