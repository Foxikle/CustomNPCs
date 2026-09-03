/*
 * Copyright (c) 2024-2026. Foxikle
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.bukkit.entity.Player;

/**
 * The interface to represent a comparison
 */
public interface Condition {

    Codec<Condition> CODEC = Codec.Enum(Type.class).unionType(Type::getCodec, Condition::getType);

    /**
     * Computes the condition to determine if the action should be executed
     *
     * @param player The player to fetch data from
     * @return if the action should be executed
     */
    boolean compute(Player player);


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
    void setTarget(String targetValue);

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
    @AllArgsConstructor
    enum Type {
        /**
         * Represents a comparison between a Value and a target value that can be any numeric value.
         *
         * @see Value
         */
        NUMERIC(NumericCondition.CODEC),

        /**
         * Represents a comparison between two strings
         */
        TEXT(TextCondition.CODEC),

        /**
         * Represents a boolean value condition
         *
         * @see Value
         */
        LOGICAL(BooleanCondition.CODEC); // name has to be logical since Enum codecs serialize by name, not ordinal

        @Getter
        private final StructCodec<? extends Condition> codec;
    }

    /**
     * A list of values the plugin can compare
     */
    enum Value {
        // numeric
        EXP_LEVELS("conditions.xp_levels", NumericCondition.class),
        EXP_POINTS("conditions.xp_points", NumericCondition.class),
        HEALTH("conditions.health", NumericCondition.class),
        ABSORPTION("conditions.absorption", NumericCondition.class),
        Y_COORD("conditions.y_coord", NumericCondition.class),
        X_COORD("conditions.x_coord", NumericCondition.class),
        Z_COORD("conditions.z_coord", NumericCondition.class),


        // boolean
        HAS_EFFECT("conditions.has_effect", BooleanCondition.class),
        HAS_PERMISSION("conditions.has_permission", BooleanCondition.class),
        IS_FLYING("conditions.is_flying", BooleanCondition.class),
        IS_SPRINTING("conditions.is_sprinting", BooleanCondition.class),
        IS_SNEAKING("conditions.is_sneaking", BooleanCondition.class),
        IS_FROZEN("conditions.is_frozen", BooleanCondition.class),
        IS_GLIDING("conditions.is_gliding", BooleanCondition.class),

        // text based
        GAMEMODE("conditions.gamemode", TextCondition.class),
        USERNAME("conditions.username", TextCondition.class),
        UUID("conditions.uuid", TextCondition.class),
        CLIENT_BRAND("conditions.client_brand", TextCondition.class);


        private final String key;
        private final Class<? extends Condition>[] supportedTypes;


        Value(String key, Class<? extends Condition>... supportedTypes) {
            this.key = key;
            this.supportedTypes = supportedTypes;
        }

        public String getTranslationKey() {
            return key;
        }

    }
}
