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

package dev.foxikle.customnpcs.api;

import com.google.common.base.Preconditions;
import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.actions.LegacyAction;
import dev.foxikle.customnpcs.api.events.NpcDeleteEvent;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.data.Settings;
import dev.foxikle.customnpcs.internal.LookAtAnchor;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A class providing a 'Paper API friendly' NPC object requiring no NMS to use within a project.
 */
@SuppressWarnings("unused")
public class NPC {

    private final InternalNpc npc;

    /**
     * The intended way to create an NPC
     * <p>
     * By default, this npc has no actions
     * <p>
     * By default, this npc has no skin
     * <p>
     * By default, this npc has no name
     * <p>
     * By default, this npc is not interactable
     * <p>
     * By default, this npc is not resilient
     * <p>
     * By default, this npc has a yaw of 0
     * <p>
     * By default, this npc has tunnelvision
     *
     * @param world The world for the NPC to be created in
     * @throws NullPointerException  if the world is null
     * @throws IllegalStateException if the API is not initialized
     * @author Foxikle
     * @since 1.5-pre5
     */
    public NPC(@NotNull World world) {
        if (NPCApi.plugin == null) throw new IllegalStateException("You must initialize the api before using it!");
        if (world == null) throw new NullPointerException("world cannot be null.");
        UUID uuid = UUID.randomUUID();
        Settings settings = new Settings();
        settings.setResilient(false);
        this.npc = NPCApi.plugin.createNPC(world, new Location(world, 0, 0, 0), new Equipment(), settings, uuid, null, new ArrayList<>());
    }

    /**
     * A constructor for creating an api friendly npc object using an internal npc object.
     *
     * @param npc the internal npc to wrap for the api
     */
    public NPC(InternalNpc npc) {
        if (npc == null) throw new IllegalArgumentException("npc cannot be null.");
        this.npc = npc;
    }


    /**
     * <p>Sets the location of the NPC
     * </p>
     *
     * @param loc the new location for the NPC
     * @return the NPC with the modified location
     * @since 1.5.2-pre3
     * @deprecated see {@link #setPosition(Location)} (typo ;|)
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.8")
    public NPC setPostion(@NotNull Location loc) {
        return setPosition(loc);
    }

    /**
     * <p>Sets the location of the NPC
     * </p>
     *
     * @param loc the new location for the NPC
     * @return the NPC with the modified location
     * @since 1.5.2-pre3
     */
    public NPC setPosition(@NotNull Location loc) {
        Preconditions.checkArgument(loc != null, "loc cannot be null.");
        npc.setSpawnLoc(loc);
        npc.getSettings().setDirection(loc.getYaw());
        return this;
    }


    /**
     * <p>Sets the leggings the NPC is wearing
     * </p>
     *
     * @param skinName  the name to reference the skin by.
     * @param signature the encoded signature of the skin.
     * @param value     the encoded value of the skin
     * @return the NPC with the modified skin
     * @throws NullPointerException if any argument is null
     * @see <a href="ttps://mineskin.org/">Use this site by InventiveTalent to find value and signature</a>
     * @since 1.5.2-pre3
     */
    public NPC setSkin(@NotNull String skinName, @NotNull String signature, @NotNull String value) {
        Preconditions.checkArgument(signature != null, "signature cannot be null.");
        Preconditions.checkArgument(value != null, "value cannot be null.");
        Preconditions.checkArgument(skinName != null && !skinName.isEmpty(), "skinName cannot be null or empty");
        npc.getSettings().setSkinData(signature, value, skinName);
        npc.updateSkin();
        return this;
    }

    /**
     * <p>Sets the NPC's actions to the specified Collection
     * </p>
     *
     * @param actionImpls the collection of actions
     * @return the NPC with the modified set of actions
     * @see Action
     * @since 1.7-pre2
     */
    public NPC setActions(Collection<Action> actionImpls) {
        npc.setActions(List.copyOf(actionImpls));
        return this;
    }

    /**
     * Adds an Action to this NPC
     *
     * @param action the action to add
     * @return this NPC, for chaining
     * @see Action
     * @since 1.7-rc1
     */
    public NPC addAction(@NotNull Action action) {
        npc.addAction(action);
        return this;
    }

