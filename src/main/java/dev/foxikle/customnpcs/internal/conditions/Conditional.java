package dev.foxikle.customnpcs.internal.conditions;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import org.bukkit.entity.Player;

public interface Conditional {

    boolean compute(Player player);

    String toJson();

    static Conditional of(String data) {
        return CustomNPCs.getGson().fromJson(data, Conditional.class);
    }

    Value getValue();
    Comparator getComparator();

    Type getType();

    void setComparator(Comparator comparator);
    void setValue(Value value);
    void setTargetValue(String targetValue);
    String getTarget();
    enum Comparator {
        EQUAL_TO(true),
        NOT_EQUAL_TO(true),
        LESS_THAN(false),
        GREATER_THAN(false),
        LESS_THAN_OR_EQUAL_TO(false),
        GREATER_THAN_OR_EQUAL_TO(false);

        private final boolean strictlyLogical;
        Comparator(boolean strictlyLogical) {
            this.strictlyLogical = strictlyLogical;
        }

        public boolean isStrictlyLogical() {
            return strictlyLogical;
        }
    }

    enum Type {
        NUMERIC,
        LOGICAL
    }

    enum Value {
        // numeric
        EXP_LEVELS(false),
        EXP_POINTS(false),
        HEALTH(false),
        ABSORBTION(false),
        Y_COORD(false),
        X_COORD(false),
        Z_COORD(false),
        // logical
        HAS_EFFECT(true),
        HAS_PERMISSION(true),
        GAMEMODE(true),
        IS_FLYING(true),
        IS_SPRINTING(true),
        IS_SNEAKING(true),
        IS_FROZEN(true),
        IS_GLIDING(true);

        private final boolean isLogical;
        Value(boolean isLogical) {
            this.isLogical = isLogical;
        }

        public boolean isLogical() {
            return isLogical;
        }
    }
}
