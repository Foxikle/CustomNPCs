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

package dev.foxikle.customnpcs.data;

import com.destroystokyo.paper.profile.ProfileProperty;
import dev.foxikle.customnpcs.api.NPC;
import dev.foxikle.customnpcs.api.Pose;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

/**
 * A class holding the data for an NPC's settings
 */
@AllArgsConstructor
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class Settings {

    @Getter boolean interactable = false;
    @Getter boolean tunnelvision = false;
    @Getter boolean resilient = true;
    @Getter
    int interpolationDuration = CustomNPCs.INTERPOLATION_DURATION;
    @Getter
    String value = "";
    @Getter
    String signature = "";
    String[] holograms = new String[]{"An Unnamed NPC"};
    @Getter
    String skinName = "not set";
    @Getter
    boolean hideClickableHologram = false;
    @Getter
    String customInteractableHologram = "";
    @Getter
    Pose pose = Pose.STANDING;
    @Getter
    Color hologramBackground = null;
    @Getter
    boolean hideBackgroundHologram = false;
    @Getter
    boolean upsideDown = false;

    /**
     * Creates a settings object with the specified settings
     * @param interactable If the npc has actions to execute
     * @param tunnelvision If the npc will look at players
     * @param resilient If the npc will persist on restarts
     * @param direction The direction to look
     * @param value The value of the npc's skin
     * @param signature The signature of the npc's skin
     * @param skinName The name of the skin as it is referenced in the Menu
     * @param name The name of NPC formatted in SERIALIZED minimessage format
     * @param customInteractableHologram The custom hologram
     * @param hideClickableHologram      If the NPC's Clickable hologram should be hidden
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.9")
    public Settings(boolean interactable, boolean tunnelvision, boolean resilient, String value, String signature, String skinName, String name, String customInteractableHologram, boolean hideClickableHologram) {
        this.interactable = interactable;
        this.tunnelvision = tunnelvision;
        this.resilient = resilient;
        this.value = value;
        this.signature = signature;
        this.skinName = skinName;
        this.holograms = new String[]{name};
        this.hideClickableHologram = hideClickableHologram;
        this.customInteractableHologram = customInteractableHologram;
    }

    /**
     * Creates a settings object with the specified settings
     *
     * @param interactable               If the npc has actions to execute
     * @param tunnelvision               If the npc will look at players
     * @param resilient                  If the npc will persist on restarts
     * @param value                      The value of the npc's skin
     * @param signature                  The signature of the npc's skin
     * @param skinName                   The name of the skin as it is referenced in the Menu
     * @param name                       The name of NPC formatted in SERIALIZED minimessage format
     * @param customInteractableHologram The custom hologram
     * @param hideClickableHologram      If the NPC's Clickable hologram should be hidden
     * @param interpolationDuration      How long to interpolate the teleportation of the NPC and its nametags
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.9")
    public Settings(boolean interactable, boolean tunnelvision, boolean resilient, String value, String signature, String skinName, String name, String customInteractableHologram, boolean hideClickableHologram, int interpolationDuration) {
        this.interactable = interactable;
        this.tunnelvision = tunnelvision;
        this.resilient = resilient;
        this.value = value;
        this.signature = signature;
        this.skinName = skinName;
        this.holograms = new String[]{name};
        this.hideClickableHologram = hideClickableHologram;
        this.customInteractableHologram = customInteractableHologram;
        this.interpolationDuration = interpolationDuration;
    }

    /**
     * Creates a settings object with the specified settings
     *
     * @param interactable               If the npc has actions to execute
     * @param tunnelvision               If the npc will look at players
     * @param resilient                  If the npc will persist on restarts
     * @param direction                  The direction to look
     * @param value                      The value of the npc's skin
     * @param signature                  The signature of the npc's skin
     * @param skinName                   The name of the skin as it is referenced in the Menu
     * @param holograms                  The lines of the NPC's hologram, formatted in SERIALIZED minimessage format. Index 0 corresponds to the top (first) line.
     * @param customInteractableHologram The custom hologram
     * @param hideClickableHologram      If the NPC's Clickable hologram should be hidden
     * @param pose                       The Pose of the NPC
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.9")
    public Settings(boolean interactable, boolean tunnelvision, boolean resilient, double direction, String value, String signature, String skinName, String[] holograms, String customInteractableHologram, boolean hideClickableHologram, Pose pose) {
        this.interactable = interactable;
        this.tunnelvision = tunnelvision;
        this.resilient = resilient;
        this.direction = direction;
        this.value = value;
        this.signature = signature;
        this.skinName = skinName;
        this.holograms = holograms;
        this.hideClickableHologram = hideClickableHologram;
        this.customInteractableHologram = customInteractableHologram;
        this.pose = pose;
    }

    /**
     * Creates a settings object with the specified settings
     *
     * @param interactable               If the npc has actions to execute
     * @param tunnelvision               If the npc will look at players
     * @param resilient                  If the npc will persist on restarts
     * @param direction                  The direction to look
     * @param value                      The value of the npc's skin
     * @param signature                  The signature of the npc's skin
     * @param skinName                   The name of the skin as it is referenced in the Menu
     * @param holograms                  The lines of the NPC's hologram, formatted in SERIALIZED minimessage format. Index 0 corresponds to the top (first) line.
     * @param customInteractableHologram The custom hologram
     * @param hideClickableHologram      If the NPC's Clickable hologram should be hidden
     * @param pose                       The Pose of the NPC
     */
    public Settings(boolean interactable, boolean tunnelvision, boolean resilient, double direction, String value, String signature, String skinName, String[] holograms, String customInteractableHologram, boolean hideClickableHologram, Pose pose, boolean upsideDown) {
        this.interactable = interactable;
        this.tunnelvision = tunnelvision;
        this.resilient = resilient;
        this.direction = direction;
        this.value = value;
        this.signature = signature;
        this.skinName = skinName;
        this.holograms = holograms;
        this.hideClickableHologram = hideClickableHologram;
        this.customInteractableHologram = customInteractableHologram;
        this.pose = pose;
        this.upsideDown = upsideDown;
    }

    /**
     * Creates a settings object with the specified settings
     *
     * @param interactable               If the npc has actions to execute
     * @param tunnelvision               If the npc will look at players
     * @param resilient                  If the npc will persist on restarts
     * @param direction                  The direction to look
     * @param value                      The value of the npc's skin
     * @param signature                  The signature of the npc's skin
     * @param skinName                   The name of the skin as it is referenced in the Menu
     * @param holograms                  The lines of the NPC's hologram, formatted in SERIALIZED minimessage format. Index 0 corresponds to the top (first) line.
     * @param customInteractableHologram The custom hologram
     * @param hideClickableHologram      If the NPC's Clickable hologram should be hidden
     * @param interpolationDuration      How long to interpolate the teleportation of the NPC and its nametags
     */
    public Settings(boolean interactable, boolean tunnelvision, boolean resilient, double direction, String value, String signature, String skinName, String[] holograms, String customInteractableHologram, boolean hideClickableHologram, int interpolationDuration) {
        this.interactable = interactable;
        this.tunnelvision = tunnelvision;
        this.resilient = resilient;
        this.direction = direction;
        this.value = value;
        this.signature = signature;
        this.skinName = skinName;
        this.holograms = holograms;
        this.hideClickableHologram = hideClickableHologram;
        this.customInteractableHologram = customInteractableHologram;
        this.interpolationDuration = interpolationDuration;
    }

    /**
     * Creates a settings with defaults.
     */
    public Settings() {
        // default constructor
    }

    /**
     * Sets if this NPC should be interactable, and if the plugin should process actions, and show the clickable
     * hologram. Showing the hologram can be overridden with {@link Settings#setHideClickableHologram(boolean)}.
     *
     * @param interactable if this NPC should be interactable
     * @return this, for chaining.
     */
    public Settings setInteractable(boolean interactable) {
        this.interactable = interactable;
        return this;
    }

    /**
     * Sets if this NPC has tunnel vision. If this NPC has tunnel vision, it will not look at nearby players.
     * To set the direction the NPC looks, there are 3 options.
     * <br>
     * {@link NPC#setFacing(float, float)} uses an arbitrary pitch and yaw
     * <br>
     * {@link NPC#lookAt(Location)} uses a point in the world to look at
     * <br>
     * {@link NPC#lookAt(Entity, boolean)} looks at some point on an entity. true corresponds
     * to the head of the entity, and false corresponds to the feet of the entity
     *
     * @param tunnelvision true to ignore nearby players, false otherwise.
     * @return this, for chaining.
     */
    public Settings setTunnelvision(boolean tunnelvision) {
        this.tunnelvision = tunnelvision;
        return this;
    }

    /**
     * Sets if this NPC should persist on server restarts. It defaults to <strong>true</strong>. Setting this to false
     * prevents the plugin from saving this npc.
     *
     * @param resilient true to save this npc, false to keep it as a temporary NPC.
     * @return this, for chaining.
     */
    public Settings setResilient(boolean resilient) {
        this.resilient = resilient;
        return this;
    }

    /**
     * Deprecated: - use {@link NPC#setFacing(float, float)}, {@link NPC#lookAt(Entity, boolean)}, or
     * {@link NPC#lookAt(Location)} instead.
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.8")
    public Settings setDirection(double direction) {
        this.direction = direction;
        return this;
    }

    /**
     * Sets the ticks this NPC should be interpolated for on move. This defaults to the value defined in the config,
     * but can be overridden for this NPC directly. The interpolation makes the nametags move more smoothly with the
     * NPC.
     *
     * @param duration the number of ticks to interpolate a movement over.
     * @return this, for chaining.
     */
    public Settings setInterpolationDuration(int duration) {
        this.interpolationDuration = duration;
        return this;
    }

    /**
     * Sets if the "CLICK" hologram (default) should be hidden. If the NPC is not interactable, this setting
     * has no effect. This defaults to false.
     *
     * @param hideClickableHologram true to hide, false to show (default)
     * @return this, for chaining
     */
    public Settings setHideClickableHologram(boolean hideClickableHologram) {
        this.hideClickableHologram = hideClickableHologram;
        return this;
    }

    /**
     * Allows the "CLICK" hologram (default) to be overridden from the value defined in the config.yml.
     *
     * @param customHologram the minimessage encoded component to replace the default with
     * @return this, for chaining
     */
    public Settings setCustomInteractableHologram(String customHologram) {
        this.customInteractableHologram = customHologram;
        return this;
    }

    /**
     * Allows the "CLICK" hologram (default) to be overridden from the value defined in the config.yml.
     *
     * @param customHologram the component to replace the default with
     * @return this, for chaining
     */
    public Settings setCustomInteractableHologram(Component customHologram) {
        this.customInteractableHologram = MiniMessage.miniMessage().serialize(customHologram);
        return this;
    }

    /**
     * Allows the NPC's pose to be changed. Please note that some poses, like {@link Pose#SLEEPING} and
     * {@link Pose#SWIMMING} have very small hitboxes, and are hard and/or annoying to interact with.
     *
     * @param pose the {@link Pose} to use
     * @return this, for chaining.
     */
    public Settings setPose(Pose pose) {
        this.pose = pose;
        return this;
    }

    @ApiStatus.Experimental
    public Settings setHologramBackground(Color color) {
        this.hologramBackground = color;
        return this;
    }

    @ApiStatus.Experimental
    public Settings setHideBackgroundHologram(boolean hideBackgroundHologram) {
        this.hideBackgroundHologram = hideBackgroundHologram;
        return this;
    }

    /**
     * Allows for setting the NPC's model upside down as if it was named "Dinnerbone" or "Grumm". Defaults to false.
     *
     * @param upsideDown true to make the model upsidedown, false for regular.
     * @return this, for chaining.
     */
    public Settings setUpsideDown(boolean upsideDown) {
        this.upsideDown = upsideDown;
        return this;
    }

    /**
     * Gets the contents of NPC's holograms. Index 0 corresponds to the top (first) line.
     *
     * @return the NPC's holograms, as components
     */
    public Component[] getHolograms() {
        Component[] components = new Component[holograms.length];
        for (int i = 0; i < holograms.length; i++) {
            components[i] = MiniMessage.miniMessage().deserialize(holograms[i]);
        }
        return components;
    }

    /**
     * Sets the NPC's holograms. These components will be serialized into strings following the MiniMessage format for storage. Index 0 corresponds to the top (first) line.
     *
     * @param holograms the collection of components
     */
    public Settings setHolograms(Component... holograms) {
        this.holograms = new String[holograms.length];
        for (int i = 0; i < holograms.length; i++) {
            this.holograms[i] = MiniMessage.miniMessage().serialize(holograms[i]);
        }
        return this;
    }

    /**
     * Gets the contents of NPC's holograms, in serialized MiniMessage format. Index 0 corresponds to the top (first) line.
     *
     * @return the NPC's holograms, as serialized MiniMessage strings
     */
    public String[] getRawHolograms() {
        return holograms;
    }

    /**
     * Sets the NPC's holograms. The plugin will parse these strings into components following the MiniMessage format. Index 0 corresponds to the top (first) line.
     *
     * @param holograms the collection of strings, optionally in minimessage format
     */
    public Settings setRawHolograms(String... holograms) {
        this.holograms = holograms;
        return this;
    }

    /**
     * Gets the NPC's name in serialized minimessage format. Since 1.7.5 onward, this method returns EITHER: the first (top) hologram OR the default, an empty string.
     *
     * @return the NPC's serialized name
     * @deprecated NPCs no longer have "names" per se, but rather a collection of name lines.
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.9")
    public String getName() {
        if (holograms.length > 0) {
            return holograms[0];
        }
        return "";
    }

    /**
     * Sets the top (first) line of the NPC's hologram
     *
     * @param name as a component
     * @deprecated As of 1.7.5, NPCs no longer have "names" per se, but rather a collection of name lines. This method sets the first (top) hologram.
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.9")
    public Settings setName(Component name) {
        String serialized = MiniMessage.miniMessage().serialize(name);
        if (holograms.length > 0) {
            holograms[0] = serialized;
            return this;
        }
        holograms = new String[]{serialized};
        return this;
    }

    /**
     * Sets the top (first) line of the NPC's hologram
     *
     * @param name as a string, serialized in minimessage format
     * @deprecated As of 1.7.5, NPCs no longer have "names" per se, but rather a collection of name lines. This method sets the first (top) hologram.
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.9")
    public Settings setName(String name) {
        if (holograms.length > 0) {
            holograms[0] = name;
            return this;
        }
        holograms = new String[]{name};
        return this;
    }

    /**
     * Sets the skin data in on fell swoop
     *
     * @param signature the skin signature
     * @param value     the skin value
     * @param skinName  the cosmetic name
     */
    public Settings setSkinData(String signature, String value, String skinName) {
        this.signature = signature;
        this.value = value;
        this.skinName = skinName;
        return this;
    }

    public Settings setSkin(Player player) {
        String signature = null;
        String value = null;
        for (ProfileProperty property : player.getPlayerProfile().getProperties()) {
            if (property.getName().equals("textures")) {
                signature = property.getSignature();
                value = property.getValue();
            }
        }
        if (signature == null || value == null) {
            throw new IllegalStateException("Player does not have a valid skin!");
        }
        this.signature = signature;
        this.value = value;
        this.skinName = player.getName() + "'s skin";
        return this;
    }

    @SuppressWarnings("all")
    public Settings clone(){
        return new Settings(interactable, tunnelvision, resilient, value, signature, skinName, name, customInteractableHologram, hideClickableHologram, pose);
    }
}
