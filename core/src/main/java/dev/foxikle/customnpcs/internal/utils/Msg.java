/*
 * Copyright (c) 2024. Foxikle
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

package dev.foxikle.customnpcs.internal.utils;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * A class that handles message translations
 */
public class Msg {
    public static Component translated(String key, ComponentLike... args) {
        return format(plainText(Component.translatable(key, args)));
    }

    public static Component translate(String key, Object... args) {
        ComponentLike[] components = new ComponentLike[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof ComponentLike) {
                components[i] = (ComponentLike) args[i];
                continue;
            }
            components[i] = Component.text(args[i].toString()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        }
        return format(plainText(Component.translatable(key, components)));
    }

    public static String translatedString(String key, ComponentLike... args) {
        return plainText(Component.translatable(key, args));
    }

    public static Component[] lore(String key, ComponentLike... args) {
        return ComponentWrapper.wrap(format(plainText(Component.translatable(key, args))), 37)
                .toArray(Component[]::new);
    }

    public static Component format(String str) {
        return CustomNPCs.getInstance().getMiniMessage().deserialize(str)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .colorIfAbsent(NamedTextColor.WHITE);
    }

    public static String plainText(Component comp) {
        return PlainTextComponentSerializer.plainText().serialize(comp);
    }
}