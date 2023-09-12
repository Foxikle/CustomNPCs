package dev.foxikle.customnpcs;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.foxikle.customnpcs.commands.CommandCore;
import dev.foxikle.customnpcs.commands.NPCActionCommand;
import dev.foxikle.customnpcs.conditions.Conditional;
import dev.foxikle.customnpcs.conditions.ConditionalTypeAdapter;
import dev.foxikle.customnpcs.listeners.Listeners;
import dev.foxikle.customnpcs.listeners.NPCMenuListeners;
import dev.foxikle.customnpcs.menu.MenuCore;
import dev.foxikle.customnpcs.menu.MenuUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

    /**
     * <p> The class that represents the plugin
     * </p>
     */
public final class CustomNPCs extends JavaPlugin implements PluginMessageListener {
    /**
     * The List of inventories that make up the skin selection menus
     */
    public List<Inventory> catalogueInventories;

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
     * The List of players the plugin is waiting for command command input
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
     * The List of NPC holograms
     */
    public List<TextDisplay> holograms = new ArrayList<>();

    /**
     * The Singleton of the FileManager class
     */
    public FileManager fileManager;

    /**
     * The Map of the pages players are on. Keyed by player.
     */
    public Map<Player, Integer> pages = new HashMap<>();

    /**
     * The Map of NPCs keyed by their UUIDs
     */
    public Map<UUID, NPC> npcs = new HashMap<>();

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
    public Map<Player, String> originalEditingActions = new HashMap<>();

    /**
     * The Map of the action a player is editing
     */
    public Map<Player, Conditional> editingConditionals = new HashMap<>();

    /**
     * The Map of the original actions a player is editing
     */
    public Map<Player, String> originalEditingConditionals = new HashMap<>();

    /**
     * Singleton for the NPCBuilder
     */
    private static CustomNPCs instance;

    /**
     * Singleton for menu utilites
     */
    private MenuUtils mu;

    /**
     * Singleton for automatic updates
     */
    private AutoUpdater updater;

    /**
    * If the plugin should try to format messages with PlaceholderAPI
    */
    public boolean papi = false;

    /**
     * The plugin's json handler
     */
    private static Gson gson;

    /**
     * If there is a new update available
     */
    public boolean update;

    /**
     * <p> Logic for when the plugin is enabled
     * </p>
     */
    @Override
    public void onEnable() {
        instance = this;
        if(!setup()){
            Bukkit.getLogger().severe("Incompatible server version! Please use 1.20 or 1.20.1! Shutting down plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        try {
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("npc");
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            team.setPrefix(ChatColor.DARK_GRAY + "[NPC] ");
        } catch (IllegalArgumentException ignored) {
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("npc");
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            team.setPrefix(ChatColor.DARK_GRAY + "[NPC] ");
        }

        this.getServer().getPluginManager().registerEvents(new NPCMenuListeners(this), this);
        this.getServer().getPluginManager().registerEvents(new Listeners(this), this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        gson = new GsonBuilder()
                .registerTypeAdapter(Conditional.class, new ConditionalTypeAdapter())
                .create();
        this.fileManager = new FileManager(this);
        this.mu = new MenuUtils(this);
        this.updater = new AutoUpdater(this);
        update = updater.checkForUpdates();
        if(fileManager.createFiles()){
            getCommand("npc").setExecutor(new CommandCore(this));
            getCommand("npcaction").setExecutor(new NPCActionCommand(this));
            this.getLogger().info("Loading NPCs!");
            for (UUID uuid : fileManager.getNPCIds()) {
                fileManager.loadNPC(uuid);
            }
            Bukkit.getScheduler().runTaskLater(this, () -> Bukkit.getOnlinePlayers().forEach(player -> npcs.values().forEach(npc -> Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> npc.injectPlayer(player), 5))), 20);
            Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> catalogueInventories = this.getMenuUtils().getCatalogueInventories(), 20);
            // setup bstats
            Metrics metrics = new Metrics(this, 18898);

            // setup service manager for the API
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

    }
    /**
     * <p> Checks if the plugin is compatable with the server version
     * </p>
     * @return If the plugin is compatable with the server
     */
    public boolean setup(){
        String sversion = "N/A";
        try{
            sversion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException ex){
            return false;
        }
        return (sversion.equals("v1_20_R1") || sversion.equals("v1_20_1_R1"));
    }

    /**
     * <p> Logic for when the plugin is disabled
     * </p>
     */
    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        Bukkit.getServicesManager().unregister(this);
        try {
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam("npc").unregister();
        } catch (IllegalArgumentException | NullPointerException ignored) {}
        for (NPC npc : npcs.values()) {
            npc.remove();
        }
    }

    /**
     * <p> Gets list of current NPCs
     * </p>
     * @return the list of current NPCs
     */
    public List<NPC> getNPCs() {
        return npcs.values().stream().toList();
    }

