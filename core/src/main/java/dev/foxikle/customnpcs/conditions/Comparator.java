/*
 * Copyright (c) 2026. Foxikle
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

import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A list of comparators used to compare the values and target values of conditions
 */
@Getter
public enum Comparator {
    /**
     * Represents the value being equal to the target value
     */
    EQUAL_TO("conditions.equal_to", NumericCondition.class, BooleanCondition.class, TextCondition.class),

    /**
     * Represents the value being equal to the target value
     */
    EQUAL_IGNORE_CASE("conditions.equal_ignore_case", TextCondition.class),

    /**
     * Compares the right-hand side as a regular expression, and checks the expression for matches.
     */
    REGEX_EQUAL("conditions.regex_equal", TextCondition.class),

    /**
     * Checks if the expression contains a value
     */
    CONTAINS("conditions.contains", TextCondition.class),

    /**
     * Checks if the expression starts with a value
     */
    STARTS_WITH("conditions.starts_with", TextCondition.class),

    /**
     * Checks if the expression ends with a value
     */
    ENDS_WITH("conditions.ends_with", TextCondition.class),

    /**
     * Represents the value being unequal to the target value
     */
    NOT_EQUAL_TO("conditions.not_equal_to", NumericCondition.class, BooleanCondition.class),

    /**
     * Represents the value being less than the target value
     */
    LESS_THAN("conditions.less_than", NumericCondition.class),

    /**
     * Represents the value being greater than the target value
     */
    GREATER_THAN("conditions.greater_than", NumericCondition.class),

    /**
     * Represents the value being less than or equal to the target value
     */
    LESS_THAN_OR_EQUAL_TO("conditions.less_than_or_equal_to", NumericCondition.class),

    /**
     * Represents the value being greater than or equal to the target value
     */
    GREATER_THAN_OR_EQUAL_TO("conditions.greater_than_or_equal_to", NumericCondition.class);

    private static final Map<Class<? extends Condition>, Set<Comparator>> BY_TYPE = new HashMap<>();

    static {
        for (Comparator comparator : values()) {
            for (Class<? extends Condition> type : comparator.supportedConditions) {
                BY_TYPE.computeIfAbsent(type, _ -> new HashSet<>()).add(comparator);
            }
        }
    }

    private final Class<? extends Condition>[] supportedConditions;
    private final String key;

    @SafeVarargs
    Comparator(String key, Class<? extends Condition>... supportedConditions) {
        this.key = key;
        this.supportedConditions = supportedConditions;
    }

    public static Set<Comparator> getSupportedConditions(Condition type) {
        return getSupportedConditions(type.getClass());
    }

    public static Set<Comparator> getSupportedConditions(Class<? extends Condition> type) {
        return BY_TYPE.getOrDefault(type, new HashSet<>());
    }
}
