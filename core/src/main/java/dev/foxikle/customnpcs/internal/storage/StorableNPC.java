package dev.foxikle.customnpcs.internal.storage;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.conditions.Condition;
import dev.foxikle.customnpcs.conditions.Selector;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.data.Settings;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;
import java.util.UUID;

@ConfigSerializable
@Data
@NoArgsConstructor
public class StorableNPC {
    @Setter(AccessLevel.NONE)
    private UUID uniqueID;
    @Setter(AccessLevel.NONE)
    private String world;
    private Settings settings;
    private Equipment equipment;
    private Location spawnLoc;
    private List<Action> actions;
    private List<Condition> injectionConditions;
    private Selector injectionSelector;

    public StorableNPC(InternalNpc pluginObj) {
        this.uniqueID = pluginObj.getUniqueID();
        this.world = pluginObj.getWorld().getName();
        this.settings = pluginObj.getSettings();
        this.equipment = pluginObj.getEquipment();
        this.spawnLoc = pluginObj.getSpawnLoc();
        this.actions = pluginObj.getActions();
        this.injectionConditions = pluginObj.getInjectionConditions();
        this.injectionSelector = pluginObj.getInjectionSelectionMode();
    }

    public InternalNpc toPluginObject() {
        World w = Bukkit.getWorld(world);
        if (w == null) throw new IllegalStateException("The world " + world + " does not exist!");
        return CustomNPCs.getInstance().createNPC(w, spawnLoc, equipment, settings, uniqueID, null, actions, injectionConditions, injectionSelector);
    }
}