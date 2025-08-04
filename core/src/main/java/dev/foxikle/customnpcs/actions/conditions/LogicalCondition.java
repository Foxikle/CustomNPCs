/*
 * Copyright (c) 2024-2025. Foxikle
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

package dev.foxikle.customnpcs.actions.conditions;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

/**
 * The object representing two non-numeric values
 */
public class LogicalCondition implements Condition {
    private final Type type = Type.LOGICAL;
    private Comparator comparator;
    private Value value;
    private String target;

    /**
     * @param comparator the comparator to use
     * @param value      the value to compare
     * @param target     the target to compare to
     * @see Value
     * @see Comparator
     */
    public LogicalCondition(Comparator comparator, Value value, String target) {
        this.comparator = comparator;
        this.value = value;
        this.target = target;
    }

    /**
     * Computes the condition to determine if the action should be executed
     *
     * @param player The player to fetch data from
     * @return if the action should be executed
     */
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

    /**
     * Serializes the condition to json using Gson
     *
     * @return the serialized condition
     */
    @Override
    public String toJson() {
        return CustomNPCs.getGson().toJson(this);
    }

    /**
     * @param data the serialized condition
     * @return the condition from the json
     */
    public static LogicalCondition of(String data) {
        return CustomNPCs.getGson().fromJson(data, LogicalCondition.class);
    }

    /**
     * Gets the type of condition
     *
     * @return the condition type
     * @see Type
     */
    @Override
    public Type getType() {
        return type;
    }

    /**
     * Sets the comparator of this condition
     *
     * @param comparator the comparator to compare the value and target value
     * @see Comparator
     */
    @Override
    public void setComparator(Comparator comparator) {
        this.comparator = comparator;
    }

    /**
     * Sets the value of this condition
     *
     * @param value the value to compare
     * @see Value
     */
    @Override
    public void setValue(Value value) {
        this.value = value;
    }

    /**
     * Sets the target value of this condition
     *
     * @param targetValue the target value
     */
    @Override
    public void setTargetValue(String targetValue) {
        this.target = targetValue;
    }

    /**
     * Gets the value the condition is comparing
     *
     * @return the value the condition is comparing
     * @see Value
     */
    @Override
    public Value getValue() {
        return this.value;
    }

    /**
     * Gets the target of the condition
     *
     * @return returns the target value
     */
    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public Condition clone() {
        try {
            return (LogicalCondition) super.clone();
        } catch (CloneNotSupportedException e) {
            return new LogicalCondition(comparator, value, target);
        }
    }

    /**
     * Gets the comparator the condition uses to compare the value and target value.
     *
     * @return the comparator
     * @see Comparator
     */
    @Override
    public Comparator getComparator() {
        return this.comparator;
    }
}
