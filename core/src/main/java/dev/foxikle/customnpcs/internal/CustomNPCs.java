/*
 * Copyright (c) 2024-2025. Foxikle
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.actions.LegacyAction;
import dev.foxikle.customnpcs.actions.conditions.ActionAdapter;
import dev.foxikle.customnpcs.actions.conditions.Condition;
import dev.foxikle.customnpcs.actions.conditions.ConditionalTypeAdapter;
import dev.foxikle.customnpcs.actions.defaultImpl.*;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.data.Settings;
import dev.foxikle.customnpcs.internal.commands.NpcCommand;
import dev.foxikle.customnpcs.internal.commands.suggestion.NpcSuggester;
import dev.foxikle.customnpcs.internal.commands.suggestion.SoundSuggester;
import dev.foxikle.customnpcs.internal.commands.suggestion.WorldSuggester;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.listeners.Listeners;
import dev.foxikle.customnpcs.internal.menu.*;
import dev.foxikle.customnpcs.internal.translations.Translations;
import dev.foxikle.customnpcs.internal.utils.ActionRegistry;
import dev.foxikle.customnpcs.internal.utils.AutoUpdater;
import dev.foxikle.customnpcs.internal.utils.WaitingType;
import dev.velix.imperat.BukkitImperat;
import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.Imperat;
import io.github.mqzen.menus.Lotus;
import io.github.mqzen.menus.base.pagination.Pagination;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p> The class that represents the plugin
 * </p>
 */
@Slf4j
public final class CustomNPCs extends JavaPlugin implements PluginMessageListener {

    public static final ActionRegistry ACTION_REGISTRY = new ActionRegistry();
    public static int INTERPOLATION_DURATION;
    /**
     * Singleton for the NPCBuilder
     */
    @Getter
    private static CustomNPCs instance;
    /**
     * The plugin's json handler
     */
    @Getter
    private static Gson gson;

    /**
     * This may contain NPCs that do not yet exist, as they are in the process of creation.
     */
    @Getter
    private final Cache<UUID, InternalNpc> editingNPCs = CacheBuilder.newBuilder().expireAfterWrite(10,
            TimeUnit.MINUTES).expireAfterAccess(10, TimeUnit.MINUTES).build();
    /**
     * Used to close the deletion menu if it was opened by the command. true = open the menu
     */
    @Getter
    private final Cache<UUID, Boolean> deletionReason = CacheBuilder.newBuilder().expireAfterWrite(1,
            TimeUnit.MINUTES).expireAfterAccess(1, TimeUnit.MINUTES).build();
    private final String[] COMPATIBLE_VERSIONS = {"1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6",
            "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9",
            "1.21.10", "1.21.11"};
    private final String NPC_CLASS = "dev.foxikle.customnpcs.versions.NPC_%s";
    /**
     * The map of what the plugin is waiting for the players to enter.
     */
    public Map<UUID, WaitingType> waiting = new ConcurrentHashMap<>();
    /**
     * The List of NPC holograms
     */
    public List<List<TextDisplay>> holograms = new ArrayList<>();
    /**
     * The Map of NPCs keyed by their UUIDs
     */
    public Map<UUID, InternalNpc> npcs = new HashMap<>();
    /**
     * The Map of the action a player is editing
     */
    public Map<UUID, Action> editingActions = new HashMap<>();
    /**
     * The Map of the original actions a player is editing
     */
    public Map<UUID, Action> originalEditingActions = new HashMap<>();
    /**
     * The Map of the action a player is editing
     */
    public Map<UUID, Condition> editingConditionals = new HashMap<>();
    /**
     * The Map of the original actions a player is editing
     */
    public Map<UUID, Condition> originalEditingConditionals = new HashMap<>();
    /**
     * If the plugin should try to format messages with PlaceholderAPI
     */
    public boolean papi = false;
    /**
     * If there is a new update available
     */
    public boolean update;
    /**
     * keeps track of the current server version
     */
    public String serverVersion;
    @Getter
    public MiniMessage miniMessage = MiniMessage.miniMessage();
    Listeners listeners;
    @Getter
    private FileManager fileManager;
    /**
     * Singleton for menu utilities
     */
    private MenuUtils mu;
    @Getter
    private AutoUpdater updater;

    @Getter
    private Lotus lotus;
    @Getter
    private Imperat<BukkitSource> imperat;

    @Getter
    @Setter
    private boolean reloading = false;

    @Getter
    @Setter
    private boolean debug = false;

