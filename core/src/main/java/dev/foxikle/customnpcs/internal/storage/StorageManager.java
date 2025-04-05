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

package dev.foxikle.customnpcs.internal.storage;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.actions.ActionType;
import dev.foxikle.customnpcs.actions.LegacyAction;
import dev.foxikle.customnpcs.actions.conditions.Condition;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.data.Settings;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.proto.NpcOuterClass;
import dev.foxikle.customnpcs.internal.proto.ProtoWrapper;
import dev.foxikle.customnpcs.internal.utils.Utils;
import dev.foxikle.customnpcs.internal.utils.exceptions.IllegalWorldException;
import dev.foxikle.customnpcs.internal.utils.exceptions.UntrackedNpcException;
import lombok.Getter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * The class that deals with all file related things
 */
@SuppressWarnings("unused")
public class StorageManager {

    public static final String[] VALID_PROVIDERS = {"LOCAL", "MONGODB", "MYSQL"};

    /**
     * The config file version
     */
    public static final int CONFIG_FILE_VERSION = 8;
    /**
     * The file version of the npcs.yml file
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.10")
    public static final double NPC_FILE_VERSION = 1.6;
    public static File PARENT_DIRECTORY = new File("plugins/CustomNPCs/");
    @Getter
    private final Map<UUID, NpcOuterClass.Npc> brokenNPCs = new HashMap<>();
    @Getter
    private final List<UUID> validNPCs = new ArrayList<>();
    private final CustomNPCs plugin;
    private final List<NpcOuterClass.Npc> trackedNpcs = new ArrayList<>();

    private boolean justMigrated = false;
    @Getter
    private StorageProvider storage = null;

    /**
     * <p> Gets the file manager object.
     * </p>
     *
     * @param plugin The instance of the Main class
     */
    public StorageManager(CustomNPCs plugin) {
        this.plugin = plugin;
    }

