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
import lombok.Setter;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

@Getter
@Setter
public class TextCondition implements Condition {

    public static final StructCodec<TextCondition> CODEC = StructCodec.struct(
            "comparator", Codec.Enum(Comparator.class), Condition::getComparator,
            "value", Codec.Enum(Value.class), Condition::getValue,
            "target", Codec.STRING, TextCondition::getTarget,
            "inverted", Codec.BOOLEAN.optional(false), TextCondition::isInverted,
            TextCondition::new
    );

    private final Type type = Type.TEXT;
    private Comparator comparator;
    private Value value;
    private String target;
    private boolean inverted;

    public TextCondition(Comparator comparator, Value value, String target, boolean inverted) {
        this.comparator = comparator;
        this.value = value;
        this.target = target;
        this.inverted = inverted;
    }

    @Override
    public boolean compute(Player player) {
        String value = switch (this.value) {
            case GAMEMODE -> player.getGameMode().name();
            default -> "";
        };
        boolean computed = switch (comparator) {
            case EQUAL_TO -> value.equals(target);
            case REGEX_EQUAL -> Pattern.matches(target, value);
            case EQUAL_IGNORE_CASE -> value.equalsIgnoreCase(target);
            case CONTAINS -> value.contains(target);
            case STARTS_WITH -> value.startsWith(target);
            case ENDS_WITH -> value.endsWith(target);
            default -> false;
        };
        if (inverted) return !computed;
        return computed;
    }

    @Override
    public Condition clone() {
        return new TextCondition(comparator, value, target, inverted);
    }

}