    /**
     * <p> Logic for when the plugin is enabled
     * </p>
     */
    @Override
    public void onEnable() {
        // paper... why??
        System.setProperty("org.bukkit.plugin.java.LibraryLoader.centralURL", "https://repo1.maven.org/maven2");
        instance = this;

        if (!checkForValidVersion()) {
            printInvalidVersion();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        String s = translateVersion();

        try {
            getLogger().info("Loading class: " + String.format(NPC_CLASS, s));
            getClassLoader().loadClass(String.format(NPC_CLASS, s));
        } catch (ClassNotFoundException e) {
            getLogger().log(Level.SEVERE, "Failed to load NPC class for server version " + s + "!", e);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "SERVERE ERROR: ", e);
        }

        INTERPOLATION_DURATION = getConfig().getInt("DefaultInterpolationDuration");


        new Translations().setup();

        gson = new GsonBuilder()
                .registerTypeAdapter(Condition.class, new ConditionalTypeAdapter())
                .registerTypeAdapter(LegacyAction.class, new ActionAdapter())
                .create();
        this.fileManager = new FileManager(this);
        this.mu = new MenuUtils(this);
        this.updater = new AutoUpdater(this);
        update = updater.checkForUpdates();

        if (!fileManager.createFiles()) {
            throw new RuntimeException("Failed to create files");
        }

        getLogger().info("Loading action registry...");
        ACTION_REGISTRY.register("ActionBar", ActionBar.class, ActionBar::creationButton);
        ACTION_REGISTRY.register("DisplayTitle", DisplayTitle.class, DisplayTitle::creationButton);
        ACTION_REGISTRY.register("GiveEffect", GiveEffect.class, GiveEffect::creationButton);
        ACTION_REGISTRY.register("GiveXP", GiveXP.class, GiveXP::creationButton);
        ACTION_REGISTRY.register("PlaySound", PlaySound.class, PlaySound::creationButton);
        ACTION_REGISTRY.register("RemoveEffect", RemoveEffect.class, RemoveEffect::creationButton);
        ACTION_REGISTRY.register("RemoveXP", RemoveXP.class, RemoveXP::creationButton);
        ACTION_REGISTRY.register("RunCommand", RunCommand.class, RunCommand::creationButton);
        ACTION_REGISTRY.register("SendMessage", SendMessage.class, SendMessage::creationButton);
        ACTION_REGISTRY.register("SendServer", SendServer.class, SendServer::creationButton, true, false, true);
        ACTION_REGISTRY.register("Teleport", Teleport.class, Teleport::creationButton);

        try {
            this.getLogger().info("Loading NPCs!");
            for (UUID uuid : fileManager.getValidNPCs()) {
                fileManager.loadNPC(uuid);
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to load NPC:", e);
        }

        //generate skin menus for the supported locales
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            this.getMenuUtils().getSkinCatalogue(Locale.ENGLISH);
            this.getMenuUtils().getSkinCatalogue(Locale.GERMAN);
            this.getMenuUtils().getSkinCatalogue(Locale.SIMPLIFIED_CHINESE);
            this.getMenuUtils().getSkinCatalogue(new Locale("ru"));
        }, 20);

        // setup bstats
        Metrics metrics = new Metrics(this, 18898);

        metrics.addCustomChart(new SimplePie("use_papi", () -> String.valueOf(papi)));
        metrics.addCustomChart(new SimplePie("look_interval",
                () -> String.valueOf(getConfig().getInt("LookInterval"))));
        metrics.addCustomChart(new SimplePie("injection_interval", () -> String.valueOf(getConfig().getInt(
                "InjectionInterval"))));
        metrics.addCustomChart(new SimplePie("injection_distance", () -> String.valueOf(getConfig().getInt(
                "InjectionDistance"))));
        metrics.addCustomChart(new SimplePie("hologram_interval", () -> String.valueOf(getConfig().getInt(
                "HologramUpdateInterval"))));
        metrics.addCustomChart(new SimplePie("update_alerts", () -> String.valueOf(getConfig().getBoolean(
                "AlertOnUpdate"))));
        metrics.addCustomChart(new SimplePie("npc_count", () -> String.valueOf(npcs.size())));

