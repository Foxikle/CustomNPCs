package dev.foxikle.customnpcs.actions.conditions;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import org.bukkit.entity.Player;

/**
 * The object representing a comparison of two numeric values
 */
public class NumericConditional implements Conditional {

    private final Type type = Type.NUMERIC;
    private Comparator comparator;
    private Value value;
    private double target;

    /**
     *
     * @param comparator the comparator to use
     * @param value the value to compare
     * @param target the target to compare to
     * @see Conditional.Value
     * @see Conditional.Comparator
     */
    public NumericConditional(Comparator comparator, Value value, double target) {
        this.comparator = comparator;
        this.value = value;
        this.target = target;
    }

    /**
     * Computes the condition to determine if the action should be executed
     * @param player The player to fetch data from
     * @return if the action should be executed
     */
    @Override
    public boolean compute(Player player) {
        double value = 0;
        switch (this.value) {
            case X_COORD -> value = player.getLocation().x();
            case Y_COORD -> value = player.getLocation().y();
            case Z_COORD -> value = player.getLocation().z();
            case EXP_LEVELS -> value = player.getLevel();
            case EXP_POINTS -> value = player.getExp();
        }
        switch (comparator) {
            case EQUAL_TO -> {
                return value == target;
            }
            case NOT_EQUAL_TO -> {
                return value != target;
            }
            case LESS_THAN -> {
                return value < target;
            }
            case LESS_THAN_OR_EQUAL_TO -> {
                return value <= target;
            }
            case GREATER_THAN -> {
                return value > target;
            }
            case GREATER_THAN_OR_EQUAL_TO -> {
                return value >= target;
            }
        }
        return false;
    }

    /**
     * Serializes the condition to json using Gson
     * @return the serialized condition
     */
    @Override
    public String toJson(){
        return CustomNPCs.getGson().toJson(this);
    }

    /**
     *
     * @param data the serialized condition
     * @return the condition from the json
     */
    public static NumericConditional of(String data) {
        return CustomNPCs.getGson().fromJson(data, NumericConditional.class);
    }

    /**
     * Gets the type of condition
     * @return the condition type
     * @see Conditional.Type
     */
    @Override
    public Type getType() {
        return type;
    }

    /**
     * Sets the comparator of this condition
     * @param comparator the comparator to compare the value and target value
     * @see Conditional.Comparator
     */
    @Override
    public void setComparator(Comparator comparator) {
        this.comparator = comparator;
    }

    /**
     * Sets the value of this condition
     * @param value the value to compare
     * @see Conditional.Value
     */
    @Override
    public void setValue(Value value) {
        this.value = value;
    }

    /**
     * Sets the target value of this condition
     * @param targetValue the target value
     */
    @Override
    public void setTargetValue(String targetValue) {
        this.target = Double.parseDouble(targetValue);
    }

    /**
     * Gets the value the condition is comparing
     * @see Conditional.Value
     * @return the value the condition is comparing
     */
    @Override
    public Value getValue() {
        return this.value;
    }

    /**
     * Gets the target of the condition
     * @return returns the target value
     */
    @Override
    public String getTarget() {
        return String.valueOf(target);
    }

    /**
     * Gets the comparator the condition uses to compare the value and target value.
     * @return the comparator
     * @see Comparator
     */
    @Override
    public Comparator getComparator() {
        return this.comparator;
    }

    @Override
    public Conditional clone() {
        return new NumericConditional(comparator, value, target);
    }
}
