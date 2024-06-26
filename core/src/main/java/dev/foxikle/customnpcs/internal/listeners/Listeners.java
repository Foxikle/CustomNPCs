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
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
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
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * The class that deals with misc listeners
 */
@SuppressWarnings("unused")
public class Listeners implements Listener {

    /**
     * Player Movement Data that keeps track of old movements to replace PlayerMoveEvent
     *
     * @since 1.6.0
     */
    private static final ConcurrentMap<UUID, MovementData> playerMovementData = new ConcurrentHashMap<>();

    // Helper Constants
    // since 1.6.0
    private static final int FIVE_BLOCKS = 25;
    private static final int FIFTY_BLOCKS = 2500; // 50 * 50
    private static final int SIXTY_BLOCKS = 3600; // 60 * 60
    private static final int FORTY_BLOCKS = 2304; // 48 * 48
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
     * Constructor for generic listeners class
     *
     * @param plugin The instance of the main class
     */
    public Listeners(CustomNPCs plugin) {
        this.plugin = plugin;
    }

    public void start() {
        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> Bukkit.getOnlinePlayers().forEach(this::actionPlayerMovement), 1000, plugin.getConfig().getInt("LookInterval") * 50L, TimeUnit.MILLISECONDS);
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
            //if (npc.getSettings().isTunnelvision()) continue;
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
        if(npc.getSettings().isTunnelvision()) return;
        if (distanceSquared > FIVE_BLOCKS) {
            SCHEDULER.runTask(plugin, () -> {
                Collection<Entity> entities = npcWorld.getNearbyEntities(npc.getCurrentLocation(), 2.5, 2.5, 2.5);
                entities.removeIf(entity -> entity.getScoreboardTags().contains("NPC"));
                for (Entity en : entities) {
                    if (!(en instanceof Player p)) continue;
                    if(!npc.getSettings().isTunnelvision()) {
                        npc.lookAt(LookAtAnchor.HEAD, p);
                        return;
                    }
                }
                npc.setYRotation((float) npc.getSettings().getDirection());
            });
        }
    }

    private void trackFromTo(Player player, InternalNpc npc, MovementData data, MovementData oldData) {
        if (data.distanceSquared <= FIVE_BLOCKS && !npc.getSettings().isTunnelvision()) {
            npc.lookAt(LookAtAnchor.HEAD, player);
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
            assert npc != null;
        } catch (IllegalArgumentException ignored) {
            return;
        }

        if (player.hasPermission("customnpcs.edit") && player.isSneaking()) {
            player.performCommand("npc edit " + uuid);
        } else {
            if (npc.getSettings().isInteractable()) {
                npc.getActions().forEach(action -> Bukkit.dispatchCommand(CONSOLE_SENDER, action.getCommand(player)));
            }
        }
    }

    /**
     * The handler for text input
     *
     * @param e The event callback
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("deprecation")
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        String message = e.getMessage();
        boolean cancel = message.equalsIgnoreCase("quit") || message.equalsIgnoreCase("exit") || message.equalsIgnoreCase("stop") || message.equalsIgnoreCase("cancel");
        MenuCore core = plugin.menuCores.get(player);
        if (plugin.commandWaiting.contains(player)) {
            Action action = plugin.editingActions.get(player);
            if (cancel) {
                plugin.commandWaiting.remove(player);
                SCHEDULER.runTask(plugin, () -> core.getActionCustomizerMenu(action).open(player));
                e.setCancelled(true);
                return;
            }
            plugin.commandWaiting.remove(player);
            List<String> currentArgs = action.getArgs();
            currentArgs.clear();
            currentArgs.addAll(Utils.list(PATTERN.split(message)));
            player.sendMessage(Utils.style("&aSuccessfully set command to be '&r" + Utils.style(message) + "&r&a'"));
            SCHEDULER.runTask(plugin, () -> core.getActionCustomizerMenu(action).open(player));
        } else if (plugin.nameWaiting.contains(player)) {
            if (cancel) {
                plugin.nameWaiting.remove(player);
                SCHEDULER.runTask(plugin, () -> core.getMainMenu().open(player));
                e.setCancelled(true);
                return;
            }
            plugin.nameWaiting.remove(player);
            core.getNpc().getSettings().setName(message);
            player.sendMessage(Component.text("Successfully set name to be '", NamedTextColor.GREEN).append(plugin.getMiniMessage().deserialize(message)).append(Component.text("'", NamedTextColor.GREEN)));
            SCHEDULER.runTask(plugin, () -> core.getMainMenu().open(player));
        } else if (plugin.targetWaiting.contains(player)) {
            Conditional conditional = plugin.editingConditionals.get(player);
            if (cancel) {
                plugin.targetWaiting.remove(player);
                SCHEDULER.runTask(plugin, () -> core.getConditionalCustomizerMenu(conditional).open(player));
                e.setCancelled(true);
                return;
            }
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
            SCHEDULER.runTask(plugin, () -> core.getConditionalCustomizerMenu(conditional).open(player));
        } else if (plugin.titleWaiting.contains(player)) {
            Action action = plugin.editingActions.get(player);
            if (cancel) {
                plugin.titleWaiting.remove(player);
                SCHEDULER.runTask(plugin, () -> core.getActionCustomizerMenu(action).open(player));
                e.setCancelled(true);
                return;
            }
            plugin.titleWaiting.remove(player);
            List<String> args = plugin.editingActions.get(player).getArgsCopy();
            List<String> currentArgs = action.getArgs();
            currentArgs.clear();
            currentArgs.add(0, args.get(0));
            currentArgs.add(1, args.get(1));
            currentArgs.add(2, args.get(2));
            currentArgs.addAll(List.of(PATTERN.split(message)));
            player.sendMessage(Component.text("Successfully set title to be '", NamedTextColor.GREEN).append(plugin.getMiniMessage().deserialize(message)).append(Component.text("'", NamedTextColor.GREEN)));
            SCHEDULER.runTask(plugin, () -> core.getActionCustomizerMenu(action).open(player));
        } else if (plugin.messageWaiting.contains(player)) {
            Action action = plugin.editingActions.get(player);
            if (cancel) {
                plugin.messageWaiting.remove(player);
                SCHEDULER.runTask(plugin, () -> core.getActionCustomizerMenu(action).open(player));
                e.setCancelled(true);
                return;
            }
            plugin.messageWaiting.remove(player);
            List<String> currentArgs = action.getArgs();
            currentArgs.clear();
            currentArgs.addAll(List.of(PATTERN.split(message)));
            player.sendMessage(Component.text("Successfully set message to be '", NamedTextColor.GREEN).append(plugin.getMiniMessage().deserialize(message)).append(Component.text("'", NamedTextColor.GREEN)));
            SCHEDULER.runTask(plugin, () -> core.getActionCustomizerMenu(action).open(player));
        } else if (plugin.serverWaiting.contains(player)) {
            Action action = plugin.editingActions.get(player);
            if (cancel) {
                plugin.serverWaiting.remove(player);
                SCHEDULER.runTask(plugin, () -> core.getActionCustomizerMenu(action).open(player));
                e.setCancelled(true);
                return;
            }
            plugin.serverWaiting.remove(player);
            List<String> currentArgs = action.getArgs();
            currentArgs.clear();
            currentArgs.addAll(List.of(PATTERN.split(message)));
            player.sendMessage(String.format(Utils.style("&aSuccessfully set server to be '&r%s&r&a'"), message));
            SCHEDULER.runTask(plugin, () -> core.getActionCustomizerMenu(action).open(player));
        } else if (plugin.actionbarWaiting.contains(player)) {
            Action action = plugin.editingActions.get(player);
            if (cancel) {
                plugin.actionbarWaiting.remove(player);
                SCHEDULER.runTask(plugin, () -> core.getActionCustomizerMenu(action).open(player));
                e.setCancelled(true);
                return;
            }
            plugin.actionbarWaiting.remove(player);
            List<String> currentArgs = action.getArgs();
            currentArgs.clear();
            currentArgs.addAll(List.of(PATTERN.split(message)));
            player.sendMessage(Component.text("Successfully set actionbar to be '", NamedTextColor.GREEN).append(plugin.getMiniMessage().deserialize(message)).append(Component.text("'", NamedTextColor.GREEN)));
            SCHEDULER.runTask(plugin, () -> core.getActionCustomizerMenu(action).open(player));
        } else if (plugin.playerWaiting.contains(player)) {
            if (cancel) {
                plugin.playerWaiting.remove(player);
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
                player.sendMessage(Utils.style("&cThere was an error parsing " + name + "'s skin? Does this player exist?"));
                e.setCancelled(true);
                return;
            }
            plugin.playerWaiting.remove(player);
            player.sendMessage(Utils.style("&aSuccessfully set NPC's skin to " + name + "'s skin!"));
            SCHEDULER.runTask(plugin, () -> core.getSkinMenu().open(player));
        } else if (plugin.urlWaiting.contains(player)) {
            if (cancel) {
                plugin.urlWaiting.remove(player);
                SCHEDULER.runTask(plugin, () -> core.getSkinMenu().open(player));
                e.setCancelled(true);
                return;
            }
            e.setCancelled(true);
            player.sendMessage(Utils.style("&e&oAttempting to fetch the skin from a URL. This may take a moment!"));
            try {
                URL url = new URL(message);
                plugin.MINESKIN_CLIENT.generateUrl(url.toString()).whenComplete((skin, throwable) -> {
                    if (throwable != null) {
                        if(throwable.getMessage().equalsIgnoreCase("java.lang.RuntimeException: org.mineskin.data.MineskinException: Failed to find image from url")) {
                            player.sendMessage(Utils.style("&cThe provided URL was &ovalid&r&c, but it doesn't contain any skin data. Sorry!"));
                            return;
                        }
                        player.sendMessage(Utils.style("&cAn error occurred whilst parsing this skin. Check the console for details."));
                        plugin.getLogger().log(Level.SEVERE, "An error occurred whilst parsing this skin from a url.", throwable);
                        return;
                    }
                    core.getNpc().getSettings().setSkinData(skin.data.texture.signature, skin.data.texture.value, "A skin imported via a URL");
                    plugin.urlWaiting.remove(player);
                    player.sendMessage(Utils.style("&aSuccessfully set NPC's skin from " + message));
                    SCHEDULER.runTask(plugin, () -> core.getSkinMenu().open(player));
                });
            } catch (Exception ex) {
                player.sendMessage(Utils.style("&cAn error occurred whilst parsing NPC skin. Is this URL valid?"));
            }
        } else if (plugin.hologramWaiting.contains(player)) {
            if (cancel) {
                plugin.titleWaiting.remove(player);
                SCHEDULER.runTask(plugin, () -> core.getExtraSettingsMenu().open(player));
                e.setCancelled(true);
                return;
            }
            plugin.hologramWaiting.remove(player);
            e.setCancelled(true);
            player.sendMessage(
                    Component.text("Successfully set the NPC's individual clickable hologram to: '", NamedTextColor.GREEN)
                            .append(plugin.getMiniMessage().deserialize(message))
                            .append(Component.text("'", NamedTextColor.GREEN))
            );
            core.getNpc().getSettings().setCustomInteractableHologram(message);
            SCHEDULER.runTask(plugin, () -> core.getExtraSettingsMenu().open(player));
        } else if (plugin.facingWaiting.contains(player)) {
            e.setCancelled(true);
            plugin.facingWaiting.remove(player);
            if(cancel) return;
            if(message.equalsIgnoreCase("confirm")) {
                InternalNpc npc = core.getNpc();
                npc.getSettings().setDirection(player.getLocation().getYaw());
                npc.getSpawnLoc().setPitch(player.getLocation().getPitch());
                player.sendMessage(Utils.style("&aSuccessfully set facing direction!"));
                player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1, 1);
                SCHEDULER.runTask(plugin, () -> core.getMainMenu().open(player));
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
        for (InternalNpc npc : plugin.getNPCs()) npc.injectPlayer(player);
    }

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
            if (distanceSquared <= FIVE_BLOCKS && !npc.getSettings().isTunnelvision()) {
                npc.lookAt(LookAtAnchor.HEAD, player);
            }
        }
    }


    /**
     * Logic for injecting NPCs on world changes
     *
     * @param e Event callback
     */
    @EventHandler
    public void onDimensionChange(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        Location location = player.getLocation();
        World world = player.getWorld();
        for (InternalNpc npc : plugin.npcs.values()) {
            if (world != npc.getWorld()) continue;
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
        plugin.commandWaiting.remove(player);
        plugin.nameWaiting.remove(player);
        plugin.targetWaiting.remove(player);
        plugin.titleWaiting.remove(player);
        plugin.messageWaiting.remove(player);
        plugin.serverWaiting.remove(player);
        plugin.actionbarWaiting.remove(player);
        plugin.urlWaiting.remove(player);
        plugin.playerWaiting.remove(player);
        plugin.hologramWaiting.remove(player);
    }

    @Getter
    private static class MovementData {
        private final UUID uniqueId;
        @Setter
        private Location lastLocation;
        @Setter
        private double distanceSquared;

        MovementData(UUID uniqueId, Location lastLocation, double distanceSquared) {
            this.uniqueId = uniqueId;
            this.lastLocation = lastLocation;
            this.distanceSquared = distanceSquared;
        }

        public MovementData copy() {
            return new MovementData(uniqueId, lastLocation, distanceSquared);
        }
    }
}
