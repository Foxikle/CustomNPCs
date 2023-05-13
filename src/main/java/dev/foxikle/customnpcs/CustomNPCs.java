package dev.foxikle.customnpcs;

import dev.foxikle.customnpcs.commands.CommandCore;
import dev.foxikle.customnpcs.listeners.Listeners;
import dev.foxikle.customnpcs.listeners.NPCMenuListeners;
import dev.foxikle.customnpcs.menu.MenuCore;
import dev.foxikle.customnpcs.menu.MenuUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import java.util.*;

public final class CustomNPCs extends JavaPlugin {

    public static CustomNPCs instance;
    public List<Inventory> invs;
    public List<Player> waiting = new ArrayList<>();
    public List<ArmorStand> armorStands = new ArrayList<>();
    public FileManager fileManager;
    public Map<Player, Integer> pages = new HashMap<>();
    public Map<UUID, NPC> npcs = new HashMap<>();
    public Map<Player, MenuCore> menus = new HashMap<>();

    public static CustomNPCs getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        try {
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("npc");
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            team.setPrefix(ChatColor.DARK_GRAY + "[NPC] ");
        } catch (IllegalArgumentException ignored) {
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("npc");
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            team.setPrefix(ChatColor.DARK_GRAY + "[NPC] ");
        }
        instance = this;
        this.getServer().getPluginManager().registerEvents(new NPCMenuListeners(), this);
        this.getServer().getPluginManager().registerEvents(new Listeners(), this);
        getCommand("npc").setExecutor(new CommandCore());
        this.fileManager = new FileManager();
        fileManager.createFiles();
        System.out.println("Loading npcs!");
        for (UUID uuid : fileManager.getNPCIds()) {
            fileManager.loadNPC(uuid);
        }
        Bukkit.getScheduler().runTaskLater(this, () -> Bukkit.getOnlinePlayers().forEach(player -> npcs.values().forEach(npc -> Bukkit.getScheduler().runTaskLaterAsynchronously(CustomNPCs.getInstance(), () -> npc.injectPlayer(player), 5))), 20);
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> invs = MenuUtils.getCatalogueInventories(), 20);

    }

    @Override
    public void onDisable() {
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

    public void addNPC(NPC npc, ArmorStand hologram) {
        armorStands.add(hologram);
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
        if (!npcs.containsKey(uuid)) throw new IllegalArgumentException("NPC does not exist");
        return npcs.get(uuid);
    }
}
