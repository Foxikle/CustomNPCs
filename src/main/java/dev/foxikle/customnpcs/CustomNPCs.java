package dev.foxikle.customnpcs;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.foxikle.customnpcs.commands.CommandCore;
import dev.foxikle.customnpcs.commands.NPCActionCommand;
import dev.foxikle.customnpcs.listeners.Listeners;
import dev.foxikle.customnpcs.listeners.NPCMenuListeners;
import dev.foxikle.customnpcs.menu.MenuCore;
import dev.foxikle.customnpcs.menu.MenuUtils;
import io.netty.util.internal.UnstableApi;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class CustomNPCs extends JavaPlugin implements @NotNull PluginMessageListener {

    public List<Inventory> invs;

    public List<Player> titleWaiting = new ArrayList<>();
    public List<Player> serverWaiting = new ArrayList<>();
    public List<Player> actionbarWaiting = new ArrayList<>();
    public List<Player> messageWaiting = new ArrayList<>();
    public List<Player> commandWaiting = new ArrayList<>();
    public List<Player> nameWaiting = new ArrayList<>();
    public List<Player> soundWaiting = new ArrayList<>();
    public List<TextDisplay> holograms = new ArrayList<>();
    public FileManager fileManager;
    public Map<Player, Integer> pages = new HashMap<>();
    public Map<UUID, NPC> npcs = new HashMap<>();
    public Map<Player, MenuCore> menuCores = new HashMap<>();
    public Map<Player, Action> editingActions = new HashMap<>();
    public Map<Player, String> originalEditingActions = new HashMap<>();

    private static CustomNPCs instance;

    private MenuUtils mu;
    private String sversion;

    @Override
    public void onEnable() {
        instance = this;
        if(!setup()){
            Bukkit.getLogger().severe("Incompatible server version! Please use 1.19.4. Shutting down plugin.");
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

        getCommand("npc").setExecutor(new CommandCore(this));
        getCommand("npcaction").setExecutor(new NPCActionCommand(this));
        this.fileManager = new FileManager(this);
        this.mu = new MenuUtils(this);
        fileManager.createFiles();
        Bukkit.getLogger().info("Loading NPCs!");
        for (UUID uuid : fileManager.getNPCIds()) {
            fileManager.loadNPC(uuid);
        }
        Bukkit.getScheduler().runTaskLater(this, () -> Bukkit.getOnlinePlayers().forEach(player -> npcs.values().forEach(npc -> Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> npc.injectPlayer(player), 5))), 20);
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> invs = this.getMenuUtils().getCatalogueInventories(), 20);
        // setup bstats
        Metrics metrics = new Metrics(this, 18898);

        // setup service manager for the API
        Bukkit.getServer().getServicesManager().register(CustomNPCs.class, this, this, ServicePriority.Normal);
    }

    public boolean setup(){
        sversion = "N/A";
        try{
            sversion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException ex){
            return false;
        }
        return (sversion.equals("v1_20_R1") || sversion.equals("v1_20_1_R1"));
    }

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

    public List<NPC> getNPCs() {
        return npcs.values().stream().toList();
    }

    public void addNPC(NPC npc, TextDisplay hologram) {
        holograms.add(hologram);
        npcs.put(npc.getUUID(), npc);
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public int getPage(Player p) {
        return pages.get(p);
    }

    public void setPage(Player p, int page) {
        pages.put(p, page);
    }

    public NPC getNPCByID(UUID uuid) {
        if (uuid == null) throw new NullPointerException("uuid cannot be null");
        if (!npcs.containsKey(uuid)) throw new IllegalArgumentException("An NPC with the uuid '" + uuid + "' does not exist");
        return npcs.get(uuid);
    }
    public MenuUtils getMenuUtils(){
        return mu;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {}


    // API stuffs
    public static class Builder {
        private final NPC npc;
        public Builder(@NotNull World world){
            Preconditions.checkArgument(world != null, "world cannot be null.");
            GameProfile profile = new GameProfile(UUID.randomUUID(), ChatColor.RED + "ERROR!");
            MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
            ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
            this.npc = new NPC(instance, nmsServer, nmsWorld, profile, new Location(world, 0, 0, 0), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), false, true, "", profile.getId(), "",  "", null, 0, null,  new ArrayList<>());
        }

        public Builder setName(@NotNull String name){
            Preconditions.checkArgument(name != null, "name cannot be null.");
            npc.setName(name);
            return this;
        }

        public Builder setPostion(@NotNull Location loc){
            Preconditions.checkArgument(loc != null, "loc cannot be null.");
            npc.setSpawnLoc(loc);
            return this;
        }

        public Builder setSkin(@NotNull String skinName, @NotNull String signature, @NotNull String value){
            Preconditions.checkArgument(signature != null && skinName.length() != 0, "signature cannot be null or empty.");
            Preconditions.checkArgument(value != null && skinName.length() != 0, "value cannot be null or empty.");
            Preconditions.checkArgument(skinName != null && skinName.length() != 0, "skinName cannot be null or empty");
            FileConfiguration config = instance.getConfig();
            ConfigurationSection section = config.getConfigurationSection("Skins");
            Set<String> keys = section.getKeys(false);
            if(keys.contains(skinName)){
                ConfigurationSection newSection = section.createSection(skinName + "=");
                newSection.set("value", value);
                newSection.set("signature", signature);
            }
            instance.saveConfig();
            GameProfile profile = npc.getGameProfile();
            npc.setSkinName(skinName);
            profile.getProperties().removeAll("textures");
            profile.getProperties().put("textures", new Property("textures", value, signature));
            return this;
        }
        // equipment setters
        public Builder setHelmet(ItemStack item){
            Preconditions.checkArgument(item != null, "item cannot be null.");
            npc.setHeadItem(item);
            return this;
        }

        public Builder setChestplate(ItemStack item){
            Preconditions.checkArgument(item != null, "item cannot be null.");
            npc.setChestItem(item);
            return this;
        }

        public Builder setLeggings(ItemStack item){
            Preconditions.checkArgument(item != null, "item cannot be null.");
            npc.setLegsItem(item);
            return this;
        }

        public Builder setBoots(ItemStack item){
            Preconditions.checkArgument(item != null, "item cannot be null.");
            npc.setBootsItem(item);
            return this;
        }

        public Builder setHandItem(ItemStack item){
            Preconditions.checkArgument(item != null, "item cannot be null.");
            npc.setHandItem(item);
            return this;
        }

        public Builder setOffhandItem(ItemStack item){
            Preconditions.checkArgument(item != null, "item cannot be null.");
            npc.setOffhandItem(item);
            return this;
        }

        public Builder setInteractable(boolean interactable){
            npc.setClickable(interactable);
            return this;
        }

        public Builder setResilient(boolean resilient){
            npc.setClickable(resilient);
            return this;
        }

        public Builder setHeading(double heading){
            npc.setDirection(heading);
            return this;
        }

        public Builder setActions(Collection<Action> actions){
            npc.setActions(actions);
            return this;
        }

        public void create(){
            npc.createNPC();
        }
    }
}