        // only supports default actions
        metrics.addCustomChart(new AdvancedPie("total_actions", () -> {

            int actionbar = 0;
            int title = 0;
            int message = 0;
            int give_effect = 0;
            int remove_effect = 0;
            int give_xp = 0;
            int remove_xp = 0;
            int play_sound = 0;
            int teleport = 0;
            int send_server = 0;
            int run_command = 0;

            for (InternalNpc npc : npcs.values()) {
                for (Action action : npc.getActions()) {
                    if (action instanceof ActionBar) actionbar++;
                    else if (action instanceof DisplayTitle) title++;
                    else if (action instanceof SendMessage) message++;
                    else if (action instanceof GiveEffect) give_effect++;
                    else if (action instanceof RemoveEffect) remove_effect++;
                    else if (action instanceof GiveXP) give_xp++;
                    else if (action instanceof RemoveXP) remove_xp++;
                    else if (action instanceof PlaySound) play_sound++;
                    else if (action instanceof Teleport) teleport++;
                    else if (action instanceof SendServer) send_server++;
                    else if (action instanceof RunCommand) run_command++;
                }
            }

            return Map.ofEntries(
                    Map.entry("ActionBar", actionbar),
                    Map.entry("DisplayTitle", title),
                    Map.entry("SendMessage", message),
                    Map.entry("GiveEffect", give_effect),
                    Map.entry("RemoveEffect", remove_effect),
                    Map.entry("GiveXP", give_xp),
                    Map.entry("RemoveXP", remove_xp),
                    Map.entry("PlaySound", play_sound),
                    Map.entry("Teleport", teleport),
                    Map.entry("SendServer", send_server),
                    Map.entry("RunCommand", run_command)
            );
        }));

