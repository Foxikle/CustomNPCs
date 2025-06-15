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
import net.kyori.adventure.translation.GlobalTranslator;

import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.Locale;

/**
 * A class that handles message translations
 */
public class Msg {

    public static Component translate(Locale locale, String key, Object... args) {
        return Msg.format(translatedString(locale, key, args));
    }

    public static String translatedString(Locale locale, String key, Object... args) {
        Object[] translatedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof ComponentLike like) {
                translatedArgs[i] = minimessage(like.asComponent());
            } else if (args[i] instanceof String string) {
                translatedArgs[i] = string;
            } else {
                translatedArgs[i] = args[i];
            }
        }

        MessageFormat format = GlobalTranslator.translator().translate(key, locale);
        if (format == null) {
            CustomNPCs.getInstance().getLogger().warning("Could not translate " + key + " to " + locale);
            return key;
        }

        StringBuffer buffer = format.format(translatedArgs, new StringBuffer(), new FieldPosition(0));
        return buffer.toString();
    }

    public static Component[] lore(Locale locale, String key, Object... args) {
        return ComponentWrapper.wrap(translate(locale, key, args), 37)
                .toArray(Component[]::new);
    }

    public static Component[] vlore(Locale locale, String key, int width, Object... args) {
        return ComponentWrapper.wrap(translate(locale, key, args), width)
                .toArray(Component[]::new);
    }

    public static Component format(String str) {
        return CustomNPCs.getInstance().getMiniMessage().deserialize(str)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .colorIfAbsent(NamedTextColor.WHITE);
    }

    public static String minimessage(Component component) {
        return CustomNPCs.getInstance().getMiniMessage().serialize(component);
    }

    public static String plainText(Component comp) {
        return PlainTextComponentSerializer.plainText().serialize(comp);
    }
}
