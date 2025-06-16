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

package dev.foxikle.customnpcs.internal.interfaces;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.data.Settings;
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

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * An interface to provide multi-version support
 */
@ApiStatus.Internal
public interface InternalNpc {


    /**
     * <p> Sets the NPC's location and rotation
     * </p>
     *
     * @param location The location to set the NPC
     */
    void setPosRot(Location location);

    /**
     * <p> Creates the NPC and injects it into every player
     * </p>
     */
    void createNPC();

    /**
     * <p> Creates the NPC's name hologram
     * </p>
     *
     * @return the TextDisplay representing the NPC's name tag
     */
    void setupHolograms();

    /**
     * <p> Creates the NPC's clickable hologram
     * </p>
     *
     * @param name The name to give the text display
     * @return the TextDisplay representing the NPC's hologram
     */
    void setupClickableHologram(String name);

    /**
     * Gets the NPC's uuid
     *
     * @return the NPC's uuid
     */
    UUID getUniqueID();

    /**
     * <p> Gets the NPC's CURRENT location
     * </p>
     *
     * @return the place where the NPC is currently located
     */
    Location getCurrentLocation();

    /**
     * <p> Gets the NPC's spawn point is
     * </p>
     *
     * @return the place where the NPC spawns
     */
    Location getSpawnLoc();

    /**
     * <p> Gets the Entity the NPC is targeting
     * </p>
     *
     * @return the Item the NPC is wearing on their feet
     */
    Entity getTarget();

    /**
     * <p> Sets the NPC's target
     * </p>
     *
     * @param target the Player the Entity should target
     */
    void setTarget(@Nullable Player target);

    /**
     * <p> Sets the Location where the NPC should spawn
     * </p>
     *
     * @param spawnLoc The location to spawn
     */
    void setSpawnLoc(Location spawnLoc);

    /**
     * <p> Gets the text display representing the NPC name
     * </p>
     *
     * @return the TextDisplay entity the NPC uses for their name tag
     */
    List<TextDisplay> getHolograms();

    /**
     * <p> Gets the text display representing the NPC name
     * </p>
     *
     * @return the TextDisplay entity the NPC uses for their clickable hologram
     */
    @Nullable
    @SuppressWarnings("unused")
    TextDisplay getClickableHologram();

    /**
     * <p> Gets the World the NPC is in
     * </p>
     *
     * @return Gets the World the NPC is in
     */
    @NotNull World getWorld();

    /**
     * <p> Gets the list of Actions the NPC executes when interacted with
     * </p>
     *
     * @return the list of Actions the NPC executes when interacted with
     */
    List<Action> getActions();

    /**
     * <p> Adds an action to the NPC's actions
     * </p>
     *
     * @param action The action to add
     */
    void addAction(Action action);

    /**
     * <p> Removes an action from the NPC's actions
     * </p>
     *
     * @param action The action to remove
     * @return if it was successfully removed
     */
    @SuppressWarnings("UnusedReturnValue")
    boolean removeAction(Action action);

    /**
     * <p> Injects packets into the specified player's connection
     * </p>
     *
     * @param p The player to inject
     */
    void injectPlayer(Player p);


    /**
     * <p> Despawns the NPC
     * </p>
     */
    void remove();

    /**
     * <p> Moves the npc to the location
     * </p>
     *
     * @param v The location to move to the npc at
     */
    void moveTo(Vector v);

    /**
     * <p> Permanently deletes an NPC. Does NOT despawn it.
     * </p>
     */
    void delete();

    /**
     * <p> Sets the actions executed when the NPC is interacted with.
     * </p>
     *
     * @param actions The collection of actions
     */
    void setActions(List<Action> actions);

    /**
     * Looks at the entity
     *
     * @param anchor The anchor point on the entity to look at
     * @param e      the entity to look at
     */
    void lookAt(LookAtAnchor anchor, Entity e);

    /**
     * Makes the NPC look at a location
     * @param loc the location to look at
     */
    void lookAt(Location loc);

    /**
     * Set the y rotation on this entity
     * @param rot the yaw
     */
    void setYRotation(float rot);

    /**
     * Set the x rotation on this entity
     *
     * @param rot the pitch
     */
    void setXRotation(float rot);

    /**
     * The yaw of the NPC's head
     *
     * @return the yaw from -180 to 180
     */
    float getYaw();

    /**
     * Retrieves the pitch (rotation around the X-axis) of the NPC.
     *
     * @return the pitch value, typically within the range of -90 to 90, where -90
     * represents looking straight up and 90 represents looking straight down.
     */
    float getPitch();

    /**
     * Reads the skin data from the Settings object and applies it to the NPC
     * @see Settings
     */
    void updateSkin();

    /**
     * Swings the NPC's arm
     */
    void swingArm();

    /**
     * Gets the equipment object representing the NPC's items
     * @return the equipment object
     */
    Equipment getEquipment();

    /**
     * Gets the settings object representing the NPC's settings
     * @return the settings object
     */
    Settings getSettings();

    /**
     * Reloads the NPC's settings and equipment
     */
    void reloadSettings();

    /**
     * Set the settings of the NPC
     * @param s The settings object
     */
    void setSettings(Settings s);

    /**
     * Sets the NPC's equipment.
     * @param e the equipment
     */
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
}
