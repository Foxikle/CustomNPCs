/*
 * Copyright (c) 2024-2026. Foxikle
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

package dev.foxikle.customnpcs.internal.listeners;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.actions.impl.*;
import dev.foxikle.customnpcs.api.Skin;
import dev.foxikle.customnpcs.api.events.NpcInteractEvent;
import dev.foxikle.customnpcs.conditions.Condition;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.LookAtAnchor;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.menu.PoseEditorMenu;
import dev.foxikle.customnpcs.internal.translations.Arg;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.SkinUtils;
import dev.foxikle.customnpcs.internal.utils.WaitingType;
import dev.foxikle.customnpcs.internal.utils.await.AwaitInput;
import dev.foxikle.customnpcs.internal.utils.await.AwaitSignal;
import dev.foxikle.customnpcs.internal.utils.await.AwaitSkin;
import dev.foxikle.customnpcs.internal.utils.await.AwaitString;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.world.WorldGameRuleChangeEvent;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mineskin.data.CodeAndMessage;
import org.mineskin.data.Visibility;
import org.mineskin.exception.MineSkinRequestException;
import org.mineskin.request.GenerateRequest;
import org.mineskin.response.MineSkinResponse;
import studio.mevera.lotus.api.menu.MenuView;

import java.net.URI;
import java.net.URL;
import java.util.*;
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
    private static final ConsoleCommandSender CONSOLE_SENDER = Bukkit.getConsoleSender();
    private static final Pattern PATTERN = Pattern.compile(" ");
    private final Map<UUID, Integer> worldSleepingPercentages = new ConcurrentHashMap<>();
    /**
     * The instance of the main Class
     */
    private final CustomNPCs plugin;

    // Executors for better handling of async scheduling than that bukkit scheduler
    private ScheduledExecutorService service;


    private final Set<String> CANCEL_VERBS = Set.of("cancel", "stop", "quit", "exit");
    private final Set<String> DONE_VERBS = Set.of("confirm", "done", "finish");

    public Listeners(CustomNPCs plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getWorlds().forEach(world -> worldSleepingPercentages.put(world.getUID(),
            world.getGameRuleValue(GameRule.PLAYERS_SLEEPING_PERCENTAGE)));
        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> Bukkit.getOnlinePlayers().forEach(this::actionPlayerMovement), 1000,
            plugin.getConfig().getInt("LookInterval") * 50L, TimeUnit.MILLISECONDS);
    }


    public void stop() {
        service.shutdown();
        Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE,
            worldSleepingPercentages.get(world.getUID())));
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
        if (player.getGameMode() == GameMode.SPECTATOR) return; // we don't care about spectators
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

    private void processPlayerMovement(final Player player, final InternalNpc npc, final World world,
                                       final World npcWorld, final Location location, final UUID uuid) {
        if (player.getGameMode() == GameMode.SPECTATOR) return; // we don't care about spectators
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
        if (npc.getSettings().isTunnelvision()) return;
        if (distanceSquared > FIVE_BLOCKS) {
            SCHEDULER.runTask(plugin, () -> {
                Collection<Entity> entities = npcWorld.getNearbyEntities(npc.getCurrentLocation(), 2.5, 2.5, 2.5);
                entities.removeIf(entity -> entity.getScoreboardTags().contains("NPC"));
                for (Entity en : entities) {
                    if (!(en instanceof Player p)) continue;
                    if (p.getGameMode() == GameMode.SPECTATOR) continue;
                    if (!npc.getSettings().isTunnelvision()) {
                        npc.lookAt(LookAtAnchor.HEAD, p);
                        return;
                    }
                }
                npc.setYRotation(npc.getSpawnLoc().getYaw());
                npc.setXRotation(npc.getSpawnLoc().getPitch());
            });
        }
    }

    private void trackFromTo(Player player, InternalNpc npc, MovementData data, MovementData oldData) {
        if (data.distanceSquared <= FIVE_BLOCKS && !npc.getSettings().isTunnelvision() && player.getGameMode() != GameMode.SPECTATOR) {
            npc.lookAt(LookAtAnchor.HEAD, player);
        }
    }

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
                NpcInteractEvent event = new NpcInteractEvent(player, npc);
                Bukkit.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) return;
                npc.getActions().forEach(action -> SCHEDULER.runTaskLater(plugin, () ->
                    action.perform(npc, player), action.getDelay()));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        actionPlayerMovement(player);
    }


    private void stopWait(Player p) {
        CustomNPCs.getInstance().waiting.remove(p.getUniqueId());
    }

    private void handleSignalAwait(@NotNull AwaitSignal as, AsyncChatEvent e, @Nullable Action a,
                                   @Nullable InternalNpc n, @NotNull Player p, String message) {
        if (!DONE_VERBS.contains(message.toLowerCase())) return;
        WaitingType type = CustomNPCs.getInstance().waiting.get(p.getUniqueId()).type();
        switch (type) {
            case RECORDING -> {
                if (!(a instanceof FollowPresetPath)) {
                    as.fail("Action != FollowPresetPath");
                    stopWait(p);
                    return;
                }
                as.complete();
                //fixme: The following lines should be covered by the FollowPresetPath call to Await
//            List<RecordedPathNode> path = FollowPresetPath.stopRecording(player);
//            follow.setPath(path);
//            player.sendMessage(Msg.get(player, "action.set.recording", Arg.arg(path.size())));
//            SCHEDULER.runTask(plugin, () -> plugin.getLotus().openMenu(player, follow.getMenu()));
            }
            case FACING -> {
                e.setCancelled(true);
                plugin.waiting.remove(p.getUniqueId());
                if (n == null) {
                    as.fail("npc == null");
                    p.sendMessage(Msg.get(p, "error.npc-menu-expired"));
                    return;
                }
                as.complete();
            }
        }
        e.setCancelled(true);
        stopWait(p);
    }

    private void handleStringAwait(@NotNull AwaitString as, AsyncChatEvent e, @Nullable Action a,
                                   @Nullable Condition c, @Nullable InternalNpc n, @NotNull Player p,
                                   String message) {
        WaitingType type = CustomNPCs.getInstance().waiting.get(p.getUniqueId()).type();
        e.setCancelled(true);
        switch (type) {
            case COMMAND -> {
                if (!(a instanceof RunCommand)) {
                    as.fail("Action != RunCommand");
                    stopWait(p);
                    return;
                }

                as.complete(message);
            }
            case NAME -> {
                if (n == null) {
                    as.fail("npc == null");
                    stopWait(p);
                    p.sendMessage(Msg.get(p, "error.npc-menu-expired"));
                    return;
                }

                //fixme: ensure hanging % signs are escaped :)
                as.complete(message.replace("%empty%", ""));

                //fixme: impl
//                int index = HologramMenu.editingIndicies.get(p.getUniqueId());
//            if (npc.getSettings().getRawHolograms().size() <= index) {
//                npc.getSettings().getRawHolograms().add(finalMessage);
//            } else {
//                npc.getSettings().getRawHolograms().set(index, finalMessage);
//            }
//            player.sendMessage(Msg.get(player, "set.name", Arg.arg(index + 1), Arg.arg(finalMessage)));
//            SCHEDULER.runTask(plugin, () -> plugin.getLotus().openMenu(player, MenuUtils.NPC_HOLOGRAMS));
            }
            case TARGET -> {
                if (c == null) {
                    as.fail("condition == null");
                    stopWait(p);
                    return;
                }

                if (c.getType() == Condition.Type.NUMERIC) {
                    try {
                        Double.parseDouble(message);
                    } catch (NumberFormatException ignored) {
                        p.sendMessage(Msg.get(p, "error.parse_number", Arg.arg(message)));
                        return;
                    }
                }
                as.complete(message);
            }
            case TITLE, SUBTITLE -> {
                if (!(a instanceof DisplayTitle)) {
                    as.fail("action != DisplayTitle");
                    stopWait(p);
                    return;
                }

                as.complete(message);
            }
            case MESSAGE -> {
                if (!(a instanceof SendMessage)) {
                    as.fail("action != SendMessage");
                    stopWait(p);
                    return;
                }

                as.complete(message);
            }
            case SERVER -> {
                if (!(a instanceof SendServer runServer)) {
                    as.fail("action != SendServer");
                    stopWait(p);
                    return;
                }
                as.complete(message);
            }
            case ACTIONBAR -> {
                if (!(a instanceof ActionBar)) {
                    as.fail("action != ActionBar");
                    return;
                }
                as.complete(message);
            }
            case HOLOGRAM -> {
                if (n == null) {
                    as.fail("npc == null");
                    p.sendMessage(Msg.get(p, "error.npc-menu-expired"));
                    return;
                }

                as.complete(message);
            }
        }
        stopWait(p);
    }

    private void handleSkinAwait(@NotNull AwaitSkin as, AsyncChatEvent e, @Nullable InternalNpc npc,
                                 @NotNull Player player, String message) {
        WaitingType type = CustomNPCs.getInstance().waiting.get(player.getUniqueId()).type();
        e.setCancelled(true);
        switch (type) {
            case URL -> {
                if (npc == null) {
                    as.fail("npc == null");
                    player.sendMessage(Msg.get(player, "error.npc-menu-expired"));
                    return;
                }
                player.sendMessage(Msg.get(player, "skins.fetching.url"));
                try {
                    URL url = new URI(message).toURL();

                    GenerateRequest request = GenerateRequest.url(url)
                        .name("URL Generated Skin")
                        .visibility(Visibility.UNLISTED);
                    SkinUtils.fetch(request).thenAccept(skin -> {
                            as.complete(new Skin(
                                skin.texture().data().signature(),
                                skin.texture().data().value(),
                                Msg.translatedString(player.locale(), "skins.imported_by.url")
                            ));
                            stopWait(player);
                        })
                        .exceptionally(throwable -> {

                            if (throwable instanceof CompletionException completionException) {
                                throwable = completionException.getCause();
                            }

                            throwable.printStackTrace(System.err);
                            if (throwable instanceof MineSkinRequestException requestException) {
                                // get error details
                                MineSkinResponse<?> response = requestException.getResponse();
                                Optional<CodeAndMessage> detailsOptional = response.getErrorOrMessage();
                                Throwable finalThrowable = throwable;
                                detailsOptional.ifPresent(details -> {
                                    plugin.getLogger().log(Level.SEVERE, details.code() + " : " + details,
                                        finalThrowable);
                                    System.out.println(details.code() + ": " + details.message());
                                });
                                as.fail("Internal Error");
                            }

                            if (throwable.getMessage().equalsIgnoreCase("java.lang.RuntimeException: org.mineskin.data.MineskinException: Failed to find image from url")) {

                                player.sendMessage(Msg.get(player, "skins.errors.no_image_data"));
                                as.fail("No image data");
                                return null;
                            }
                            player.sendMessage(Msg.get(player, "skins.errors.unknown_url_error"));
                            plugin.getLogger().log(Level.SEVERE, "An error occurred whilst parsing this skin from a url.", throwable);
                            as.fail("Unknown URL");
                            return null;
                        });
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                    player.sendMessage(Msg.get(player, "skins.errors.invalid_url"));
                    as.fail("Invalid URL");
                }
            }
            case PLAYER -> {
                if (npc == null) {
                    as.fail("npc == null");
                    player.sendMessage(Msg.get(player, "error.npc-menu-expired"));
                    return;
                }


                // this runs on an async thread, so there isn't any need to do this async :)
                player.sendMessage(Msg.get(player, "skins.fetching.player", Arg.arg(message)));

                PlayerProfile profile = Bukkit.createProfile(message);
                if (!profile.complete()) {
                    as.fail("profile != complete");
                    player.sendMessage(Msg.get(player, "skins.errors.player_does_not_exist", Arg.arg(message)));
                    return;
                }

                for (ProfileProperty property : profile.getProperties()) {
                    if (!property.getName().equals("textures")) continue;
                    as.complete(new Skin(property.getSignature(), property.getValue(), Msg.plainText(Msg.get(player, "skins.imported_by.player_name", Arg.arg(message)))));
                }
            }
        }
        stopWait(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent e) {
        Player player = e.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(e.message());
        boolean cancel = CANCEL_VERBS.contains(message.toLowerCase());

        if (plugin.isWaiting(player) && cancel) {
            plugin.waiting.remove(player.getUniqueId()).cancel();
            return;
        }

        AwaitInput<?> as = plugin.waiting.get(player.getUniqueId());

        Action a = as.data().getOrNull(Action.KEY);
        Condition condition = as.data().getOrNull(Condition.KEY);
        InternalNpc npc = as.data().getOrNull(InternalNpc.KEY);
        switch (as) {
            case AwaitString input -> handleStringAwait(input, e, a, condition, npc, player, message);
            case AwaitSignal input -> handleSignalAwait(input, e, a, npc, player, message);
            case AwaitSkin input -> handleSkinAwait(input, e, npc, player, message);
            default -> throw new IllegalStateException("Unexpected value: " + as);
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (plugin.update && plugin.getConfig().getBoolean("AlertOnUpdate") && player.hasPermission("customnpcs.alert")) {
            player.sendMessage(Msg.get(player, "should_update").appendNewline()
                .append(Msg.format("<click:open_url:https://modrinth.com/plugin/customnpcs/versions><b><#1bd96a>[Modrinth]")));
        }
        recalcSleepingPercentages();
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        Location location = player.getLocation();
        World world = player.getWorld();

        for (InternalNpc npc : plugin.npcs.values()) {
            Location spawnLocation = npc.getSpawnLoc();
            if (world != npc.getWorld()) continue;

            double distanceSquared = location.distanceSquared(spawnLocation);
            if (distanceSquared <= FIVE_BLOCKS && !npc.getSettings().isTunnelvision()) {
                npc.lookAt(LookAtAnchor.HEAD, player);
            }
        }
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
        recalcSleepingPercentages();
        Player player = e.getPlayer();

        if (player.getGameMode() == GameMode.SPECTATOR) return;

        Location location = player.getLocation();
        World world = player.getWorld();
        for (InternalNpc npc : plugin.npcs.values()) {
            Location spawnLocation = npc.getSpawnLoc();
            if (world != npc.getWorld()) continue;

            double distanceSquared = location.distanceSquared(spawnLocation);
            if (distanceSquared <= FIVE_BLOCKS && !npc.getSettings().isTunnelvision()) {
                npc.lookAt(LookAtAnchor.HEAD, player);
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
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        Location location = player.getLocation();
        World world = player.getWorld();
        for (InternalNpc npc : plugin.npcs.values()) {
            Location spawnLocation = npc.getSpawnLoc();
            if (world != npc.getWorld()) return;

            double distanceSquared = location.distanceSquared(spawnLocation);
            if (distanceSquared <= FIFTY_BLOCKS) {
                SCHEDULER.runTaskLater(plugin, () -> npc.getInjectionManager().markForInjection(player.getUniqueId()), 5);
            }

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

        if (player.getGameMode() == GameMode.SPECTATOR) return;

        Location location = player.getLocation();
        World world = player.getWorld();

        for (InternalNpc npc : plugin.npcs.values()) {
            Location spawnLocation = npc.getSpawnLoc();
            if (world != npc.getWorld()) continue;

            double distanceSquared = location.distanceSquared(spawnLocation);
            if (distanceSquared <= FIVE_BLOCKS && !npc.getSettings().isTunnelvision()) {
                npc.lookAt(LookAtAnchor.HEAD, player);
            }
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
        AwaitInput<?> ai = plugin.waiting.remove(e.getPlayer().getUniqueId());
        if (ai != null) ai.cancel();
        Bukkit.getScheduler().runTaskLater(plugin, this::recalcSleepingPercentages, 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent e) {
        if (e.isCancelled()) {
            return;
        }

        Player clicker = (Player) e.getWhoClicked();
        MenuView<Component, ?> menu = plugin.getLotus().resolveView(clicker);

        if (menu != null) {
            if (e.getClick() == ClickType.DOUBLE_CLICK) e.setCancelled(true);
        }
    }

    private void recalcSleepingPercentages() {
        Bukkit.getWorlds().forEach(world -> {
            if (world == null) return;
            if (worldSleepingPercentages.get(world.getUID()) == null) return;
            int target = worldSleepingPercentages.get(world.getUID());
            int npcCount = plugin.getNPCs().stream().filter(npc -> npc.getWorld() == world).toList().size();
            int playercount = world.getPlayers().size();
            if (npcCount == 0) {
                world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, target);
                return;
            }
            world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE,
                (int) (((playercount - npcCount) / (double) playercount) * target));
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameruleChange(WorldGameRuleChangeEvent event) {
        if (event.getCommandSender() == null) return; // prevent stack overflows
        if (!event.getGameRule().equals(GameRule.PLAYERS_SLEEPING_PERCENTAGE)) return;
        worldSleepingPercentages.put(event.getWorld().getUID(), Integer.parseInt(event.getValue()));
        recalcSleepingPercentages();
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        worldSleepingPercentages.put(event.getWorld().getUID(),
            event.getWorld().getGameRuleValue(GameRule.PLAYERS_SLEEPING_PERCENTAGE));
        recalcSleepingPercentages();
    }

    @EventHandler
    public void onWorld(WorldUnloadEvent event) {
        worldSleepingPercentages.remove(event.getWorld().getUID());
        recalcSleepingPercentages();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onScroll(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isWaiting(player, WaitingType.NUDGE)) return;
        AwaitSignal as = (AwaitSignal) plugin.waiting.get(player.getUniqueId());
        if (as == null) return;

        if (as.data().getOrNull(InternalNpc.KEY) == null) {
            player.sendMessage(Msg.get(player, "error.npc-menu-expired"));
            plugin.waiting.remove(player.getUniqueId()).fail("No NPC");
            return;
        }

        InternalNpc npc = as.data().getOrNull(PoseEditorMenu.PREVIEW_NPC);
        if (npc == null) {
            as.fail("No Preview");
            // bad
            return;
        }
        int delta = (event.getNewSlot() - event.getPreviousSlot() + 9) % 9;
        double multiplier = delta <= 4 ? -.05 : .05; // handle toward or away
        multiplier = player.isSneaking() ? multiplier * 5 : multiplier; // sneaking makes it use larger jumps

        BlockFace face = player.getFacing();
        if (player.getLocation().getPitch() < -45) face = BlockFace.UP;
        if (player.getLocation().getPitch() > 45) face = BlockFace.DOWN;

        Vector vec = face.getDirection().multiply(multiplier);
        player.playSound(player, Sound.ITEM_FLINTANDSTEEL_USE, 1, 1);
        npc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, npc.getCurrentLocation().add(vec), 10, 0, 0, 0, 0);
        npc.moveTo(vec);
    }

    @EventHandler
    public void onSwapToOffhand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (plugin.isWaiting(player, WaitingType.NUDGE)) {
            event.setCancelled(true);
            AwaitSignal as = (AwaitSignal) plugin.waiting.get(player.getUniqueId());
            if (as == null) return;

            InternalNpc npc = as.data().getOrNull(InternalNpc.KEY);
            if (npc == null) {
                as.fail("npc == null");
                player.sendMessage(Msg.get(player, "error.npc-menu-expired"));
                plugin.waiting.remove(player.getUniqueId()).fail("No NPC");
                return;
            }
            stopWait(player);
            as.complete();
        }
    }

    @EventHandler
    public void serverLoadEvent(ServerLoadEvent event) {
        SCHEDULER.runTaskLater(plugin, () -> {
            // setup papi, which happens after the server load event is fired.
            Plugin placeholderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
            if (placeholderAPI != null) {
                PlaceholderAPIPlugin papiPlugin = (PlaceholderAPIPlugin) placeholderAPI;
                plugin.papiPlayerExpansion = papiPlugin.getLocalExpansionManager().getExpansion("Player") != null;

                plugin.getLogger().info("Successfully hooked into PlaceholderAPI.");
                plugin.papi = true;
            } else {
                plugin.papi = false;
            }
            plugin.checkForPlaceholderViolations();
        }, 30);
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
