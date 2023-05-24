package dev.foxikle.customnpcs;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FileManager {

    public void createFiles(){
        if (!new File("plugins/CustomNPCs/npcs.yml").exists()) {
            CustomNPCs.getInstance().saveResource("npcs.yml", false);
        } else if (!new File("plugins/CustomNPCs/config.yml").exists()) {
            CustomNPCs.getInstance().saveResource("config.yml", false);
        }
    }

    public void addNPC(NPC npc){
        File file = new File("plugins/CustomNPCs/npcs.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        yml.createSection(npc.getUUID().toString());
        ConfigurationSection section = yml.getConfigurationSection(npc.getUUID().toString());
        section.addDefault("value", npc.getValue());
        section.addDefault("signature", npc.getSignature());
        section.addDefault("skin", npc.getSkinName());
        section.addDefault("clickable", npc.isClickable());
        section.addDefault("location", npc.getLocation());
        section.addDefault("command", npc.getCommand());
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
            e.printStackTrace();
        }
    }

    public void loadNPC(UUID uuid){
        File file = new File("plugins/CustomNPCs/npcs.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yml.getConfigurationSection(uuid.toString());

        GameProfile profile = new GameProfile(uuid, section.getBoolean("clickable") ? "§e§lClick" : "nothing");
        profile.getProperties().removeAll("textures");
        profile.getProperties().put("textures", new Property("textures", section.getString("value"), section.getString("signature")));
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel nmsWorld = ((CraftWorld) section.getLocation("location").getWorld()).getHandle();
        NPC npc = new NPC(nmsServer, nmsWorld, profile, section.getLocation("location"), section.getItemStack("handItem"), section.getItemStack("offhandItem"), section.getItemStack("headItem"), section.getItemStack("chestItem"), section.getItemStack("legsItem"), section.getItemStack("feetItem"), section.getBoolean("clickable"), true, section.getString("command"), section.getString("name"), uuid, section.getString("value"), section.getString("signature"), section.getString("skin"), section.getDouble("direction"));
        npc.createNPC();
    }

    public Set<UUID> getNPCIds(){
        File file = new File("plugins/CustomNPCs/npcs.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        Set<UUID> uuids = new HashSet<>();
        for (String str: yml.getKeys(false)) {
            uuids.add(UUID.fromString(str));
        }
        return uuids;
    }

    public void remove(UUID uuid){
        File file = new File("plugins/CustomNPCs/npcs.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        yml.set(uuid.toString(), null);
        try {
            yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
