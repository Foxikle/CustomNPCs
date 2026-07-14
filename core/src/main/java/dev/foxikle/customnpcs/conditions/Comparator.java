package dev.foxikle.customnpcs.conditions;

import lombok.Getter;

/**
 * A list of comparators used to compare the values and target values of conditions
 */
@Getter
public enum Comparator {
    /**
     * Represents the value being equal to the target value
     */
    EQUAL_TO(true, "customnpcs.conditions.equal_to"),

    /**
     * Represents the value being unequal to the target value
     */
    NOT_EQUAL_TO(true, "customnpcs.conditions.not_equal_to"),

    /**
     * Represents the value being less than the target value
     */
    LESS_THAN(false, "customnpcs.conditions.less_than"),

    /**
     * Represents the value being greater than the target value
     */
    GREATER_THAN(false, "customnpcs.conditions.greater_than"),

    /**
     * Represents the value being less than or equal to the target value
     */
    LESS_THAN_OR_EQUAL_TO(false, "customnpcs.conditions.less_than_or_equal_to"),

    /**
     * Represents the value being greater than or equal to the target value
     */
    GREATER_THAN_OR_EQUAL_TO(false, "customnpcs.conditions.greater_than_or_equal_to");

    private final boolean strictlyLogical;
    private final String key;

    /**
     * Constructor for the Comparator
     *
     * @param strictlyLogical if the comparator is to only be used on logical parameters
     * @param key             the translation key
     */
    Comparator(boolean strictlyLogical, String key) {
        this.strictlyLogical = strictlyLogical;
        this.key = key;
    }
}