    /**
     * <p> Creates the files the plugin needs to run
     * </p>
     *
     * @return if creating the files was successful
     */
    public boolean setup() {
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

            if (version < 7) {
                BackupResult br = createBackup(file);
                if (!br.success()) {
                    throw new RuntimeException("Failed to create a backup of the config file before updating it!");
                } else {
                    plugin.getLogger().info("Created backup of config.yml before updating it! A copy of your existing config was saved to " + br.filePath().toString());
                }
            }

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
            if (version < 7) {
                plugin.getLogger().log(Level.WARNING, String.format("Outdated Config version! Converting config (%d -> %d).", version, 7));
                yml.set("CONFIG_VERSION", 7);
                yml.set("DefaultInterpolationDuration", 5);
                yml.setComments("DefaultInterpolationDuration", List.of("DefaultInterpolationDuration -> How long should moving NPCs interpolate their Nametags moving?"));
                try {
                    yml.save(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (version < 8) {
                plugin.getLogger().log(Level.WARNING, String.format("Outdated Config version! Converting config (%d -> %d).", version, 8));

                yml.set("DebugMode", false);
                yml.setComments("DebugMode", List.of("DebugMode -> Should the plugin launch in debug mode? This can be quite spammy, so only use this if you're sure!"));

                yml.set("CONFIG_VERSION", 8);
                ConfigurationSection storage = yml.createSection("storage");
                yml.setComments("storage", Utils.list("",
                        "+---------------------------------------+",
                        "|           NPC Data Storage            |",
                        "+---------------------------------------+"
                ));

                storage.set("provider", "LOCAL");
                storage.setComments("provider", Utils.list("",
                        "+---------------------------------------+",
                        "|           Storage Provider            |",
                        "+---------------------------------------+",
                        "",
                        "The storage proivder determines how the plugin stores the NPC data. There are 3 options:",
                        "",
                        "\"LOCAL\" is used by default. It stores the data on the same disc the server is running on. It is a good choice if you don't want to deal with setting up a database or don't have one.",
                        "\"MYSQL\" is a typical relational database. It's not really optimized for this kind of storage, it's a good choice if you already use a MySQL or MariaDB database.",
                        "\"MONGODB\" is the recommended option for using remote storage. It tends to be more performant and optimized for storing BLOBs."
                ));

                ConfigurationSection mysql = storage.createSection("mysql");
                storage.setComments("mysql", Utils.list("",
                        "+---------------------------------------+",
                        "|         MySQL Configuration           |",
                        "+---------------------------------------+",
                        "These settings only matter if the provider is set to \"MYSQL\""
                ));

                mysql.set("hostname", "YOUR_HOST");
                mysql.setComments("hostname", Utils.list(
                        "hostname -> the host name, or ip address of your database server."
                ));

                mysql.set("port", 3306);
                mysql.setComments("port", Utils.list(
                        "port -> The port the database runs on. Don't change this unless you know what you're doing"
                ));

                mysql.set("username", "YOUR_USERNAME");
                mysql.setComments("username", Utils.list(
                        "username -> The database username"
                ));

                mysql.set("password", "YOUR_PASSWORD");
                mysql.setComments("password", Utils.list(
                        "password -> The database password"
                ));

                mysql.set("database", "YOUR_DATABASE");
                mysql.setComments("database", Utils.list(
                        "database -> The name of the database to use"
                ));

                mysql.set("table", "npcs");
                mysql.setComments("table", Utils.list(
                        "table -> The name of the table used to store the data in. This can be used to separate your npc configurations across servers. ie: lobby, survival, etc."
                ));

                ConfigurationSection mongo = storage.createSection("mongo");
                storage.setComments("mongo", Utils.list("",
                        "+---------------------------------------+",
                        "|        MongoDB Configuration          |",
                        "+---------------------------------------+",
                        "These settings only matter if the provider is set to \"MONGODB\""
                ));

                mongo.set("connectionString", "YOUR_CONNECTION_STRING");
                mongo.setComments("connectionString", Utils.list(
                        "connectionString -> The connection string provided by your mongo server."
                ));

                mongo.set("database", "YOUR_DATABASE");
                mongo.setComments("database", Utils.list(
                        "database -> The name of the database to use"
                ));

                mongo.set("document", "npcs");
                mongo.setComments("document", Utils.list(
                        "document -> The document to use to store the data. This can be used to separate your npc configurations across servers. ie: lobby, survival, etc."
                ));

                try {
                    yml.save(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            // enable debug mode as quickly as possible:
            plugin.setDebug(yml.getBoolean("DebugMode"));

            if (Arrays.stream(VALID_PROVIDERS).noneMatch(s -> s.equalsIgnoreCase(yml.getString("storage.provider")))) {
                throw new IllegalArgumentException("Invalid config provider " + yml.getString("storage.provider"));
            }
            plugin.getLogger().info("Successfully loaded " + yml.getString("storage.provider") + " storage provider.");
        }

        // npcs (we only care if it exists)
        if (new File(PARENT_DIRECTORY, "npcs.yml").exists()) {
            // make it colapsable
            File file = new File(PARENT_DIRECTORY, "npcs.yml");
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            {

                String version = yml.getString("version");

                if (version == null) { // Config is from before 1.3-pre4
                    plugin.getLogger().warning("Old NPC file version found! Bumping version! (unknown version -> 1.3)");
                    BackupResult br = createBackup(file);
                    if (!br.success) {
                        plugin.getLogger().warning("Could not create backup before updating npcs.yml!");
                        return false;
                    }
                    yml.set("version", "1.3");
                    version = "1.3";

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
                    version = "1.4";

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
                    version = "1.5";

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
                    version = "1.6";
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
                    version = "1.7";

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

                // After 1.7-pre6
                if (version.equals("1.7")) {
                    plugin.getLogger().warning("Old NPC file version found! Bumping version! (1.7 -> 1.8)");
                    BackupResult br = createBackup(file);
                    if (!br.success) {
                        plugin.getLogger().warning("Could not create backup before updating npcs.yml!");
                        return false;
                    }
                    yml.set("version", "1.8");

                    Set<String> npcs = yml.getKeys(false);
                    for (String npc : npcs) {
                        if (npc.equals("version")) continue; // its a key
                        ConfigurationSection section = yml.getConfigurationSection(npc);

                        assert section != null : "Section is null -- Upgrading NPC file from 1.7 to 1.8";

                        double dir = section.getDouble("direction");
                        Location loc = section.getLocation("location");
                        assert loc != null : "Location is null -- Upgrading NPC file from 1.7 to 1.8";
                        loc.setYaw((float) dir);

                        section.set("location", loc); // update the location
                        section.set("direction", null); // remove the direction field
                    }
                    try {
                        yml.save(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }


                // check for valid NPCs:
                boolean found = false;
                Set<String> npcs = yml.getKeys(false);
                for (String npc : npcs) {
                    if (npc.equals("version")) continue; // not an NPC uuid
                    ConfigurationSection section = yml.getConfigurationSection(npc);
                    boolean err = false;
                    boolean exists = false;
                    UUID uuid = UUID.fromString(npc);

                    try {
                        var sec = section.getLocation("location");
                        exists = sec != null;
                    } catch (Exception e) {
                        err = true;
                    }

                    if (err || !exists) {
                        String rawName = plugin.getMiniMessage().stripTags(section.getString("name"));
                        throw new IllegalStateException("Detected an NPC (" + rawName + ") with an invalid location! Please revert to 1.7.x and use the /npc fixconfig command to fix this!");
                    } else validNPCs.add(uuid);
                }
            }
            // migrate data to the new storage provider!
            plugin.getLogger().warning(" <!>  --  Migrating NPCs to dataformat 2!  --  <!>");

            List<NpcOuterClass.Npc> migrated = new ArrayList<>();
            for (String key : yml.getKeys(false)) {
                if (key.equals("version")) continue; // version tracking
                migrated.add(migrateNPC(UUID.fromString(key)));
            }
            trackedNpcs.addAll(migrated);
            createBackup(file); // create a backup just in case
            if (!file.delete()) {
                plugin.getLogger().warning("Couldn't delete old NPC file!");
            }
            justMigrated = true;
        }

        File configFile = new File(PARENT_DIRECTORY, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        switch (config.getString("storage.provider").toUpperCase(Locale.ROOT)) {
            case "LOCAL" -> storage = new FileStorage();
            case "MYSQL" -> storage = new MysqlStorage();
            case "MONGODB" -> storage = new MongoStorage();
            default -> throw new IllegalStateException("Unknown provider: " + config.getString("storage.provider"));
        }

        storage.init(plugin).whenComplete((unused, throwable) -> {
            if (throwable != null) throw new RuntimeException("Couldn't load storage provider!", throwable);
            if (justMigrated) saveNpcs();

            // load the NPCs now (skip if just migrated)

            plugin.getLogger().info("Loading NPCs!");
            AtomicInteger successfullyLoaded = new AtomicInteger();
            if (!justMigrated) {
                trackedNpcs.clear();
                getAllNpcs().whenComplete((internalNpcs, t) -> Bukkit.getScheduler().runTask(plugin, () -> {
                    // this needs to be sync since it adds entities
                    if (t != null) {
                        plugin.getLogger().log(Level.SEVERE, "An error occured whilst loading NPCs!", t);
                        return;
                    }

                    // track the npcs
                    for (NpcOuterClass.Npc npc : internalNpcs) {
                        if (plugin.isDebug()) {
                            plugin.getLogger().info("[DEBUG] Tracking NPC " + npc.getUuid());
                        }
                        track(npc);
                        if (loadProto(npc)) {
                            successfullyLoaded.incrementAndGet();
                        } else {
                            plugin.getLogger().log(Level.WARNING, "NPC " + npc.getUuid() + " could not be loaded!");
                        }
                    }
                    plugin.getLogger().info("Succesfully loaded " + successfullyLoaded + " NPCs, failed to load " + (trackedNpcs.size() - successfullyLoaded.get()) + " NPCs.");
                }));
            } else {
                trackedNpcs.forEach(npc -> {
                    if (loadProto(npc)) {
                        successfullyLoaded.incrementAndGet();
                    } else {
                        plugin.getLogger().log(Level.WARNING, "NPC " + npc.getUuid() + " could not be loaded!");
                    }
                });
                plugin.getLogger().info("Succesfully loaded " + successfullyLoaded + " cached NPCs, failed to load " + (trackedNpcs.size() - successfullyLoaded.get()) + " NPCs.");
            }
        });
        return true;
    }

    public boolean loadProto(NpcOuterClass.Npc npc) {
        if (plugin.isDebug()) {
            plugin.getLogger().info("[DEBUG] Loading NPC " + npc.getUuid());
        }
        try {
            if (loadNPC(UUID.fromString(npc.getUuid()))) {
                return true;
            } else {
                plugin.getLogger().warning("Failed to load NPC " + npc.getUuid() + "!");
            }
        } catch (IllegalWorldException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load NPC " + npc.getUuid() + " due to an invalid world.");
        } catch (UntrackedNpcException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load NPC " + npc.getUuid() + " as it was not tracked.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load NPC " + npc.getUuid() + " due to an unknown error.", e);
        }
        brokenNPCs.put(UUID.fromString(npc.getUuid()), npc);
        return false;
    }

    /**
     * <p> Adds an NPC to the `npcs.yml` file.
     * </p>
     *
     * @param npc The NPC to store
     */
    public void addNPC(InternalNpc npc) {
        trackedNpcs.add(ProtoWrapper.toProtoNpc(npc));
        saveNpcs();
    }

    /**
     * <p> Gets the NPC of the specified UUID
     * </p>
     *
     * @param uuid The NPC to load from the file
     * @return if the load was successful
     */
    public boolean loadNPC(UUID uuid) {
        if (trackedNpcs.stream().noneMatch(npc -> npc.getUuid().equals(uuid.toString()))) {
            throw new IllegalArgumentException("NPC does not exist!");
        }
        NpcOuterClass.Npc proto = trackedNpcs.stream().filter(n -> n.getUuid().equals(uuid.toString())).findFirst().orElse(null);
        if (proto == null) throw new IllegalArgumentException("NPC does not exist!"); // should never be thrown

        InternalNpc npc = ProtoWrapper.fromProtoNpc(proto);
        if (npc.getWorld() == null) {
            printInvalidConfig();
            brokenNPCs.put(uuid, proto);
            return false;
        }
        // has to be on a sync thread
        Bukkit.getScheduler().runTask(plugin, npc::createNPC);
        return true;
    }

    public NpcOuterClass.Npc migrateNPC(UUID uuid) {
        File file = new File("plugins/CustomNPCs/npcs.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yml.getConfigurationSection(uuid.toString());
        if (section == null) throw new IllegalArgumentException("NPC uuid cannot be null.");

        List<LegacyAction> actionImpls = new ArrayList<>();
        List<Action> actions;

        if (section.getList("actions") == null) { // meaning it does not exist
            throw new IllegalStateException("Your NPC file is too old to migrate to 1.8! Please update to 1.7.x first!");
        }
        if (Objects.requireNonNull(section.getString("name")).contains("§")) {
            throw new IllegalStateException("Your NPC file is too old to migrate to 1.8! Please update to 1.7.x first!");
        }

        String rawName = plugin.getMiniMessage().stripTags(section.getString("name"));
        World world;

        try {
            world = Bukkit.getWorld(Objects.requireNonNull(section.getString("world")));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("The NPC '" + rawName + "' has an invalid world. Please downgrade to 1.7.x and use the /npc fixconfig command!");
        }

        Location location;
        try {
            location = section.getLocation("location");
        } catch (Exception ex) {
            throw new IllegalArgumentException("The NPC '" + rawName + "' has an invalid location. Please downgrade to 1.7.x and use the /npc fixconfig command!");
        }

        if (world == null)
            throw new IllegalArgumentException("The NPC '" + rawName + "' has an invalid world. Please downgrade to 1.7.x and use the /npc fixconfig command!");
        if (location == null)
            throw new IllegalArgumentException("The NPC '" + rawName + "' has an invalid location. Please downgrade to 1.7.x and use the /npc fixconfig command!");


        // use the actions freshly converted

        actions = new ArrayList<>();
        for (String s : section.getStringList("actions")) {
            actions.add(Action.parse(s));
        }

        InternalNpc npc = plugin.createNPC(world, location, new Equipment(section.getItemStack("headItem"), section.getItemStack("chestItem"), section.getItemStack("legsItem"), section.getItemStack("feetItem"), section.getItemStack("handItem"), section.getItemStack("offhandItem")), new Settings(section.getBoolean("clickable"), section.getBoolean("tunnelvision"), true, section.getString("value"), section.getString("signature"), section.getString("skin"), section.getString("name"), section.getString("customHologram"), section.getBoolean("hideInteractableHologram")), uuid, null, actions);

        return ProtoWrapper.toProtoNpc(npc);
    }


    /**
     * <p> Gets the set of stored UUIDs.
     * </p>
     *
     * @return the set of stored NPC uuids.
     */
    public Set<UUID> getNPCIds() {
        return trackedNpcs.stream().map(npc -> UUID.fromString(npc.getUuid())).collect(Collectors.toSet());
    }

    /**
     * <p> Removes the specified NPC from storage
     * </p>
     *
     * @param uuid The NPC uuid to remove
     */
    public void remove(UUID uuid) {
        NpcOuterClass.Npc proto = trackedNpcs.stream().filter(npc -> npc.getUuid().equals(uuid.toString())).findFirst().orElse(null);
        if (proto == null) throw new IllegalArgumentException("NPC does not exist!");
        trackedNpcs.remove(proto);
        saveNpcs();
    }

    private BackupResult createBackup(File file) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        File f = new File(PARENT_DIRECTORY, new Date().toString().replace(" ", "_").replace(":", "_") + "_backup_of_" + file.getName() + Instant.now().hashCode());
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

    public CompletableFuture<Boolean> saveNpcs() {
        return storage.save(ProtoWrapper.serializeProtoList(trackedNpcs));
    }

    public void track(NpcOuterClass.Npc proto) {
        NpcOuterClass.Npc found = trackedNpcs.stream().filter(npc -> npc.getUuid().equals(proto.getUuid())).findFirst().orElse(null);
        if (found != null) trackedNpcs.remove(found);
        trackedNpcs.add(proto);
    }

    /**
     * Gets ALL npcs stored in the storage provider, their configurations are not validated.
     */
    public CompletableFuture<List<NpcOuterClass.Npc>> getAllNpcs() {
        CompletableFuture<List<NpcOuterClass.Npc>> future = new CompletableFuture<>();
        storage.load().whenComplete((bytes, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }
            try {
                future.complete(ProtoWrapper.deserializeProtoList(bytes));
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public void resetTracked() {
        trackedNpcs.clear();
    }

    private record BackupResult(Path filePath, boolean success) {
    }
}
