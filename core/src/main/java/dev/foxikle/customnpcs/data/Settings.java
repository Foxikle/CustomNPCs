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

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * A class holding the data for an NPC's settings
 */
@Setter
public class Settings {

    @Getter boolean interactable = false;
    @Getter boolean tunnelvision = false;
    @Getter boolean resilient = true;
    @Getter double direction = 180;
    @Getter String value = "";
    @Getter String signature = "";
    String name = "not set";
    @Getter String skinName = "not set";
    @Getter boolean hideClickableHologram = false;
    @Getter String customInteractableHologram = "";


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
     * @param hideClickableHologram If the NPC's Clickable hologram should be hidden
     */
    public Settings(boolean interactable, boolean tunnelvision, boolean resilient, double direction, String value, String signature, String skinName, String name, String customInteractableHologram, boolean hideClickableHologram) {
        this.interactable = interactable;
        this.tunnelvision = tunnelvision;
        this.resilient = resilient;
        this.direction = direction;
        this.value = value;
        this.signature = signature;
        this.skinName = skinName;
        this.name = name;
        this.hideClickableHologram = hideClickableHologram;
        this.customInteractableHologram = customInteractableHologram;
    }

    /**
     * Creates a settings with defaults.
     */
    public Settings() {
        // default constructor
    }

    /**
     * gets the NPC's name in serialized minimessage format.
     * @return the NPC's serialized name
     */
    public String getName() {
        return name.replace("%empty%", "");
    }

    /**
     * Sets the name of the npc
     * @param name as a component
     */
    public void setName(Component name) {
        this.name = MiniMessage.miniMessage().serialize(name);
    }

    /**
     * Sets the name of the npc
     * @param name as a string
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the skin data in on fell swoop
     * @param signature the skin signature
     * @param value the skin value
     * @param skinName the cosmetic name
     */
    public void setSkinData(String signature, String value, String skinName) {
        this.signature = signature;
        this.value = value;
        this.skinName = skinName;
    }

    @SuppressWarnings("all")
    public Settings clone(){
        return new Settings(interactable, tunnelvision, resilient, direction, value, signature, skinName, name, customInteractableHologram, hideClickableHologram);
    }
}