        // setup papi
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.getLogger().info("Successfully hooked into PlaceholderAPI.");
            papi = true;
        } else {
            papi = false;
            this.getLogger().warning("Could not find PlaceholderAPI! PlaceholderAPI isn't required, but CustomNPCs " +
                    "does support it.");
        }

        if (!System.getProperties().containsKey("customnpcs-reload-check")) {
            getLogger().info("Loading listeners...");
            listeners = new Listeners(this);
            this.getServer().getPluginManager().registerEvents(listeners, this);
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        }

        listeners.start();

        getLogger().info("Loading menus!");

        lotus = Lotus.load(this);
        lotus.registerMenu(new ActionMenu());
        lotus.registerMenu(new ActionCustomizerMenu());
        lotus.registerMenu(new MainNPCMenu());
        lotus.registerMenu(new ConditionCustomizerMenu());
        lotus.registerMenu(new ConditionMenu());
        lotus.registerMenu(new DeleteMenu());
        lotus.registerMenu(new EquipmentMenu());
        lotus.registerMenu(new ExtraSettingsMenu());
        lotus.registerMenu(new NewActionMenu());
        lotus.registerMenu(new NewConditionMenu());
        lotus.registerMenu(new SkinCatalog());
        lotus.registerMenu(new SkinMenu());
        lotus.registerMenu(new HologramMenu());
        lotus.registerMenu(new DeleteLineMenu());
        lotus.registerMenu(new PoseEditorMenu());

        // prevent reload goofery
        if (!System.getProperties().containsKey("CUSTOMNPCS_LOADED")) {
            getLogger().info("Loading commands!");

            imperat = BukkitImperat.builder(this).applyBrigadier(false)//todo: true
                    .namedSuggestionResolver("sound", new SoundSuggester())
                    .namedSuggestionResolver("current_npc", new NpcSuggester())
                    .namedSuggestionResolver("broken_npc", new NpcSuggester())
                    .namedSuggestionResolver("worlds", new WorldSuggester())
                    .build();

            // only one command, the rest are sub commands
            imperat.registerCommand(new NpcCommand());
        }

        System.setProperty("CUSTOMNPCS_LOADED", "true");
    }

    /**
     * <p> Checks if the plugin is compatible with the server version
     * </p>
     *
     * @return If the plugin is compatible with the server
     */

    public boolean checkForValidVersion() {
        serverVersion = Bukkit.getMinecraftVersion();
        return List.of(COMPATIBLE_VERSIONS).contains(serverVersion);
    }

    /**
     * <p> Logic for when the plugin is disabled
     * </p>
     */
    @Override
    public void onDisable() {
        for (List<TextDisplay> t : holograms) {
            if (t != null) t.forEach(TextDisplay::remove);
        }
        if (listeners != null) listeners.stop();

        HandlerList.unregisterAll(this);

        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        Bukkit.getScheduler().cancelTasks(this);
        for (InternalNpc npc : npcs.values()) {
            npc.remove();
        }

        holograms.clear();
        npcs.clear();

        // don't unregister these on reload
        if (!reloading) {
            if (imperat != null) {
                imperat.shutdownPlatform();
                imperat.unregisterAllCommands();
            }
        }
    }

    /**
     * <p> Gets list of current NPCs
     * </p>
     *
     * @return the list of current NPCs
     */
    public List<InternalNpc> getNPCs() {
        return npcs.values().stream().toList();
    }

    /**
     * <p> Adds an NPC to the list of current NPCs
     * </p>
     *
     * @param npc      The NPC to add
     * @param hologram the TextDisplay representing the NPC's name
     */
    public void addNPC(InternalNpc npc, List<TextDisplay> hologram) {
        holograms.add(hologram);
        npcs.put(npc.getUniqueID(), npc);
    }

    /**
     * <p> Gets the NPC by its UUID
     * </p>
     *
     * @param uuid The UUID of the npc
     * @return the NPC of the specified UUID
     * @throws NullPointerException if the specified UUID is null
     */
    public InternalNpc getNPCByID(UUID uuid) {
        if (uuid == null) throw new NullPointerException("uuid cannot be null");
        if (!npcs.containsKey(uuid))
            return null;
        return npcs.get(uuid);
    }

    /**
     * <p> Gets the MenuUtils object
     * </p>
     *
     * @return the MenuUtils object
     */
    public MenuUtils getMenuUtils() {
        return mu;
    }

    /**
     * <p> Doesn't do anything since this plugin is not expecting to receive any plugin messages. It exists sole to
     * be able to send a player to a bungeecord server.
     * </p>
     */
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
    }

    /**
     * Creates an npc
     *
     * @param world       the world
     * @param location    the location to spawn it
     * @param equipment   the equipment object representing the NPC's items
     * @param settings    the settings object representing the NPC's settings
     * @param uuid        the NPC's UUID
     * @param target      the NPC's target to follow
     * @param actionImpls the NPC's actions
     * @return the created NPC
     * @throws RuntimeException If the reflective creation of the NPC object fails
     */
    public InternalNpc createNPC(World world, Location location, Equipment equipment, Settings settings, UUID uuid,
                                 @Nullable Player target, List<Action> actionImpls) {
        try {
            Class<?> clazz = Class.forName(String.format(NPC_CLASS, translateVersion()));
            return (InternalNpc) clazz
                    .getConstructor(this.getClass(), World.class, Location.class, Equipment.class, Settings.class,
                            UUID.class, Player.class, List.class)
                    .newInstance(this, world, location, equipment, settings, uuid, target, actionImpls);
        } catch (ReflectiveOperationException e) {
            getLogger().log(Level.SEVERE, ("An error occurred whilst creating the NPC '{name}! This is most likely a " +
                    "configuration issue.").replace("{name}", settings.getName()), e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error occurred whilst creating the NPC '{name}!".replace("{name}",
                    settings.getName()), e);
            throw new RuntimeException(e);
        }
    }

    public String translateVersion() {
        return switch (serverVersion) {
            case "1.20", "1.20.1" -> "v1_20_R1";
            case "1.20.2" -> "v1_20_R2";
            case "1.20.3", "1.20.4" -> "v1_20_R3";
            case "1.20.5", "1.20.6" -> "v1_20_R4";
            case "1.21", "1.21.1" -> "v1_21_R0";
            case "1.21.2", "1.21.3" -> "v1_21_R1";
            case "1.21.4" -> "v1_21_R2";
            case "1.21.5" -> "v1_21_R3";
            case "1.21.6", "1.21.7", "1.21.8" -> "v1_21_R4";
            case "1.21.9", "1.21.10" -> "v1_21_R5";
            case "1.21.11" -> "v1_21_R6";
            default -> "";
        };
    }

    private Locale setupLocale(String lang) {
        return switch (lang.toUpperCase()) {
            case "GERMAN", "DE" -> Locale.GERMAN;
            case "CHINESE", "ZH" -> Locale.CHINESE;
            case "RUSSIAN", "RU" -> new Locale("ru");
            default -> Locale.ENGLISH;
        };
    }


    private void printInvalidVersion() {
        Logger logger = getLogger();
        logger.severe("");
        logger.severe("");
        logger.severe("+------------------------------------------------------------------------------+");
        logger.severe("|                      INVALID SERVER VERSION DETECTED                         |");
        logger.severe("|             ** PLEASE USE ONE OF THE FOLLOWING SERVER VERSIONS **            |");
        logger.severe("|                                [1.20.x, 1.21.x]                              |");
        logger.severe("|                               DETECTED: '" + serverVersion + "'                             " +
                "|");
        logger.severe("|           Please contact @foxikle on Discord for more information.           |");
        logger.severe("+------------------------------------------------------------------------------+");
        logger.severe("");
        logger.severe("");
    }

    public boolean isWaiting(Player player, WaitingType type) {
        return waiting.getOrDefault(player.getUniqueId(), null) == type;
    }

    public void wait(Player player, WaitingType type) {
        waiting.put(player.getUniqueId(), type);
    }

    public Pagination getSkinCatalog(Player player) {
        return getMenuUtils().getSkinCatalogue(player.locale());
    }

}
