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

package dev.foxikle.customnpcs.actions;

import dev.foxikle.customnpcs.actions.defaultImpl.*;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

/**
 * All supported types of actions
 * <p>
 * Scheduled for removal in 1.9.0, meaning configurations prior to 1.7 will
 * need to update to 1.7.x-1.8.x prior to updating to 1.9.0
 *
 * @deprecated
 */
@Getter
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "1.9.0")
public enum ActionType {

    /**
     * Represents running a command. The argument syntax is as follows:
     * label, args...
     */
    RUN_COMMAND(true, true, true, RunCommand.class),

    /**
     * Represents sending the player a message. The argument syntax is as follows:
     * message...
     */
    SEND_MESSAGE(true, true, true, SendMessage.class),

    /**
     * Represents sending the player a title. The argument syntax is as follows:
     * fade_in, stay, fade_out, title...
     */
    DISPLAY_TITLE(true, true, true, DisplayTitle.class),

    /**
     * Represents sending the player an action bar. The argument syntax is as follows:
     * message...
     */
    ACTION_BAR(true, true, true, ActionBar.class),

    /**
     * Represents starting/stopping following the player. There are no arguments.
     * @apiNote This action was removed in 1.7-pre3
     */
    @Deprecated
    TOGGLE_FOLLOWING(false, false, false, null),

    /**
     * Represents playing a sound for the player. The argument syntax is as follows:
     * pitch, volume, sound_name
     */
    PLAY_SOUND(true, true, true, PlaySound.class),

    /**
     * Represents teleporting the player. The argument syntax is as follows:
     * x, y, z, pitch, yaw
     */
    TELEPORT(true, true, true, Teleport.class),

    /**
     * Represents sending the player to a BungeeCord server. The argument syntax is as follows:
     * Case_sensitive_Server_Name
     */
    SEND_TO_SERVER(true, true, true, SendServer.class),

    /**
     * Represents giving an effect to the player. The argument syntax is as follows:
     * duration, strength, hide_particles, effect
     */
    ADD_EFFECT(true, true, true, GiveEffect.class),

    /**
     * Represents removing an effect to the player. The argument syntax is as follows:
     * effect
     */
    REMOVE_EFFECT(true, true, true, RemoveEffect.class),

    /**
     * Represents giving a xp to the player. The argument syntax is as follows:
     * amount, levels
     */
    GIVE_EXP(true, true, true, GiveXP.class),

    /**
     * Represents removing a xp to the player. The argument syntax is as follows:
     * amount, levels
     */
    REMOVE_EXP(true, true, true, RemoveXP.class);

    private final boolean editable;
    private final boolean duplicatable;
    private final boolean delayable;
    private final Class<? extends Action> actionClass;

    ActionType(boolean editable, boolean canDuplicate, boolean canDelay, Class<? extends Action> actionClass) {
        this.editable = editable;
        this.duplicatable = canDuplicate;
        this.delayable = canDelay;
        this.actionClass = actionClass;
    }
}
