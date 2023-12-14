package dev.foxikle.customnpcs.data;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Settings {
    boolean interactable = false;
    boolean tunnelvision = false;
    boolean resilient = false;
    double direction = 180;
    String value = "";
    String signature = "";
    String name = "not set";
    String skinName = "not set";

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

    public Settings() {
        // defualt constructor
    }

    public boolean isInteractable() {
        return interactable;
    }

    public boolean isResilient() {
        return resilient;
    }

    public boolean isTunnelvision() {
        return tunnelvision;
    }

    public double getDirection() {
        return direction;
    }

    public String getName() {
        return name.replace("%empty%", "");
    }

    public String getSignature() {
        return signature;
    }

    public String getSkinName() {
        return skinName;
    }

    public String getValue() {
        return value;
    }



    public void setResilient(boolean resilient) {
        this.resilient = resilient;
    }

    public void setInteractable(boolean interactable) {
        this.interactable = interactable;
    }

    public void setTunnelvision(boolean tunnelvision) {
        this.tunnelvision = tunnelvision;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setName(Component name) {
        this.name = MiniMessage.miniMessage().serialize(name);
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setSkinName(String skinName) {
        this.skinName = skinName;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
