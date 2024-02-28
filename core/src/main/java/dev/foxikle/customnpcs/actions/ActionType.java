package dev.foxikle.customnpcs.actions;

import lombok.Getter;

/**
 * All supported types of actions
 */
@Getter
public enum ActionType {

    /**
     * Represents running a command. The argument syntax is as follows:
     * label, args...
     */
    RUN_COMMAND(true, true, true),

    /**
     * Represents sending the player a message. The argument syntax is as follows:
     * message...
     */
    SEND_MESSAGE(true, true, true),

    /**
     * Represents sending the player a title. The argument syntax is as follows:
     * fade_in, stay, fade_out, title...
     */
    DISPLAY_TITLE(true, true, true),

    /**
     * Represents sending the player an action bar. The argument syntax is as follows:
     * message...
     */
    ACTION_BAR(true, true, true),

    /**
     * Represents starting/stopping following the player. There are no arugments.
     */
    TOGGLE_FOLLOWING(false, false, false),

    /**
     * Represents playing a sound for the player. The argument syntax is as follows:
     * pitch, volume, sound_name
     */
    PLAY_SOUND(true, true, true),

    /**
     * Represents teleporting the player. The argument syntax is as follows:
     * x, y, z, pitch, yaw
     */
    TELEPORT(true, true, true),

    /**
     * Represents sending the player to a BungeeCord server. The argument syntax is as follows:
     * Case_sensitive_Server_Name
     */
    SEND_TO_SERVER(true, true, true),

    /**
     * Represents giving an effect to the player. The argument syntax is as follows:
     * duration, strength, hide_particles, effect
     */
    ADD_EFFECT(true, true, true),

    /**
     * Represents removing an effect to the player. The argument syntax is as follows:
     * effect
     */
    REMOVE_EFFECT(true, true, true),

    /**
     * Represents giving a xp to the player. The argument syntax is as follows:
     * amount, levels
     */
    GIVE_EXP(true, true, true),

    /**
     * Represents removing a xp to the player. The argument syntax is as follows:
     * amount, levels
     */
    REMOVE_EXP(true, true, true);

    private final boolean editable;
    private final boolean duplicatable;
    private final boolean delayable;

    ActionType(boolean editable, boolean canDubplicate, boolean canDelay) {
        this.editable = editable;
        this.duplicatable = canDubplicate;
        this.delayable = canDelay;
    }
}
