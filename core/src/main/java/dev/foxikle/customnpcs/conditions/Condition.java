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

package dev.foxikle.customnpcs.conditions;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * The interface to represent a comparison
 */
public interface Condition {

    /**
     * Computes the condition to determine if the action should be executed
     *
     * @param player The player to fetch data from
     * @return if the action should be executed
     */
    boolean compute(Player player);

    /**
     * Serializes the condition to json using Gson
     *
     * @return the serialized condition
     */
    String toJson();

    /**
     * @param data the serialized condition
     * @return the condition from the json
     */
    static Condition of(String data) {
        return CustomNPCs.getGson().fromJson(data, Condition.class);
    }

    /**
     * Gets the value the condition is comparing
     *
     * @return the value the condition is comparing
     * @see Value
     */
    Value getValue();

    /**
     * Gets the comparator the condition uses to compare the value and target value.
     *
     * @return the comparator
     * @see Comparator
     */
    Comparator getComparator();

    /**
     * Gets the type of condition
     *
     * @return the condition type
     * @see Type
     */
    Type getType();

    /**
     * Sets the comparator of this condition
     *
     * @param comparator the comparator to compare the value and target value
     * @see Comparator
     */
    void setComparator(Comparator comparator);

    /**
     * Sets the value of this condition
     *
     * @param value the value to compare
     * @see Value
     */
    void setValue(Value value);

    /**
     * Sets the target value of this condition
     *
     * @param targetValue the target value
     */
    void setTargetValue(String targetValue);

    /**
     * Gets the target of the condition
     *
     * @return returns the target value
     */
    String getTarget();

    /**
     * Clones this conditional object
     *
     * @return the cloned object
     */
    Condition clone();

    /**
     * A list of comparator types
     */
    enum Type {
        /**
         * Represents a comparison between a Value and a target value that can be any numeric value.
         *
         * @see Value
         */
        NUMERIC,

        /**
         * Represents a comparison between a Value and a target value with a finite number of possibilities
         *
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
        EXP_LEVELS(false, "customnpcs.conditions.xp_levels"),

        /**
         * Represents the player's experience points
         */
        EXP_POINTS(false, "customnpcs.conditions.xp_points"),

        /**
         * Represents the player's health
         */
        HEALTH(false, "customnpcs.conditions.health"),

        /**
         * Represents the player's absorption
         */
        ABSORPTION(false, "customnpcs.conditions.absorption"),

        /**
         * Represents the player's Y coordinate
         */
        Y_COORD(false, "customnpcs.conditions.y_coord"),

        /**
         * Represents the player's X coordinate
         */
        X_COORD(false, "customnpcs.conditions.x_coord"),

        /**
         * Represents the player's Z coordinate
         */
        Z_COORD(false, "customnpcs.conditions.z_coord"),


        // logical
        /**
         * Represents if the player has an effect
         */
        HAS_EFFECT(true, "customnpcs.conditions.has_effect"),

        /**
         * Represents if the player has a permission node
         */
        HAS_PERMISSION(true, "customnpcs.conditions.has_permission"),

        /**
         * Represents if the player is in the gamemode
         */
        GAMEMODE(true, "customnpcs.conditions.gamemode"),

        /**
         * Represents if the player is flying
         */
        IS_FLYING(true, "customnpcs.conditions.is_flying"),

        /**
         * Represents if the player is sprinting
         */
        IS_SPRINTING(true, "customnpcs.conditions.is_sprinting"),

        /**
         * Represents if the player is sneaking
         */
        IS_SNEAKING(true, "customnpcs.conditions.is_sneaking"),

        /**
         * Represents if the player is frozen
         */
        IS_FROZEN(true, "customnpcs.conditions.is_frozen"),

        /**
         * Represents if the player is gliding
         */
        IS_GLIDING(true, "customnpcs.conditions.is_gliding");


        /**
         * -- GETTER --
         *  Determines if the value is considered 'logical'
         *
         * @return if the value is logical
         */
        @Getter
        private final boolean isLogical;
        private final String key;

        /**
         * The constructor for the Value
         *
         * @param isLogical if the value is considered 'logical'
         * @param key       The translation key for the value
         */
        Value(boolean isLogical, String key) {
            this.isLogical = isLogical;
            this.key = key;
        }

        public String getTranslationKey() {
            return key;
        }

    }
}
