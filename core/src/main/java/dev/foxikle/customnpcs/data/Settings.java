package dev.foxikle.customnpcs.data;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * A class holding the data for an NPC's settings
 */
public class Settings {

    /**
     *
     */
    boolean interactable = false;

    /**
     *
     */
    boolean tunnelvision = false;

    /**
     *
     */
    boolean resilient = false;

    /**
     *
     */
    double direction = 180;

    /**
     *
     */
    String value = "";

    /**
     *
     */
    String signature = "";

    /**
     *
     */
    String name = "not set";

    /**
     *
     */
    String skinName = "not set";

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
    public Settings(boolean interactable, boolean tunnelvision, boolean resilient, double direction, String value, String signature, String skinName, String name) {
        this.interactable = interactable;
        this.tunnelvision = tunnelvision;
        this.resilient = resilient;
        this.direction = direction;
        this.value = value;
        this.signature = signature;
        this.skinName = skinName;
        this.name = name;
    }

    /**
     * Creates a settings with defaults.
     */
    public Settings() {
        // defualt constructor
    }

    /**
     * Determines if the NPC runs the actions on interaction
     * @return if the NPC is interactable
     */
    public boolean isInteractable() {
        return interactable;
    }

    /**
     * Determines in the NPC will persist
     * @return if the NPC will persist on server restart
     */
    public boolean isResilient() {
        return resilient;
    }

    /**
     * Determines if the NPC will look at players
     * @return if the npc will ignore nearby players (true = no movement)
     */
    public boolean isTunnelvision() {
        return tunnelvision;
    }

    /**
     * Gets the heading the NPC faces
     * @return gets the yaw
     */
    public double getDirection() {
        return direction;
    }

    /**
     * gets the NPC's name in serialized minimessage format.
     * @return the NPC's serialized name
     */
    public String getName() {
        return name.replace("%empty%", "");
    }

    /**
     * Gets the skin's signature
     * @return the npc's skin signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Gets the name of the skin
     * @return the name of the skin
     */
    public String getSkinName() {
        return skinName;
    }

    /**
     * Gets the value of the NPC's skin
     * @return the value of the skin
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the NPC's resiliency
     * @param resilient if the npc should persist on restarts
     */
    public void setResilient(boolean resilient) {
        this.resilient = resilient;
    }

    /**
     * Sets if the npc will execute actions
     * @param interactable if the npc should be clickable
     */
    public void setInteractable(boolean interactable) {
        this.interactable = interactable;
    }

    /**
     * Sets if the npc will ignore players around it
     * @param tunnelvision if the npc will ignore players
     */
    public void setTunnelvision(boolean tunnelvision) {
        this.tunnelvision = tunnelvision;
    }

    /**
     * Sets the direction of the NPC to look when a player isn't nearby
     * @param direction to face
     */
    public void setDirection(double direction) {
        this.direction = direction;
    }

    /**
     * Sets the name of the NPC in serialized MiniMessage format
     * @param name the serialized Adventure component
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the name of the npc
     * @param name as a component
     */
    public void setName(Component name) {
        this.name = MiniMessage.miniMessage().serialize(name);
    }

    /**
     * Set the signature of the skin
     * @param signature the signature
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * Sets the name of the skin
     * @param skinName The name of the skin
     */
    public void setSkinName(String skinName) {
        this.skinName = skinName;
    }

    /**
     * Sets the value of the npc skin
     * @param value the value of the NPC's skin
     */
    public void setValue(String value) {
        this.value = value;
    }
}
