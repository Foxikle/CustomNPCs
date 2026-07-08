/*
 * Copyright (c) 2026. Foxikle
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
import dev.foxikle.customnpcs.conditions.Condition;
import dev.foxikle.customnpcs.conditions.Selector;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.data.Settings;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import lombok.*;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@ToString
public class StorableNPC {

    public static final Codec<StorableNPC> CODEC = StructCodec.struct(
            "uuid", Codec.UUID_STRING, StorableNPC::getUniqueID,
            "world", Codec.STRING, StorableNPC::getWorld,
            "settings", Settings.CODEC, StorableNPC::getSettings,
            "equipment", Equipment.CODEC, StorableNPC::getEquipment,
            "spawnLocation", StorableLocation.CODEC, StorableNPC::getSpawnLoc,
            "actions", Action.CODEC.list(), StorableNPC::getActions,
            "injectionConditions", Condition.CODEC.list(), StorableNPC::getInjectionConditions,
            "injectionSelector", Codec.Enum(Selector.class), StorableNPC::getInjectionSelector,
            StorableNPC::new
    );

    @Setter(AccessLevel.NONE)
    private final UUID uniqueID;
    @Setter(AccessLevel.NONE)
    private String world;
    private Settings settings;
    private Equipment equipment;
    private StorableLocation spawnLoc;
    private List<Action> actions;
    private List<Condition> injectionConditions;
    private Selector injectionSelector;

    public StorableNPC(InternalNpc pluginObj) {
        this.uniqueID = pluginObj.getUniqueID();
        this.world = pluginObj.getWorld().getName();
        this.settings = pluginObj.getSettings();
        this.equipment = pluginObj.getEquipment();
        this.spawnLoc = StorableLocation.convert(pluginObj.getSpawnLoc());
        this.actions = pluginObj.getActions();
        this.injectionConditions = pluginObj.getInjectionConditions();
        this.injectionSelector = pluginObj.getInjectionSelector();
    }

    public InternalNpc toPluginObject() {
        World w = Bukkit.getWorld(world);
        if (w == null) throw new IllegalStateException("The world " + world + " does not exist!");
        return CustomNPCs.getInstance().createNPC(w, spawnLoc.convert(), equipment, settings, uniqueID, null, actions,
                injectionConditions, injectionSelector);
    }

    @With
    public record StorableLocation(double x, double y, double z, float yaw, float pitch, String world) {

        public static final Codec<StorableLocation> CODEC = StructCodec.struct(
                "x", Codec.DOUBLE, StorableLocation::x,
                "y", Codec.DOUBLE, StorableLocation::y,
                "z", Codec.DOUBLE, StorableLocation::z,
                "pitch", Codec.FLOAT, StorableLocation::pitch,
                "yaw", Codec.FLOAT, StorableLocation::yaw,
                "world", Codec.STRING, StorableLocation::world,
                StorableLocation::new
        );

        public static StorableLocation convert(Location l) {
            return new StorableLocation(l.x(), l.y(), l.z(), l.getYaw(), l.getPitch(), l.getWorld().getName());
        }

        public Location convert() {
            return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
        }
    }
}