    /**
     * <p> Adds an NPC to the list of current NPCs
     * </p>
     * @param npc The NPC to add
     * @param hologram the TextDisplay representing the NPC's name
     */
    public void addNPC(NPC npc, TextDisplay hologram) {
        holograms.add(hologram);
        npcs.put(npc.getUUID(), npc);
    }

    /**
     * <p> Gets the FileManager
     * </p>
     * @return the file manager object
     */
    public FileManager getFileManager() {
        return fileManager;
    }

    /**
     * <p> Gets the page the player is in.
     * </p>
     * @param p The player to get the page of
     * @return the current page in the Skin browser the player is in
     */
    public int getPage(Player p) {
        return pages.get(p);
    }

    /**
     * <p> Sets the page the player is in. Does not actually set the player's open inventory.
     * </p>
     * @param p The player to ser the page of
     * @param page The page number to set.
     */
    public void setPage(Player p, int page) {
        pages.put(p, page);
    }

    /**
     * <p> Gets the delay of an action
     * </p>
     * @param uuid The UUID of the npc
     * @return the NPC of the specified UUID
     * @throws NullPointerException if the specified UUID is null
     * @throws IllegalArgumentException if an NPC with the specified UUID does not exist
     */
    public NPC getNPCByID(UUID uuid) {
        if (uuid == null) throw new NullPointerException("uuid cannot be null");
        if (!npcs.containsKey(uuid)) throw new IllegalArgumentException("An NPC with the uuid '" + uuid + "' does not exist");
        return npcs.get(uuid);
    }

    /**
     * <p> Gets the MenuUtils object
     * </p>
     * @return the MenuUtils object
     */
    public MenuUtils getMenuUtils(){
        return mu;
    }

    /**
     * <p> Gets the Gson object
     * </p>
     * @return the Gson object
     */
    public static Gson getGson(){
            return gson;
        }

    /**
     * <p> Doesn't do anything since this plugin is not expecting to receive any plugin messages. It exists soley to be able to send a player to a bungeecord server.
     * </p>
     */
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {}


    // API stuffs
    /**
     * The class for external use to create an NPC
     */
    public static class NPCBuilder {

        /**
         * The NPC this builder is creating
         */
        private final NPC npc;

        /**
         * The intended way to create an NPC
         * @param world The world for the NPC to be create in
         * @author Foxikle
         *
         */
        public NPCBuilder(@NotNull World world){
            Preconditions.checkArgument(world != null, "world cannot be null.");
            GameProfile profile = new GameProfile(UUID.randomUUID(), ChatColor.RED + "ERROR!");
            MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
            ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
            this.npc = new NPC(instance, nmsServer, nmsWorld, profile, new Location(world, 0, 0, 0), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), false, true, "", profile.getId(), "",  "", null, 0, null,  new ArrayList<>());
        }

        /**
         * <p>Sets the display name of the NPC
         * </p>
         * @param name the item to be used
         * @return the NPCBuilder with the modified name
         * @since 1.3-pre5
         */
        public NPCBuilder setName(@NotNull String name){
            Preconditions.checkArgument(name != null, "name cannot be null.");
            npc.setName(name);
            return this;
        }

        /**
         * <p>Sets the location of the NPC
         * </p>
         * @param loc the new location for the NPC
         * @return the NPCBuilder with the modified location
         * @since 1.3-pre5
         */
        public NPCBuilder setPostion(@NotNull Location loc){
            Preconditions.checkArgument(loc != null, "loc cannot be null.");
            npc.setSpawnLoc(loc);
            return this;
        }

        /**
         * <p>Sets the leggings the NPC is wearing
         * </p>
         * @param skinName the name to reference the skin by.
         * @param signature the encoded signature of the skin.
         * @param value the encoded value of the skin
         * @return the NPCBuilder with the modified skin
         * @see <a href="ttps://mineskin.org/">Use this site by InventiveTalent to find value and signature</a>
         * @since 1.3-pre5
         */
        public NPCBuilder setSkin(@NotNull String skinName, @NotNull String signature, @NotNull String value){
            Preconditions.checkArgument(signature != null && skinName.length() != 0, "signature cannot be null or empty.");
            Preconditions.checkArgument(value != null && skinName.length() != 0, "value cannot be null or empty.");
            Preconditions.checkArgument(skinName != null && skinName.length() != 0, "skinName cannot be null or empty");
            GameProfile profile = npc.getGameProfile();
            npc.setSkinName(skinName);
            npc.setValue(value);
            npc.setSignature(signature);
            profile.getProperties().removeAll("textures");
            profile.getProperties().put("textures", new Property("textures", value, signature));
            return this;
        }

        /**
         * <p>Sets the helmet the NPC is wearing
         * </p>
                * @param item the item to be used
         * @return the NPCBuilder with the modified helmet
         * @since 1.3-pre5
         */
        public NPCBuilder setHelmet(ItemStack item){
            Preconditions.checkArgument(item != null, "item cannot be null.");
            npc.setHeadItem(item);
            return this;
        }