    /**
     * <p>Sets the NPC's actions to the specified Collection
     * </p>
     *
     * @param actionImpls the collection of actions
     * @return the NPC with the modified set of actions
     * @see Action
     * @since 1.5.2-pre3
     * @deprecated Use  {@link #setActions(Collection)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.8")
    public NPC setLegacyActions(Collection<LegacyAction> actionImpls) {
        List<Action> actionList = new ArrayList<>();
        for (LegacyAction legacyAction : actionImpls) {
            actionList.add(legacyAction.toAction());
        }
        npc.setActions(actionList);
        return this;
    }

    /**
     * Move the npc to the specified location. Takes into account pitch and yaw
     *
     * @param location the location to move to
     */
    public void moveTo(Location location) {
        npc.moveTo(location);
    }

    /**
     * Swings the NPC's arm
     *
     * @since 1.5.2-pre3
     */
    public void swingArm() {
        npc.swingArm();
    }

    /**
     * Injects the npc into the player's connection. This should be handled by the plugin, but this is here for more control.
     *
     * @param player the player to inject
     * @see Player
     * @since 1.5.2-pre3
     */
    public void injectPlayer(Player player) {
        npc.injectPlayer(player);
    }

    /**
     * Points the NPC's head in the direction of an entity
     *
     * @param e      The entity to look at
     * @param atHead If the npc should look head (true), or feet (false)
     */
    public void lookAt(@NotNull Entity e, boolean atHead) {
        Objects.requireNonNull(e);
        npc.lookAt(atHead ? LookAtAnchor.HEAD : LookAtAnchor.FEET, e);
    }

    /**
     * Points the NPC's head at a location
     *
     * @param location the location to look at
     */
    public void lookAt(@NotNull Location location) {
        Objects.requireNonNull(location);
        npc.lookAt(location);
    }

    /**
     * <p>Creates the NPC.
     * </p>
     *
     * @return UUID the NPC's uuid for later reference
     * @since 1.5.2-pre3
     */
    public UUID create() {
        npc.createNPC();
        return npc.getUniqueID();
    }

    /**
     * <p>Permanently deletes an NPC. They will NOT reappear on the next reload or server restart.
     * The NPC will NOT be despawned. Only applicable if the NPC is resilient.
     * </p>
     *
     * @apiNote If the NpcDeleteEvent gets canceled, this npc will not be deleted.
     *
     * @throws IllegalStateException if the NPC is not resilient
     * @since 1.5.2-pre3
     */
    public boolean delete() {
        if (!npc.getSettings().isResilient())
            throw new IllegalStateException("The NPC must be resilient to be deleted!");

        NpcDeleteEvent event = new NpcDeleteEvent(null, npc, NpcDeleteEvent.DeletionSource.API);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return false;

        npc.delete();
        return true;
    }

    /**
     * <p>Temporarily removes an NPC. They will reappear on the next reload or server restart IF it is resilient.
     * </p>
     *
     * @since 1.5.2-pre3
     */
    public void remove() {
        npc.remove();
    }

    /**
     * Gets the object representing the NPC's settings
     *
     * @return the object containing the resilience, tunnelvision, and interactablility
     */
    public Settings getSettings() {
        return npc.getSettings();
    }

    /**
     * Sets the npc's settings
     *
     * @param settings the settings to set
     * @see NPC#reloadSettings() Make sure to reload the settings after changing them!
     */
    public void setSettings(Settings settings) {
        npc.setSettings(settings);
    }

    /**
     * Gets the equipment object representing the NPC's armor and hand items
     *
     * @return the equipment object holding the NPC's items
     */
    public Equipment getEquipment() {
        return npc.getEquipment();
    }

    /**
     * Sets the npc's equipment
     *
     * @param equipment the equipment to set
     * @see NPC#reloadSettings() Make sure to reload the equipment after changing them!
     */
    public void setEquipment(Equipment equipment) {
        npc.setEquipment(equipment);
    }

    /**
     * Gets the spawning location of the NPC. This may be different from {@link NPC#getLocation()} if the NPC has pathfinding (Coming soon).
     *
     * @return The spawn location of the NPC
     */
    public Location getSpawnLocation() {
        return npc.getSpawnLoc();
    }

    /**
     * Gets te CURRENT location of the NPC. This may differ from {@link NPC#getSpawnLocation()} if the NPC has pathfinding
     *
     * @return the current location of the NPC
     */
    public Location getLocation() {
        return npc.getCurrentLocation();
    }

    /**
     * Reloads the NPC's settings within the Settings and Equipment objects
     *
     * @see Equipment
     * @see Settings
     */
    public void reloadSettings() {
        npc.reloadSettings();
    }
}
