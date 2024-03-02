package dev.foxikle.customnpcs.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.actions.conditions.ActionAdapter;
import dev.foxikle.customnpcs.actions.conditions.Conditional;
import dev.foxikle.customnpcs.actions.conditions.ConditionalTypeAdapter;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.data.Settings;
import dev.foxikle.customnpcs.internal.commands.CommandCore;
import dev.foxikle.customnpcs.internal.commands.NPCActionCommand;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.listeners.Listeners;
import dev.foxikle.customnpcs.internal.menu.MenuCore;
import dev.foxikle.customnpcs.internal.menu.MenuUtils;
import lombok.Getter;
import me.flame.menus.menu.PaginatedMenu;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.mineskin.MineskinClient;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;

/**
 * <p> The class that represents the plugin
 * </p>
 */
public final class CustomNPCs extends JavaPlugin implements PluginMessageListener {
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
    private static boolean wasPreviouslyEnabled = false;
    /**
     * The client for the MineSkin API
     */
    public final MineskinClient MINESKIN_CLIENT = new MineskinClient("MineSkin-JavaClient");
    private final String NPC_CLASS = "dev.foxikle.customnpcs.versions.NPC_%s";
    private final String[] COMPATIBLE_VERSIONS = {"1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4"};
    /**
     * The List of inventories that make up the skin selection menus
     */
    public PaginatedMenu skinCatalogue;
    /**
     * The List of players the plugin is waiting for title text input
     */
    public List<Player> titleWaiting = new ArrayList<>();
    /**
     * The List of players the plugin is waiting for title text input
     */
    public List<Player> targetWaiting = new ArrayList<>();
    /**
     * The List of players the plugin is waiting for server name text input
     */
    public List<Player> serverWaiting = new ArrayList<>();
    /**
     * The List of players the plugin is waiting for action bar text input
     */
    public List<Player> actionbarWaiting = new ArrayList<>();
    /**
     * The List of players the plugin is waiting for message text input
     */
    public List<Player> messageWaiting = new ArrayList<>();
    /**
     * The List of players the plugin is waiting for confirmation on.
     */
    public List<Player> facingWaiting = new ArrayList<>();
    /**
     * The List of players the plugin is waiting for command input
     */
    public List<Player> commandWaiting = new ArrayList<>();
    /**
     * The List of players the plugin is waiting for name text input
     */
    public List<Player> nameWaiting = new ArrayList<>();
    /**
     * The List of players the plugin is waiting for sound text input
     */
    public List<Player> soundWaiting = new ArrayList<>();
    /**
     * The List of players the plugin is waiting for url input
     */
    public List<Player> urlWaiting = new ArrayList<>();
    /**
     * The List of players the plugin is waiting for player name input
     */
    public List<Player> playernameWating = new ArrayList<>();

    /**
     * The list of player the plugin is waiting for input from
     */
    public List<Player> hologramWaiting = new ArrayList<>();
    /**
     * The List of NPC holograms
     */
    public List<TextDisplay> holograms = new ArrayList<>();

    @Getter
    public FileManager fileManager;
    /**
     * The Map of NPCs keyed by their UUIDs
     */
    public Map<UUID, InternalNpc> npcs = new HashMap<>();
    /**
     * The Map of player's MenuCores
     */
    public Map<Player, MenuCore> menuCores = new HashMap<>();
    /**
     * The Map of the action a player is editing
     */
    public Map<Player, Action> editingActions = new HashMap<>();
    /**
     * The Map of the original actions a player is editing
     */
    public Map<Player, Action> originalEditingActions = new HashMap<>();
    /**
     * The Map of the action a player is editing
     */
    public Map<Player, Conditional> editingConditionals = new HashMap<>();
    /**
     * The Map of the original actions a player is editing
     */
    public Map<Player, Conditional> originalEditingConditionals = new HashMap<>();
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

    @Getter public MiniMessage miniMessage = MiniMessage.miniMessage();
    Listeners listeners;
    /**
     * Singleton for menu utilites
     */
    private MenuUtils mu;

    @Getter
    private AutoUpdater updater;