        /**
         * <p>Sets the chestplate the NPC is wearing
         * </p>
         * @param item the item to be used
         * @return the NPCBuilder with the modified chestplate
         * @since 1.3-pre5
         */
        public NPCBuilder setChestplate(ItemStack item){
            Preconditions.checkArgument(item != null, "item cannot be null.");
            npc.setChestItem(item);
            return this;
        }

        /**
         * <p>Sets the leggings the NPC is wearing
         * </p>
         * @param item the item to be used
         * @return the NPCBuilder with the modified pair leggings
         * @since 1.3-pre5
         */
        public NPCBuilder setLeggings(ItemStack item){
            Preconditions.checkArgument(item != null, "item cannot be null.");
            npc.setLegsItem(item);
            return this;
        }

        /**
         * <p>Sets the boots the NPC is wearing
         * </p>
         * @param item the item to be used
         * @return the NPCBuilder with the modified pair of boots
         * @since 1.3-pre5
         */
        public NPCBuilder setBoots(ItemStack item){
            Preconditions.checkArgument(item != null, "item cannot be null.");
            npc.setBootsItem(item);
            return this;
        }

        /**
         * <p>Sets the item in the NPC's hand
         * </p>
         * @param item the item to be used
         * @return the NPCBuilder with the modified hand item
         * @since 1.3-pre5
         */
        public NPCBuilder setHandItem(ItemStack item){
            Preconditions.checkArgument(item != null, "item cannot be null.");
            npc.setHandItem(item);
            return this;
        }

        /**
         * <p>Sets the item in the NPC's offhand
         * </p>
         * @param item the item to be used
         * @return the NPCBuilder with the modified offhand item
         * @since 1.3-pre5
         */
        public NPCBuilder setOffhandItem(ItemStack item){
            Preconditions.checkArgument(item != null, "item cannot be null.");
            npc.setOffhandItem(item);
            return this;
        }

        /**
         * <p>Sets the NPC's interactability
         * </p>
         * @param interactable should the NPC be interactable?
         * @return the NPCBuilder with the modified interacability
         * @since 1.3-pre5
         */
        public NPCBuilder setInteractable(boolean interactable){
            npc.setClickable(interactable);
            return this;
        }

        /**
         * <p>Sets the NPC's persistence (staying on server restart/reload).
         * </p>
         * @param resilient should the NPC persist on reloads/server restarts?
         * @return the NPCBuilder with the modified resiliency
         * @since 1.3-pre5
         */
        public NPCBuilder setResilient(boolean resilient){
            npc.setResilient(resilient);
            return this;
        }

        /**
         * <p>Sets the NPC's facing direction
         * </p>
         * @param heading the heading the NPC should face.
         * @return the NPCBuilder with the modified heading
         * @since 1.3-pre5
         */
        public NPCBuilder setHeading(double heading){
            npc.setDirection(heading);
            return this;
        }

        /**
         * <p>Sets the NPC's actions to the specified Collection
         * </p>
         * @param actions the colection of actions
         * @return the NPCBuilder with the modified set of actions
         * @see Action
         * @since 1.3-pre5
         */
        public NPCBuilder setActions(Collection<Action> actions){
            npc.setActions(actions);
            return this;
        }

        /**
         * Move the npc to the specified location. Takes into account pitch and yaw
         * @param location the location to move to
         */
        public void moveTo(Location location){
            npc.moveTo(location.x(), location.y(), location.z(), location.getYaw(), location.getPitch());
        }

        /**
         * Swings the NPC's arm
         * @since 1.4
         */
        public void swingArm(){
            npc.swing(InteractionHand.MAIN_HAND);
        }

        /**
         * Injects the npc into the player's connection. This should be handled by the plugin, but this is here for more control.
         * @param player the player to inject
         * @since 1.4
         * @see Player
         */
        public void injectPlayer(Player player) {
            npc.injectPlayer(player);
        }

        /**
         * Points the NPC's head in the direction of an entity
         * @param e The entity to look at
         * @param atHead If the npc should look head (true), or feet (false)
         */
        public void lookAt(Entity e, boolean atHead){
            npc.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftEntity) e).getHandle(), atHead ? EntityAnchorArgument.Anchor.EYES : EntityAnchorArgument.Anchor.FEET);
        }

        /**
         * Points the NPC's head at a location
         * @param location the location to look at
         */
        public void lookAt(Location location) {
            npc.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(location.x(), location.y(), location.z()));
        }

        /**
         * <p>Creates the NPC.
         * </p>
         * @since 1.3-pre5
         */
        public void create(){
            npc.createNPC();
        }

        /**
         * <p>Permanantly deletes an NPC. They will NOT reappear on the next reload or server restart.
         * </p>
         * @since 1.3-pre5
         */
        public void delete() {
            npc.delete();
        }

        /**
         * <p>Temporarily removes an NPC. They will reappear on the next reload or server restart.
         * </p>
         * @since 1.3-pre5
         */
        public void remove() {
            npc.remove();
        }
    }
}
