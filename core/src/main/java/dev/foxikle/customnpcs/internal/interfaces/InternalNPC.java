package dev.foxikle.customnpcs.internal.interfaces;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.data.Settings;
import dev.foxikle.customnpcs.internal.LookAtAnchor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * An interface to provide multiversion support
 */
public interface InternalNPC {


    /**
     * <p> Sets the NPC's loaction and rotation
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
     * @param name The name to give the text display
     * @return the TextDisplay representing the NPC's nametag
     */
    TextDisplay setupHologram(String name);

    /**
     * <p> Creates the NPC's clickable hologram
     * </p>
     *
     * @param name The name to give the text display
     * @return the TextDisplay representing the NPC's hologram
     */
    TextDisplay setupClickableHologram(String name);

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
     * <p> Gets the NPC's spawnpoint is
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
     * <p> Gets the text display representing the NPC nametag
     * </p>
     *
     * @return the TextDisplay entity the NPC uses for their nametag
     */
    TextDisplay getHologram();

    /**
     * <p> Gets the text display representing the NPC nametag
     * </p>
     *
     * @return the TextDisplay entity the NPC uses for their clickable hologram
     */
    @Nullable
    TextDisplay getClickableHologram();

    /**
     * <p> Gets the World the NPC is in
     * </p>
     *
     * @return Gets the World the NPC is in
     */
    World getWorld();

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
    void moveTo(Location v);

    /**
     * <p> Permantanly deletes an NPC. Does NOT despawn it.
     * </p>
     */
    void delete();

    /**
     * <p> Sets the actions executed when the NPC is interacted with.
     * </p>
     *
     * @param actions The collection of actions
     */
    void setActions(Collection<Action> actions);

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
}
