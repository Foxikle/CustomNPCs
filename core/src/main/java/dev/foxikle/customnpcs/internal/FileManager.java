/*
 * Copyright (c) 2024. Foxikle
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

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.actions.ActionType;
import dev.foxikle.customnpcs.actions.LegacyAction;
import dev.foxikle.customnpcs.actions.conditions.Condition;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.data.Settings;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.utils.Utils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

/**
 * The class that deals with all file related things
 */
@SuppressWarnings("unused")
public class FileManager {

    /**
     * The config file version
     */
    public static final int CONFIG_FILE_VERSION = 5;

    /**
     * The file version of the npcs.yml file
     */
    public static final double NPC_FILE_VERSION = 1.6;

    public static File PARENT_DIRECTORY = new File("plugins/CustomNPCs/");

    private final CustomNPCs plugin;

    /**
     * <p> Gets the file manager object.
     * </p>
     *
     * @param plugin The instance of the Main class
     */
    public FileManager(CustomNPCs plugin) {
        this.plugin = plugin;
    }

    /**
     * <p> Creates the files the plugin needs to run
     * </p>
     *
     * @return if creating the files was successful
     */
    public boolean createFiles() {
        if (!new File(PARENT_DIRECTORY, "/npcs.yml").exists()) {
            plugin.saveResource("npcs.yml", false);
        }
        if (!new File(PARENT_DIRECTORY, "config.yml").exists()) {
            plugin.saveResource("config.yml", false);
            return true;
        }
        // config
        {
            File file = new File(PARENT_DIRECTORY, "config.yml");
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

            if (!yml.contains("Skins")) {
                BackupResult br = createBackup(file);
                if (br.success) {
                    plugin.getLogger().warning("The config is irreparably damaged! Resetting config. Your old config was saved to the file \"" + br.filePath.toString() + "\"");
                    plugin.saveResource("config.yml", true);
                }
            }
            int version = yml.getInt("CONFIG_VERSION");
            if (version == 0) { // doesn't exist?
                plugin.getLogger().log(Level.WARNING, String.format("Outdated Config version! Converting config (%d -> %d).", version, 1));
                yml.set("CONFIG_VERSION", 1);
                yml.setComments("CONFIG_VERSION", List.of(" DO NOT, under ANY circumstances modify the 'CONFIG_VERSION' field. Doing so can cause catastrophic data loss.", ""));
                yml.set("ClickText", "&e&lCLICK");
                yml.setComments("ClickText", List.of("ClickText -> The hologram displayed above the NPC if it is interactable", " NOTE: Due to Minecraft limitations, this cannot be more than 16 characters INCLUDING color and format codes.", " (But not the &)", ""));
                yml.set("DisplayClickText", true);
                yml.setComments("DisplayClickText", List.of(" DisplayClickText -> Should the plugin display a hologram above the NPC's head if it is interactable?", ""));
                try {
                    yml.save(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (version < 2) { // prior to 1.4-pre2
                plugin.getLogger().log(Level.WARNING, String.format("Outdated Config version! Converting config (%d -> %d).", version, 2));
                yml.set("CONFIG_VERSION", 2);
                yml.set("AlertOnUpdate", true);
                try {
                    yml.save(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (version < 3) { // prior to 1.5.2-pre1
                plugin.getLogger().log(Level.WARNING, String.format("Outdated Config version! Converting config (%d -> %d).", version, 3));
                yml.set("CONFIG_VERSION", 3);
                yml.set("ClickText", plugin.getMiniMessage().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(Objects.requireNonNull(yml.getString("ClickText")))));
                try {
                    yml.save(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (version < 4) { //prior to 1.6-pre2
                plugin.getLogger().log(Level.WARNING, String.format("Outdated Config version! Converting config (%d -> %d).", version, 4));
                yml.set("CONFIG_VERSION", 4);
                yml.set("DisableCollisions", true);
                try {
                    yml.save(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (version < 5) {
                plugin.getLogger().log(Level.WARNING, String.format("Outdated Config version! Converting config (%d -> %d).", version, 5));
                yml.set("CONFIG_VERSION", 5);
                yml.set("NameReferenceMessages", true);
                try {
                    yml.save(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (version < 6) {
                plugin.getLogger().log(Level.WARNING, String.format("Outdated Config version! Converting config (%d -> %d).", version, 6));
                yml.set("CONFIG_VERSION", 6);
                yml.set("InjectionDistance", 48);
                yml.set("InjectionInterval", 10);
                yml.set("HologramUpdateInterval", 200);
                yml.set("LookInterval", 5);
                try {
                    yml.save(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // npcs
        {
            File file = new File(PARENT_DIRECTORY, "npcs.yml");
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            String version = yml.getString("version");


            if (version == null) { // Config is from before 1.3-pre4
                plugin.getLogger().warning("Old NPC file version found! Bumping version! (unkown version -> 1.3)");
                BackupResult br = createBackup(file);
                if (!br.success) {
                    plugin.getLogger().warning("Could not create backup before updating npcs.yml!");
                    return false;
                }
                yml.set("version", "1.3");

                plugin.getLogger().warning("Adding delay to old actions.");
                for (UUID u : getNPCIds()) {
                    ConfigurationSection s = yml.getConfigurationSection(u.toString());
                    assert s != null;
                    List<String> strings = s.getStringList("actions");
                    List<String> convertedActions = new ArrayList<>();
                    for (String string : strings) {
                        List<String> split = Utils.list(string.split("%::%"));
                        String sub = split.get(0);
                        split.remove(0);
                        int delay = 0;
                        LegacyAction actionImpl = new LegacyAction(ActionType.valueOf(sub), split, delay, Condition.SelectionMode.ONE, new ArrayList<>());
                        convertedActions.add(actionImpl.toJson());
                    }
                    s.set("actions", convertedActions);
                }
                // save after adding actions
                try {
                    yml.save(file);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "An error occurred saving the npcs.yml file after saving a list of converted actions. Please report the following stacktrace to Foxikle.", e);
                }
            }

            if (version.equalsIgnoreCase("1.3")) {
                plugin.getLogger().warning("Old NPC file version found! Bumping version! (1.3-> 1.4)");
                BackupResult br = createBackup(file);
                if (!br.success) {
                    plugin.getLogger().warning("Could not create backup before updating npcs.yml!");
                    return false;
                }
                yml.set("version", "1.4");

                Set<String> npcs = yml.getKeys(false);
                for (String npc : npcs) {
                    if (npc.equals("version")) continue; // it's a key
                    ConfigurationSection section = yml.getConfigurationSection(npc);

                    plugin.getLogger().warning("Old Actions found. Converting to json.");
                    List<String> legacyActions = section.getStringList("actions");
                    List<String> newActions = new ArrayList<>();
                    legacyActions.forEach(s -> {
                        if (s != null) {
                            LegacyAction a = LegacyAction.of(s); // going to be converted the old way
                            if (a != null) {
                                newActions.add(a.toJson());
                            }
                        }
                    });
                    section.set("actions", newActions);
                    try {
                        yml.save(file);
                    } catch (IOException e) {
                        plugin.getLogger().severe("An error occurred whilst saving the converted actions. Please report the following stacktrace to Foxikle. \n" + Arrays.toString(e.getStackTrace()));
                    }
                }
            }

            if (version.equalsIgnoreCase("1.4")) {
                plugin.getLogger().warning("Old NPC file version found! Bumping version! (1.4 -> 1.5)");
                BackupResult br = createBackup(file);
                if (!br.success) {
                    plugin.getLogger().warning("Could not create backup before updating npcs.yml!");
                    return false;
                }

                yml.set("version", "1.5");

                Set<String> npcs = yml.getKeys(false);
                for (String npc : npcs) {
                    if (npc.equals("version")) continue; // it's a key
                    ConfigurationSection section = yml.getConfigurationSection(npc);

                    section.set("tunnelvision", false);
                    try {
                        yml.save(file);
                    } catch (IOException e) {
                        plugin.getLogger().severe("An error occurred whilst saving the tunelvision status to the config. Please report the following stacktrace to Foxikle. \n" + Arrays.toString(e.getStackTrace()));
                    }
                }
            }

            if (version.equalsIgnoreCase("1.5")) {
                plugin.getLogger().warning("Old NPC file version found! Bumping version! (1.5-> 1.6)");
                BackupResult br = createBackup(file);
                if (!br.success) {
                    plugin.getLogger().warning("Could not create backup before updating npcs.yml!");
                    return false;
                }

                yml.set("version", "1.6");
                Set<String> npcs = yml.getKeys(false);
                for (String npc : npcs) {
                    if (npc.equals("version")) continue; // it's a key
                    ConfigurationSection section = yml.getConfigurationSection(npc);

                    section.set("customHologram", false);
                    section.set("hideInteractableHologram", "");
                    try {
                        yml.save(file);
                    } catch (IOException e) {
                        plugin.getLogger().severe("An error occurred whilst saving the tunelvision status to the config. Please report the following stacktrace to Foxikle. \n" + Arrays.toString(e.getStackTrace()));
                    }
                }
            }


            if (version.equals("1.6")) {
                plugin.getLogger().warning("Old NPC file version found! Bumping version! (1.6 -> 1.7)");
                BackupResult br = createBackup(file);
                if (!br.success) {
                    plugin.getLogger().warning("Could not create backup before updating npcs.yml!");
                    return false;
                }
                yml.set("version", "1.7");

                Set<String> npcs = yml.getKeys(false);
                for (String npc : npcs) {
                    if (npc.equals("version")) continue; // its a key
                    ConfigurationSection section = yml.getConfigurationSection(npc);

                    // convert actions to new format
                    List<String> actionStrs = section.getStringList("actions");
                    List<Action> list = new ArrayList<>();
                    for (String actionStr : actionStrs) {
                        LegacyAction a = LegacyAction.of(actionStr);
                        if (a == null) {
                            plugin.getLogger().warning("Found an invalid action in the config. Please report the following action string to Foxikle. \n" + actionStr);
                            continue;
                        }
                        if (a.getActionType() == ActionType.TOGGLE_FOLLOWING) {
                            plugin.getLogger().warning("Found an action of the type `TOGGLE_FOLLOWING`. This action has been removed in 1.7.");
                            continue;
                        }

                        list.add(a.toAction());
                    }

                    List<String> newActions = new ArrayList<>();

                    for (Action a : list) {
                        if (a == null) {
                            plugin.getLogger().warning("Found an invalid action in the config.");
                            continue;
                        }
                        newActions.add(a.serialize());
                    }

                    section.set("actions", newActions);
                }


                try {
                    yml.save(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return true;
    }

    /**
     * <p> Adds an NPC to the `npcs.yml` file.
     * </p>
     *
     * @param npc The NPC to store
     */
    public void addNPC(InternalNpc npc) {
        File file = new File("plugins/CustomNPCs/npcs.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        yml.createSection(npc.getUniqueID().toString());
        ConfigurationSection section = yml.getConfigurationSection(npc.getUniqueID().toString());

        List<String> actions = new ArrayList<>();
        npc.getActions().forEach(action -> actions.add(action.serialize()));

        assert section != null;
        section.addDefault("value", npc.getSettings().getValue());
        section.addDefault("signature", npc.getSettings().getSignature());
        section.addDefault("skin", npc.getSettings().getSkinName());
        section.addDefault("clickable", npc.getSettings().isInteractable());
        section.addDefault("customHologram", npc.getSettings().getCustomInteractableHologram());
        section.addDefault("hideInteractableHologram", npc.getSettings().isHideClickableHologram());
        section.addDefault("location", npc.getSpawnLoc());
        section.addDefault("actions", actions);
        section.addDefault("handItem", npc.getEquipment().getHand());
        section.addDefault("offhandItem", npc.getEquipment().getOffhand());
        section.addDefault("headItem", npc.getEquipment().getHead());
        section.addDefault("chestItem", npc.getEquipment().getChest());
        section.addDefault("legsItem", npc.getEquipment().getLegs());
        section.addDefault("feetItem", npc.getEquipment().getBoots());
        section.addDefault("name", npc.getSettings().getName());
        section.addDefault("world", npc.getWorld().getName());
        section.addDefault("direction", npc.getSettings().getDirection());
        section.addDefault("tunnelvision", npc.getSettings().isTunnelvision());
        yml.options().copyDefaults(true);
        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred saving the npcs.yml file after creating a new section. Please report the following stacktrace to Foxikle.", e);
        }
    }

    /**
     * <p> Gets the NPC of the specified UUID
     * </p>
     *
     * @param uuid The NPC to load from the file
     */
    public void loadNPC(UUID uuid) {
        File file = new File("plugins/CustomNPCs/npcs.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yml.getConfigurationSection(uuid.toString());
        if (section == null) throw new IllegalArgumentException("NPC uuid cannot be null.");
        List<LegacyAction> actionImpls = new ArrayList<>();
        List<Action> actions = new ArrayList<>();

        if (section.getConfigurationSection("actions") == null) { // meaning it does not exist
            if (section.getString("command") != null) { // if there is a legacy command
                Bukkit.getLogger().info("Converting legacy commands to Actions.");
                String command = section.getString("command");
                assert command != null;
                LegacyAction actionImpl = new LegacyAction(ActionType.RUN_COMMAND, Utils.list(command.split(" ")), 0, Condition.SelectionMode.ONE, new ArrayList<>());
                actionImpls.add(actionImpl);
                section.set("actions", actionImpls);
                section.set("command", null);
                try {
                    yml.save(file);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "An error occurred saving the npcs.yml file after converting legacy commands to actions. Please report the following stacktrace to Foxikle.", e);
                }
            }
        }
        if (Objects.requireNonNull(section.getString("name")).contains("§")) {
            section.set("name", plugin.getMiniMessage().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(Objects.requireNonNull(section.getString("name")).replace("§", "&"))));
            try {
                yml.save(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "An error occurred saving the npcs.yml file after converting legacy names to minimessage. Please report the following stacktrace to Foxikle.", e);
            }
        }

        World world;
        try {
            world = Bukkit.getWorld(Objects.requireNonNull(section.getString("world")));
        } catch (IllegalArgumentException ex) {
            printInvalidConfig();
            return;
        }
        Location location;
        try {
            location = section.getLocation("location");
        } catch (Exception ex) {
            printInvalidConfig();
            return;
        }

        if (world == null) {
            printInvalidConfig();
            return;
        }

        if (location == null) {
            printInvalidConfig();
            return;
        }

        // use the actions freshly converted

        actions = new ArrayList<>();
        for (String s : section.getStringList("actions")) {
            actions.add(Action.parse(s));
        }


        InternalNpc npc = plugin.createNPC(
                world,
                location,
                new Equipment(
                        section.getItemStack("headItem"),
                        section.getItemStack("chestItem"),
                        section.getItemStack("legsItem"),
                        section.getItemStack("feetItem"),
                        section.getItemStack("handItem"),
                        section.getItemStack("offhandItem")
                ), new Settings(
                        section.getBoolean("clickable"),
                        section.getBoolean("tunnelvision"),
                        true,
                        section.getDouble("direction"),
                        section.getString("value"),
                        section.getString("signature"),
                        section.getString("skin"),
                        section.getString("name"),
                        section.getString("customHologram"),
                        section.getBoolean("hideInteractableHologram")
                ), uuid, null, actions);
        if (npc != null) {
            npc.createNPC();
        } else {
            plugin.getLogger().severe("The NPC '{name}' could not be created!".replace("{name}", Objects.requireNonNull(section.getString("name"))));
        }
    }

    /**
     * <p> Gets the set of stored UUIDs.
     * </p>
     *
     * @return the set of stored NPC uuids.
     */
    public Set<UUID> getNPCIds() {
        File file = new File("plugins/CustomNPCs/npcs.yml");
        YamlConfiguration yml;
        try {
            yml = YamlConfiguration.loadConfiguration(file);
        } catch (Exception ex) {
            printInvalidConfig();
            return new HashSet<>();
        }
        Set<UUID> uuids = new HashSet<>();
        for (String str : yml.getKeys(false)) {
            if (!str.equalsIgnoreCase("version"))
                uuids.add(UUID.fromString(str));
        }
        return uuids;
    }

    /**
     * <p> Removes the specified NPC from storage
     * </p>
     *
     * @param uuid The NPC uuid to remove
     */
    public void remove(UUID uuid) {
        File file = new File("plugins/CustomNPCs/npcs.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        yml.set(uuid.toString(), null);
        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred saving the npcs.yml file after removing an npc. Please report the following stacktrace to Foxikle.", e);
        }
    }

    private BackupResult createBackup(File file) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        File f = new File(PARENT_DIRECTORY, new Date().toString().replace(" ", "_").replace(":", "_") + "_backup_of_" + file.getName());
        try {
            if (f.createNewFile()) {
                yml.save(f);
            } else {
                throw new RuntimeException("A duplicate file of file '" + f.getName() + "' exists! This means the plugin attempted to back up the file '" + file.getName() + "' multiple times within this millisecond! This is a serious issue that should be reported to @foxikle on discord!");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred whilst creating a backup of the file '" + file.getName() + "'", e);
            return new BackupResult(null, false);
        }
        return new BackupResult(f.toPath(), true);
    }

    private void printInvalidConfig() {
        plugin.getLogger().severe("");
        plugin.getLogger().severe("+------------------------------------------------------------------------------+");
        plugin.getLogger().severe("|                 NPC with an invalid configuration detected!                  |");
        plugin.getLogger().severe("|                 ** THIS IS NOT AN ERROR WITH CUSTOMNPCS **                   |");
        plugin.getLogger().severe("|         This is most likely a configuration error as a result of             |");
        plugin.getLogger().severe("|                       modifying the `npcs.yml` file.                         |");
        plugin.getLogger().severe("+------------------------------------------------------------------------------+");
        plugin.getLogger().severe("");
    }

    private record BackupResult(Path filePath, boolean success) {
    }
}
