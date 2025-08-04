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

package dev.foxikle.customnpcs.data;

import com.destroystokyo.paper.profile.ProfileProperty;
import dev.foxikle.customnpcs.api.Pose;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

/**
 * A class holding the data for an NPC's settings
 */
@Setter
//@Builder
@AllArgsConstructor
public class Settings {


    @Getter
    boolean interactable = false;
    @Getter
    boolean tunnelvision = false;
    @Getter
    boolean resilient = true;
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.8")
    @Getter
    double direction = 180;
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
     *
     * @param interactable               If the npc has actions to execute
     * @param tunnelvision               If the npc will look at players
     * @param resilient                  If the npc will persist on restarts
     * @param direction                  The direction to look
     * @param value                      The value of the npc's skin
     * @param signature                  The signature of the npc's skin
     * @param skinName                   The name of the skin as it is referenced in the Menu
     * @param name                       The name of NPC formatted in SERIALIZED minimessage format
     * @param customInteractableHologram The custom hologram
     * @param hideClickableHologram      If the NPC's Clickable hologram should be hidden
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.9")
    public Settings(boolean interactable, boolean tunnelvision, boolean resilient, double direction, String value, String signature, String skinName, String name, String customInteractableHologram, boolean hideClickableHologram) {
        this.interactable = interactable;
        this.tunnelvision = tunnelvision;
        this.resilient = resilient;
        this.direction = direction;
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
     * @param direction                  The direction to look
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
    public Settings(boolean interactable, boolean tunnelvision, boolean resilient, double direction, String value, String signature, String skinName, String name, String customInteractableHologram, boolean hideClickableHologram, int interpolationDuration) {
        this.interactable = interactable;
        this.tunnelvision = tunnelvision;
        this.resilient = resilient;
        this.direction = direction;
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
    public void setHolograms(Component... holograms) {
        this.holograms = new String[holograms.length];
        for (int i = 0; i < holograms.length; i++) {
            this.holograms[i] = MiniMessage.miniMessage().serialize(holograms[i]);
        }
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
    public void setRawHolograms(String... holograms) {
        this.holograms = holograms;
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
    public void setName(Component name) {
        String serialized = MiniMessage.miniMessage().serialize(name);
        if (holograms.length > 0) {
            holograms[0] = serialized;
            return;
        }
        holograms = new String[]{serialized};
    }

    /**
     * Sets the top (first) line of the NPC's hologram
     *
     * @param name as a string, serialized in minimessage format
     * @deprecated As of 1.7.5, NPCs no longer have "names" per se, but rather a collection of name lines. This method sets the first (top) hologram.
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.9")
    public void setName(String name) {
        if (holograms.length > 0) {
            holograms[0] = name;
            return;
        }
        holograms = new String[]{name};
    }

    /**
     * Sets the skin data in on fell swoop
     *
     * @param signature the skin signature
     * @param value     the skin value
     * @param skinName  the cosmetic name
     */
    public void setSkinData(String signature, String value, String skinName) {
        this.signature = signature;
        this.value = value;
        this.skinName = skinName;
    }

    public void setSkin(Player player) {
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
    }

    @SuppressWarnings("all")
    public Settings clone() {
        return new Settings(interactable, tunnelvision, resilient, direction, value, signature, skinName, holograms, customInteractableHologram, hideClickableHologram, pose);
    }
}
