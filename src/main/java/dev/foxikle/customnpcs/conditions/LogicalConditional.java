package dev.foxikle.customnpcs.conditions;

import dev.foxikle.customnpcs.CustomNPCs;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class LogicalConditional implements Conditional {
    private final Type type = Type.LOGICAL;
    private Comparator comparator;
    private Value value;
    private String target;

    public LogicalConditional(Comparator comparator, Value value, String target) {
        this.comparator = comparator;
        this.value = value;
        this.target = target;
    }

    @Override
    public boolean compute(Player player) {
        boolean value = false;
        switch (this.value) { //todo: add more
            case HAS_PERMISSION -> value = player.hasPermission(target);
            case HAS_EFFECT -> value = player.hasPotionEffect(PotionEffectType.getByName(target));
            case GAMEMODE -> value = player.getGameMode().equals(GameMode.valueOf(target));
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

    @Override
    public String toJson(){
        return CustomNPCs.getGson().toJson(this);
    }
    
    public static LogicalConditional of(String data) {
        return CustomNPCs.getGson().fromJson(data, LogicalConditional.class);
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
        this.target = targetValue;
    }

    @Override
    public Value getValue() {
        return this.value;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public Comparator getComparator() {
        return this.comparator;
    }
}