    /**
     * <p> Logic for when the plugin is enabled
     * </p>
     */
    @Override
    public void onEnable() {
        instance = this;
        if (!setup()) {
            Bukkit.getLogger().severe("Incompatible server version! Please use " + Arrays.toString(COMPATIBLE_VERSIONS));
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        registerNpcTeam();

        gson = new GsonBuilder()
                .registerTypeAdapter(Conditional.class, new ConditionalTypeAdapter())
                .registerTypeAdapter(Action.class, new ActionAdapter())
                .create();
        this.fileManager = new FileManager(this);
        this.mu = new MenuUtils(this);
        this.updater = new AutoUpdater(this);
        update = updater.checkForUpdates();
        if (fileManager.createFiles()) {
            getCommand("npc").setExecutor(new CommandCore(this));
            getCommand("npcaction").setExecutor(new NPCActionCommand(this));
            this.getLogger().info("Loading NPCs!");
            for (UUID uuid : fileManager.getNPCIds()) {
                fileManager.loadNPC(uuid);
            }
            Bukkit.getScheduler().runTaskLater(this, () -> Bukkit.getOnlinePlayers().forEach(player -> npcs.values().forEach(npc -> Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> npc.injectPlayer(player), 5))), 20);
            Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> skinCatalogue = this.getMenuUtils().getSkinCatalogue(), 20);
            // setup bstats
            Metrics metrics = new Metrics(this, 18898);

            // setup service manager for the API (This isn't used lol)
            Bukkit.getServer().getServicesManager().register(CustomNPCs.class, this, this, ServicePriority.Normal);

            // setup papi
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                this.getLogger().info("Successfully hooked into PlaceholderAPI.");
                papi = true;
            } else {
                papi = false;
                this.getLogger().warning("Could not find PlaceholderAPI! PlaceholderAPI isn't required, but CustomNPCs does support it.");
            }
        }
        if (!wasPreviouslyEnabled) {
            listeners = new Listeners(this);
            this.getServer().getPluginManager().registerEvents(listeners, this);
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                for (InternalNpc npc : npcs.values()) {
                    if (npc.getSettings().isTunnelvision()) {
                        npc.setYRotation((float) npc.getSettings().getDirection());
                        npc.lookAt(calcLocation(npc));
                    }
                }
            }, 0, 20);
        }
        listeners.start();
        // detecting reloads
        wasPreviouslyEnabled = true;
    }

    private void registerNpcTeam() {
        Team team;
        try {
            team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("npc");
        } catch (IllegalArgumentException ignored) {
            team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("npc");
        }
        if (team == null) throw new NullPointerException("There was an error whilst creating the NPC team!");
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        if (getConfig().getBoolean("DisableCollisions"))
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        team.setPrefix(Utils.style("&8[NPC] "));
    }

    private Location calcLocation(InternalNpc npc) {
        Location loc = npc.getCurrentLocation();
        double yaw = npc.getSettings().getDirection();
        double heading = -Math.toRadians(yaw);
        // trig to calculate the position
        loc.add(5 * Math.sin(heading),
                1.6 + -5 * Math.sin(Math.toRadians(npc.getSpawnLoc().getPitch())),
                5 * Math.cos(heading)
        );

        return loc;
    }

    /**
     * <p> Checks if the plugin is compatable with the server version
     * </p>
     *
     * @return If the plugin is compatable with the server
     */

    public boolean setup() {
        serverVersion = Bukkit.getMinecraftVersion();
        return List.of(COMPATIBLE_VERSIONS).contains(serverVersion);
    }

    /**
     * <p> Logic for when the plugin is disabled
     * </p>
     */
    @Override
    public void onDisable() {
        if (listeners != null) listeners.stop();
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        Bukkit.getServicesManager().unregister(this);
        try {
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam("npc").unregister();
        } catch (IllegalArgumentException | NullPointerException ignored) {
        }
        for (InternalNpc npc : npcs.values()) {
            npc.remove();
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
    public void addNPC(InternalNpc npc, TextDisplay hologram) {
        holograms.add(hologram);
        npcs.put(npc.getUniqueID(), npc);
    }

    /**
     * <p> Gets the delay of an action
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
     * <p> Doesn't do anything since this plugin is not expecting to receive any plugin messages. It exists soley to be able to send a player to a bungeecord server.
     * </p>
     */
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
    }

    /**
     * Creates an npc
     *
     * @param world     the world
     * @param spawnLoc  the location to spawn it
     * @param equipment the equipment object representing the NPC's items
     * @param settings  the settings object representing the NPC's settings
     * @param uuid      the NPC's UUID
     * @param target    the NPC's target to follow
     * @param actions   the NPC's actions
     * @return the created NPC
     */
    public InternalNpc createNPC(World world, Location spawnLoc, Equipment equipment, Settings settings, UUID uuid, @Nullable Player target, List<Action> actions) {
        try {
            Class<?> clazz = Class.forName(String.format(NPC_CLASS, translateVersion()));
            return (InternalNpc) clazz
                    .getConstructor(this.getClass(), World.class, Location.class, Equipment.class, Settings.class, UUID.class, Player.class, List.class)
                    .newInstance(this, world, spawnLoc, equipment, settings, uuid, target, actions);
        } catch (ReflectiveOperationException e) {
            getLogger().log(Level.SEVERE, "An error occoured whilst creating the NPC '{name}! This is most likley a configuration issue.".replace("{name}", settings.getName()), e);
            return null;
        }
    }

    public String translateVersion() {
        switch (serverVersion) {
            case "1.20", "1.20.1" -> {
                return "v1_20_R1";
            }
            case "1.20.2" -> {
                return "v1_20_R2";
            }
            case "1.20.3", "1.20.4" -> {
                return "v1_20_R3";
            }
        }
        return "";
    }
}
