package dev.foxikle.customnpcs.conditions;

/**
 * Represents how the conditions should be computed
 */
public enum Selector {
    /**
     * If ALL the conditions must be true for the action to be executed
     */
    ALL,

    /**
     * if at least ONE of the conditions must be met for the action to be executed
     */
    ONE
}
