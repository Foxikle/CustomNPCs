package dev.foxikle.customnpcs.internal.conditions;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import org.bukkit.entity.Player;

public class NumericConditional implements Conditional {

    private final Type type = Type.NUMERIC;
    private Comparator comparator;
    private Value value;
    private double target;

    public NumericConditional(Comparator comparator, Value value, double target) {
        this.comparator = comparator;
        this.value = value;
        this.target = target;
    }

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
    @Override
    public String toJson(){
        return CustomNPCs.getGson().toJson(this);
    }

    public static NumericConditional of(String data) {
        return CustomNPCs.getGson().fromJson(data, NumericConditional.class);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setComparator(Comparator comparator) {
        this.comparator = comparator;
    }

    @Override
    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public void setTargetValue(String targetValue) {
        this.target = Double.parseDouble(targetValue);
    }

    @Override
    public Value getValue() {
        return this.value;
    }

    @Override
    public String getTarget() {
        return String.valueOf(target);
    }

    @Override
    public Comparator getComparator() {
        return this.comparator;
    }
}
