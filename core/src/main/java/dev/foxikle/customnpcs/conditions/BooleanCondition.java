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
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

@Setter
@Getter
public class BooleanCondition implements Condition {

    public static final StructCodec<? extends Condition> CODEC = StructCodec.struct(
            "comparator", Codec.Enum(Comparator.class), Condition::getComparator,
            "value", Codec.Enum(Value.class), Condition::getValue,
            "target", Codec.STRING, Condition::getTarget,
            (comparator, value, s) -> {
                if (value == Value.GAMEMODE) {
                    return new TextCondition(comparator, value, s, false);
                }
                return new BooleanCondition(comparator, value, s);
            }
    );

    private final Type type = Type.LOGICAL;
    private Comparator comparator;
    private Value value;
    private String target;

    public BooleanCondition(Comparator comparator, Value value, String target) {
        this.comparator = comparator;
        this.value = value;
        this.target = target;
    }

    public static BooleanCondition of(String data) {
        return CustomNPCs.getGson().fromJson(data, BooleanCondition.class);
    }

    @Override
    public boolean compute(Player player) {
        boolean value = false;
        switch (this.value) {
            case HAS_PERMISSION -> value = player.hasPermission(target);
            case HAS_EFFECT ->
                    value = player.hasPotionEffect(Objects.requireNonNull(PotionEffectType.getByName(target)));
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

    @Override
    public Condition clone() {
        return new BooleanCondition(comparator, value, target);
    }
}
