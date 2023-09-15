package dev.foxikle.customnpcs.api.conditions;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * The object representing two non-numeric values
 */
public class LogicalConditional implements Conditional {
    private final Type type = Type.LOGICAL;
    private Comparator comparator;
    private Value value;
    private String target;

    /**
     *
     * @param comparator the comparator to use
     * @param value the value to compare
     * @param target the target to compare to
     * @see dev.foxikle.customnpcs.api.conditions.Conditional.Value
     * @see dev.foxikle.customnpcs.api.conditions.Conditional.Comparator
     */
    public LogicalConditional(Comparator comparator, Value value, String target) {
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
        boolean value = false;
        switch (this.value) {
            case HAS_PERMISSION -> value = player.hasPermission(target);
            case HAS_EFFECT -> value = player.hasPotionEffect(PotionEffectType.getByName(target));
            case GAMEMODE -> value = player.getGameMode().equals(GameMode.valueOf(target));
            case IS_FLYING -> value = player.isFlying();
            case IS_SPRINTING -> value = player.isSprinting();
            case IS_SNEAKING -> value = player.isSneaking();
            case IS_FROZEN -> value = player.isFrozen();
            case IS_GLIDING -> value = player.isGliding();
        }
        switch (comparator) {
            case EQUAL_TO -> {
                return value;
            }
            case NOT_EQUAL_TO -> {
                return !value;
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
    public static LogicalConditional of(String data) {
        return CustomNPCs.getGson().fromJson(data, LogicalConditional.class);
    }

    /**
     * Gets the type of condition
     * @return the condition type
     * @see dev.foxikle.customnpcs.api.conditions.Conditional.Type
     */
    @Override
    public Type getType() {
        return type;
    }

    /**
     * Sets the comparator of this condition
     * @param comparator the comparator to compare the value and target value
     * @see dev.foxikle.customnpcs.api.conditions.Conditional.Comparator
     */
    @Override
    public void setComparator(Comparator comparator) {
        this.comparator = comparator;
    }

    /**
     * Sets the value of this condition
     * @param value the value to compare
     * @see dev.foxikle.customnpcs.api.conditions.Conditional.Value
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
        this.target = targetValue;
    }

    /**
     * Gets the value the condition is comparing
     * @see dev.foxikle.customnpcs.api.conditions.Conditional.Value
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
        return target;
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
}
