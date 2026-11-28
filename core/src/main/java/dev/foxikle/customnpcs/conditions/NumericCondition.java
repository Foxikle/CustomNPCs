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
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
@Setter
public class NumericCondition implements Condition {

    public static final StructCodec<NumericCondition> CODEC = StructCodec.struct(
        "comparator", Codec.Enum(Comparator.class), Condition::getComparator,
        "value", Codec.Enum(Value.class), Condition::getValue,
        "uuid", Codec.UUID_STRING.optional(), Condition::getUuid,
        "target", Codec.DOUBLE, NumericCondition::getTypedTarget,
        NumericCondition::new
    );

    private final Type type = Type.NUMERIC;
    private final UUID uuid;
    private Comparator comparator;
    private Value value;
    private double target;


    public NumericCondition(Comparator comparator, Value value, UUID uuid, double target) {
        this.comparator = comparator;
        this.value = value;
        this.target = target;
        if (uuid == null) uuid = UUID.randomUUID();
        this.uuid = uuid;
    }

    public static NumericCondition of(String data) {
        return CustomNPCs.getGson().fromJson(data, NumericCondition.class);
    }

    @Override
    public boolean compute(Player player) {
        double value = switch (this.value) {
            case X_COORD -> player.getLocation().x();
            case Y_COORD -> player.getLocation().y();
            case Z_COORD -> player.getLocation().z();
            case EXP_LEVELS -> player.getLevel();
            case EXP_POINTS -> player.getExp();
            default -> 0;
        };
        return switch (comparator) {
            case EQUAL_TO -> value == target;
            case NOT_EQUAL_TO -> value != target;
            case LESS_THAN -> value < target;
            case LESS_THAN_OR_EQUAL_TO -> value <= target;
            case GREATER_THAN -> value > target;
            case GREATER_THAN_OR_EQUAL_TO -> value >= target;
            default -> false;
        };
    }

    @Override
    public String getTarget() {
        return String.valueOf(target);
    }

    @Override
    public void setTarget(String targetValue) {
        this.target = Double.parseDouble(targetValue);
    }

    private double getTypedTarget() {
        return target;
    }

    @Override
    public Condition clone() {
        return new NumericCondition(comparator, value, uuid, target);
    }
}