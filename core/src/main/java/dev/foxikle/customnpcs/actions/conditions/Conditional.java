package dev.foxikle.customnpcs.actions.conditions;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * The interface to represent a comparison
 */
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
         * Represents the value being inequal to the target value
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
         * Represents the player's absorbtion
         */
        ABSORBTION(false),

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
         * @param isLogical if the value is considdered 'logical'
         */
        Value(boolean isLogical) {
            this.isLogical = isLogical;
        }

        /**
         * Determines if the value is considdered 'logical'
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
