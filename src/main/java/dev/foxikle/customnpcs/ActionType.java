package dev.foxikle.customnpcs;

import javax.annotation.Nullable;

/**
 * All supported types of actions
 */
public enum ActionType {

    /**
     * Represents running a command. The argument syntax is as follows:
     * label, args...
     */
    RUN_COMMAND,

    /**
     * Represents sending the player a message. The argument syntax is as follows:
     * message...
     */
    SEND_MESSAGE,

    /**
     * Represents sending the player a title. The argument syntax is as follows:
     * fade_in, stay, fade_out, title...
     */
    DISPLAY_TITLE,

    /**
     * Represents sending the player an action bar. The argument syntax is as follows:
     * message...
     */
    ACTION_BAR,

    /**
     * Represents starting/stopping following the player. There are no arugments.
     */
    TOGGLE_FOLLOWING,

    /**
     * Represents playing a sound for the player. The argument syntax is as follows:
     * pitch, volume, sound_name
     */
    PLAY_SOUND,

    /**
     * Represents teleporting the player. The argument syntax is as follows:
     * x, y, z, pitch, yaw
     */
    TELEPORT,

    /**
     * Represents sending the player to a BungeeCord server. The argument syntax is as follows:
     * Case_sensitive_Server_Name
     */
    SEND_TO_SERVER,

    /**
     * Represents giving an effect to the player. The argument syntax is as follows:
     * duration, strength, hide_particles, effect
     */
    ADD_EFFECT,

    /**
     * Represents removing an effect to the player. The argument syntax is as follows:
     * effect
     */
    REMOVE_EFFECT,

    /**
     * Represents giving a xp to the player. The argument syntax is as follows:
     * amount, levels
     */
    GIVE_EXP,

    /**
     * Represents removing a xp to the player. The argument syntax is as follows:
     * amount, levels
     */
    REMOVE_EXP,


}
