package dev.foxikle.customnpcs;

import dev.foxikle.customnpcs.commands.CommandCore;
import dev.foxikle.customnpcs.commands.NPCActionCommand;
import dev.foxikle.customnpcs.listeners.Listeners;
import dev.foxikle.customnpcs.listeners.NPCMenuListeners;
import dev.foxikle.customnpcs.menu.MenuCore;
import dev.foxikle.customnpcs.menu.MenuUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.Inventory;
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

    private MenuUtils mu;
    private String sversion;

    @Override
    public void onEnable() {

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
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {}
}
