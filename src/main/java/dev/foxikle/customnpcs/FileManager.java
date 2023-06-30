package dev.foxikle.customnpcs;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The class that deals with all file related things
 */
public class FileManager {

    private final CustomNPCs plugin;

    /**
     * <p> Gets the file manager object.
     * </p>
     * @param plugin The instance of the Main class
     */
    public FileManager (CustomNPCs plugin){
        this.plugin = plugin;
    }

    /**
     * <p> Creates the files the plugin needs to run
     * </p>
     */
    public void createFiles(){
        if (!new File("plugins/CustomNPCs/npcs.yml").exists()) {
            plugin.saveResource("npcs.yml", false);
        } else if (!new File("plugins/CustomNPCs/config.yml").exists()) {
            plugin.saveResource("config.yml", false);
        }
    }

    /**
     * <p> Adds an NPC to the `npcs.yml` file.
     * </p>
     * @param npc The NPC to store
     */
    public void addNPC(NPC npc){
        File file = new File("plugins/CustomNPCs/npcs.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        yml.createSection(npc.getUUID().toString());
        ConfigurationSection section = yml.getConfigurationSection(npc.getUUID().toString());

        List<String> actions = new ArrayList<>();
        npc.getActions().forEach(action -> actions.add(action.serialize()));

        section.addDefault("value", npc.getValue());
        section.addDefault("signature", npc.getSignature());
        section.addDefault("skin", npc.getSkinName());
        section.addDefault("clickable", npc.isClickable());
        section.addDefault("location", npc.getSpawnLoc());
        section.addDefault("actions", actions);
        section.addDefault("handItem", npc.getHandItem());
        section.addDefault("offhandItem", npc.getItemInOffhand());
        section.addDefault("headItem", npc.getHeadItem());
        section.addDefault("chestItem", npc.getChestItem());
        section.addDefault("legsItem", npc.getLegsItem());
        section.addDefault("feetItem", npc.getBootsItem());
        section.addDefault("name", npc.getHologramName());
        section.addDefault("world", npc.getWorld().getName());
        section.addDefault("direction", npc.getFacingDirection());
        yml.options().copyDefaults(true);
        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("An error occoured saving the npcs.yml file after creating a new section. Please report the following stacktrace to Foxikle.");
            e.printStackTrace();
        }
    }

    /**
     * <p> Gets the NPC of the specified UUID
     * </p>
     * @param uuid The NPC to load from the file
     */
    public void loadNPC(UUID uuid){
        File file = new File("plugins/CustomNPCs/npcs.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yml.getConfigurationSection(uuid.toString());
        if(section == null) throw new IllegalArgumentException("NPC uuid cannot be null.");
        List<Action> actions = new ArrayList<>();
        if(yml.getString("version") == null) { // Config is from before 1.3-pre4
            yml.set("version", "1.3");
            // save updating the version
            try {
                yml.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("An error occoured saving the npcs.yml file after updating version number. Please report the following stacktrace to Foxikle.");
                e.printStackTrace();
            }
            plugin.getLogger().info("Adding delay to old actions.");
            for (UUID u : getNPCIds()) {
                ConfigurationSection s = yml.getConfigurationSection(u.toString());
                List<String> strings = s.getStringList("actions");
                List<String> convertedActions = new ArrayList<>();
                for (String string : strings) {
                    ArrayList<String> split = new ArrayList<>(Arrays.stream(string.split("%::%")).toList());
                    String sub = split.get(0);
                    split.remove(0);
                    int delay = 0;
                    Action acttion = new Action(ActionType.valueOf(sub), split, delay);
                    convertedActions.add(acttion.serialize());
                    actions.add(acttion);
                }
                s.set("actions", convertedActions);
            }
            // save after adding actions
            try {
                yml.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("An error occoured saving the npcs.yml file after saving a list of converted actions. Please report the following stacktrace to Foxikle.");
                e.printStackTrace();
            }
        }
        if(section.getConfigurationSection("actions") == null) { // meaning it does not exist
            if (section.getString("command") != null) { // if there is a legacy command
                Bukkit.getLogger().info("Converting legacy commands to Actions.");
                String command = section.getString("command");
                Action action = new Action(ActionType.RUN_COMMAND, new ArrayList<>(Arrays.stream(command.split(" ")).toList()), 0);
                actions.add(action);
                section.set("actions", actions);
                section.set("command", null);
                try {
                    yml.save(file);
                } catch (IOException e) {
                    plugin.getLogger().severe("An error occoured saving the npcs.yml file after converting legacy commands to actions. Please report the following stacktrace to Foxikle.");
                    e.printStackTrace();
                }
            }
        }




        GameProfile profile = new GameProfile(uuid, section.getBoolean("clickable") ? "§e§lClick" : "nothing");
        profile.getProperties().removeAll("textures");
        profile.getProperties().put("textures", new Property("textures", section.getString("value"), section.getString("signature")));
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel nmsWorld = ((CraftWorld) section.getLocation("location").getWorld()).getHandle();
        NPC npc = new NPC(plugin, nmsServer, nmsWorld, profile, section.getLocation("location"), section.getItemStack("handItem"), section.getItemStack("offhandItem"), section.getItemStack("headItem"), section.getItemStack("chestItem"), section.getItemStack("legsItem"), section.getItemStack("feetItem"), section.getBoolean("clickable"), true, section.getString("name"), uuid, section.getString("value"), section.getString("signature"), section.getString("skin"), section.getDouble("direction"), null,  section.getStringList("actions"));
        npc.createNPC();
    }

    /**
     * <p> Gets the set of stored UUIDs.
     * </p>
     * @return the set of stored NPC uuids.
     */
    public Set<UUID> getNPCIds(){
        File file = new File("plugins/CustomNPCs/npcs.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        Set<UUID> uuids = new HashSet<>();
        for (String str: yml.getKeys(false)) {
            if(!str.equalsIgnoreCase("version"))
                uuids.add(UUID.fromString(str));
        }
        return uuids;
    }

    /**
     * <p> Removes the specified NPC from storage
     * </p>
     * @param uuid The NPC uuid to remove
     */
    public void remove(UUID uuid){
        File file = new File("plugins/CustomNPCs/npcs.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        yml.set(uuid.toString(), null);
        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("An error occoured saving the npcs.yml file after removing an npc. Please report the following stacktrace to Foxikle.");
            e.printStackTrace();
        }
    }
}
