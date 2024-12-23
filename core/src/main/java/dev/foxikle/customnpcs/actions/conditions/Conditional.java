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

package dev.foxikle.customnpcs.actions.conditions;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

/**
 * The interface to represent a comparison
 * @deprecated See {@link Condition}
 */
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "1.8.0")
public interface Conditional {

    /**
     * Computes the condition to determine if the action should be executed
     * @param player The player to fetch data from
     * @return if the action should be executed
     */
    boolean compute(Player player);

    /**
     * Serializes the condition to json using Gson
     * @return the serialized condition
     */
    String toJson();

    /**
     *
     * @param data the serialized condition
     * @return the condition from the json
     */
    static Conditional of(String data) {
        return CustomNPCs.getGson().fromJson(data, Conditional.class);
    }

    /**
     * Gets the value the condition is comparing
     * @see Value
     * @return the value the condition is comparing
     */
    Value getValue();

    /**
     * Gets the comparator the condition uses to compare the value and target value.
     * @return the comparator
     * @see Comparator
     */
    Comparator getComparator();

    /**
     * Gets the type of condition
     * @return the condition type
     * @see Type
     */
    Type getType();

    /**
     * Sets the comparator of this condition
     * @param comparator the comparator to compare the value and target value
     * @see Comparator
     */
    void setComparator(Comparator comparator);

    /**
     * Sets the value of this condition
     * @param value the value to compare
     * @see Value
     */
    void setValue(Value value);

    /**
     * Sets the target value of this condition
     * @param targetValue the target value
     */
    void setTargetValue(String targetValue);

    /**
     * Gets the target of the condition
     * @return returns the target value
     */
    String getTarget();

    /**
     * Clones this conditional object
     * @return the cloned object
     */
    Conditional clone();

    /**
     * A list of comparators used to compare the values and target values of conditions
     */
    @Getter
    enum Comparator {
        /**
         * Represents the value being equal to the target value
         */
        EQUAL_TO(true),

        /**
         * Represents the value being unequal to the target value
         */
        NOT_EQUAL_TO(true),

        /**
         * Represents the value being less than the target value
         */
        LESS_THAN(false),

        /**
         * Represents the value being greater than the target value
         */
        GREATER_THAN(false),

        /**
         * Represents the value being less than or equal to the target value
         */
        LESS_THAN_OR_EQUAL_TO(false),

        /**
         * Represents the value being greater than or equal to the target value
         */
        GREATER_THAN_OR_EQUAL_TO(false);

        private final boolean strictlyLogical;

        /**
         * Constructor for the Comparator
         * @param strictlyLogical if the comparator is to only be used on logical parameters
         */
        Comparator(boolean strictlyLogical) {
            this.strictlyLogical = strictlyLogical;
        }

    }

    /**
     * A list of comparator types
     */
    enum Type {
        /**
         * Represents a comparison between a Value and a target value that can be any numeric value.
         * @see Value
         */
        NUMERIC,

        /**
         * Represents a comparison between a Value and a target value with a finite number of possibilities
         * @see Value
         */
        LOGICAL
    }

    /**
     * A list of values the plugin can compare
     */
    enum Value {
        // numeric
        /**
         * Represents the player's experience levels
         */
        EXP_LEVELS(false),

        /**
         * Represents the player's experience points
         */
        EXP_POINTS(false),

        /**
         * Represents the player's health
         */
        HEALTH(false),

        /**
         * Represents the player's absorption
         * @deprecated - Misspelled, see {@link Conditional.Value#ABSORPTION}
         */
        @ApiStatus.ScheduledForRemoval(inVersion = "1.7")
        @Deprecated
        ABSORBTION(false),

        /**
         * Represents the player's absorption
         */
        ABSORPTION(false),

        /**
         * Represents the player's Y coordinate
         */
        Y_COORD(false),

        /**
         * Represents the player's X coordinate
         */
        X_COORD(false),

        /**
         * Represents the player's Z coordinate
         */
        Z_COORD(false),


        // logical
        /**
         * Represents if the player has an effect
         */
        HAS_EFFECT(true),

        /**
         * Represents if the player has a permission node
         */
        HAS_PERMISSION(true),

        /**
         * Represents if the player is in the gamemode
         */
        GAMEMODE(true),

        /**
         * Represents if the player is flying
         */
        IS_FLYING(true),

        /**
         * Represents if the player is sprinting
         */
        IS_SPRINTING(true),

        /**
         * Represents if the player is sneaking
         */
        IS_SNEAKING(true),

        /**
         * Represents if the player is frozen
         */
        IS_FROZEN(true),

        /**
         * Represents if the player is gliding
         */
        IS_GLIDING(true);



        private final boolean isLogical;

        /**
         * The constructor for the Value
         * @param isLogical if the value is considered 'logical'
         */
        Value(boolean isLogical) {
            this.isLogical = isLogical;
        }

        /**
         * Determines if the value is considered 'logical'
         * @return if the value is logical
         */
        public boolean isLogical() {
            return isLogical;
        }
    }

    /**
     * Represents if how the conditions should be computed
     */
    enum SelectionMode {
        /**
         * If ALL the conditions must be true for the action to be executed
         */
        ALL,

        /**
         * if at least ONE of the conditions must be met for the action to be executed
         */
        ONE
    }
}
