/*
 * Copyright (c) 2024-2026. Foxikle
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

package dev.foxikle.customnpcs.internal.interfaces;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.conditions.Condition;
import dev.foxikle.customnpcs.conditions.Selector;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.data.Settings;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.InjectionManager;
import dev.foxikle.customnpcs.internal.LookAtAnchor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import studio.mevera.lotus.api.data.Key;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApiStatus.Internal
public interface InternalNpc {

    Key<InternalNpc> KEY = Key.of("npc", InternalNpc.class);

    void setPosRot(Location location);

    List<Condition> getInjectionConditions();

    InjectionManager getInjectionManager();

    Selector getInjectionSelector();

    void setInjectionConditions(List<Condition> conditions);

    void setInjectionSelector(Selector mode);

    void createNPC();

    void setupHolograms();

    void setupClickableHologram(String name);

    UUID getUniqueID();

    Location getCurrentLocation();

    Location getSpawnLoc();

    Entity getTarget();

    void setTarget(@Nullable Player target);

    void setSpawnLoc(Location spawnLoc);

    List<TextDisplay> getHolograms();

    @Nullable
    @SuppressWarnings("unused")
    TextDisplay getClickableHologram();

    @NotNull World getWorld();

    List<Action> getActions();

    void addAction(Action action);

    @SuppressWarnings("UnusedReturnValue")
    boolean removeAction(Action action);

    void injectPlayer(Player p);

    void remove();

    /**
     * <p> Moves the npc the DELTA provided by the vector.
     * </p>
     *
     * @param v The location to move to the npc at
     */
    void moveTo(Vector v);

    default void delete() {
        CustomNPCs.getInstance().getStorageManager().remove(getUniqueID());
    }

    void setActions(List<Action> actions);

    void lookAt(LookAtAnchor anchor, Entity e);

    void lookAt(Location loc);

    void setYRotation(float rot);

    void setXRotation(float rot);

    float getYaw();

    float getPitch();

    void updateSkin();

    void swingArm();

    Equipment getEquipment();

    Settings getSettings();

    void reloadSettings();

    void setSettings(Settings s);

    void setEquipment(Equipment e);

    Particle getSpawnParticle();

    /**
     * Clones the NPC object.
     * <p>
     * THE UUID IS **<strong>NOT</strong>** CLONED, the new npc has its own UUID.
     * @return the cloned object, with a different memory address.
     */
    InternalNpc clone();

    void teleport(Location loc);

    void withdraw(Player player);

    default Map<Condition, Boolean> evaluateInjectionConditions(Player player){
        Map<Condition, Boolean> map = new HashMap<>();
        for (Condition c : getInjectionConditions()) {
            map.put(c, c.compute(player));
        }
        return map;
    }

    default boolean passesInjectionConditions(Player player) {
        Map<Condition, Boolean> map = evaluateInjectionConditions(player);
        if (map.isEmpty()) return true;
        return (getInjectionSelector() == Selector.ALL ? !map.containsValue(false) : map.containsValue(true));
    }
}
