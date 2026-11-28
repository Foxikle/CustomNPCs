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

package dev.foxikle.customnpcs.data;

import dev.foxikle.customnpcs.actions.impl.RunCommand;
import dev.foxikle.customnpcs.conditions.Condition;
import dev.foxikle.customnpcs.conditions.Selector;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.bukkit.potion.PotionEffectType;

import java.util.function.Function;

interface CommandDataFixers {

    static <T, V> Function<T, V> err() {
        return _ -> {
            throw new IllegalStateException("Called encode on a decode only codec.");
        };
    }


    CommandDataFixer GIVE_EFFECT = () -> StructCodec.struct(
            "effect", Codec.STRING, err(),
            "duration", Codec.INT, err(),
            "amplifier", Codec.INT, err(),
            "particles", Codec.BOOLEAN, err(),
            "delay", Codec.INT, err(),
            "selector", Codec.Enum(Selector.class), err(),
            "conditions", Condition.CODEC.list(), err(),
            "cooldown", Codec.INT, err(),
            "uuid", Codec.UUID_STRING.optional(), err(),
            (s, i, i2, b, i3, e, l, i4, u) -> {
                PotionEffectType eff = PotionEffectType.getByName(s);
                return new RunCommand(
                        "effect give %%player_name%% %s %d %d %s".formatted(eff.getKey().asString(), i / 20, i2, b),
                        true, i3, e, l, i4, u
                );
            }
    );

    CommandDataFixer REMOVE_EFFECT = () -> StructCodec.struct(
            "effect", Codec.STRING, err(),
            "delay", Codec.INT, err(),
            "selector", Codec.Enum(Selector.class), err(),
            "conditions", Condition.CODEC.list(), err(),
            "cooldown", Codec.INT, err(),
            "uuid", Codec.UUID_STRING.optional(), err(),
            (s, i, e, l, i2, u) ->{
                PotionEffectType eff = PotionEffectType.getByName(s);
                return new RunCommand("effect clear %%player_name%% %s".formatted(eff.getKey().asString()),
                        true, i, e, l, i2, u);
            }
    );

    CommandDataFixer GIVE_XP = () -> StructCodec.struct(
            "amount", Codec.INT, err(),
            "levels", Codec.BOOLEAN, err(),
            "delay", Codec.INT, err(),
            "selector", Codec.Enum(Selector.class), err(),
            "conditions", Condition.CODEC.list(), err(),
            "cooldown", Codec.INT, err(),
            "uuid", Codec.UUID_STRING.optional(), err(),
            (i, b, i2, e, l, i3, u) ->
                    new RunCommand("xp add %%player_name%% %d %s".formatted(i, b ? "levels" : "points"),
                            true, i2, e, l, i3, u)
    );

    CommandDataFixer REMOVE_XP = () -> StructCodec.struct(
            "amount", Codec.INT, err(),
            "levels", Codec.BOOLEAN, err(),
            "delay", Codec.INT, err(),
            "selector", Codec.Enum(Selector.class), err(),
            "conditions", Condition.CODEC.list(), err(),
            "cooldown", Codec.INT, err(),
            "uuid", Codec.UUID_STRING.optional(), err(),
            (i, b, i2, e, l, i3, u) ->
                    new RunCommand("xp add %%player_name%% -%d %s".formatted(i, b ? "levels" : "points"),
                            true, i2, e, l, i3, u)
    );

    CommandDataFixer PLAY_SOUND = () -> StructCodec.struct(
            "sound", Codec.STRING, err(),
            "volume", Codec.FLOAT, err(),
            "pitch", Codec.FLOAT, err(),
            "delay", Codec.INT, err(),
            "selector", Codec.Enum(Selector.class), err(),
            "conditions", Condition.CODEC.list(), err(),
            "cooldown", Codec.INT, err(),
            "uuid", Codec.UUID_STRING.optional(), err(),
            (s, f, f2, i, e, l, i2, u) ->
                    new RunCommand("playsound %s master %%player_name%% %%player_x%% %%player_y%% %%player_z%% %f %f".formatted(s, f, f2),
                            true, i, e, l, i2, u)
    );

    CommandDataFixer TELEPORT = () -> StructCodec.struct(
            "x", Codec.DOUBLE, err(),
            "y", Codec.DOUBLE, err(),
            "z", Codec.DOUBLE, err(),
            "pitch", Codec.FLOAT, err(),
            "yaw", Codec.FLOAT, err(),
            "delay", Codec.INT, err(),
            "selector", Codec.Enum(Selector.class), err(),
            "conditions", Condition.CODEC.list(), err(),
            "cooldown", Codec.INT, err(),
            "uuid", Codec.UUID_STRING.optional(), err(),
            (d, d1, d2, f, f1, i, e, l, i2, u) ->
                    new RunCommand("teleport %%player_name%% %f %f %f %f %f".formatted(d, d1, d2, f, f1),
                            true, i, e, l, i2, u)
    );


}
