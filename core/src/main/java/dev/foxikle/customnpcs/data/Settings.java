package dev.foxikle.customnpcs.data;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.ApiStatus;

/**
 * A class holding the data for an NPC's settings
 */
public class Settings {

    @Setter @Getter
    boolean interactable = false;

    @Setter @Getter
    boolean tunnelvision = false;


    @Setter @Getter
    boolean resilient = true;

    @Setter @Getter
    double direction = 180;


    @Setter @Getter
    String value = "";

    @Setter @Getter
    String signature = "";

    String name = "not set";

    @Setter @Getter
    String skinName = "not set";

    /**
     * Should this NPC not have a clickable Hologram?
     */
    @Setter @Getter
    boolean hideClickableHologram = false;

    @Setter @Getter
    String customInteractableHologram = "";

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
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.7")
    public Settings(boolean interactable, boolean tunnelvision, boolean resilient, double direction, String value, String signature, String skinName, String name) {
        this.interactable = interactable;
        this.tunnelvision = tunnelvision;
        this.resilient = resilient;
        this.direction = direction;
        this.value = value;
        this.signature = signature;
        this.skinName = skinName;
        this.name = name;
        this.customInteractableHologram = "";
        this.hideClickableHologram = false;
    }

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
        // defualt constructor
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

    public Settings clone(){
        return new Settings(interactable, tunnelvision, resilient, direction, value, signature, skinName, name, customInteractableHologram, hideClickableHologram);
    }
}
