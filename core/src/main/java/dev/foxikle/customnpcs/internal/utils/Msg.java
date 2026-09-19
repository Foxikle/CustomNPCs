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

package dev.foxikle.customnpcs.internal.utils;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.translations.Translations;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.*;

/**
 * A class that handles message translations
 */
public class Msg {

    public static final MiniMessage MINI = MiniMessage.miniMessage();
    public static final JSONComponentSerializer JSON = JSONComponentSerializer.json();

    private static final String TAG_NAME = "tr";

    private static TagResolver getTagResolver(
            Locale locale,
            TagResolver... inherited
    ) {
        return TagResolver.resolver(TAG_NAME, (queue, context) -> {
            String key = queue.popOr(
                    "<tr:...> requires a translation key"
            ).value();

            String translated = translatedString(locale, key);

            TagResolver nested = TagResolver.builder()
                    .resolvers(inherited)
                    .resolver(getTagResolver(locale, inherited)) //todo: stack overflow?
                    .build();

            Component component = context.deserialize(translated, nested);

            return Tag.selfClosingInserting(component);
        });
    }

    public static String translatedString(Locale locale, String key) {
        String message = Translations.getString(locale, key);

        if (message.equals(key)) {
            CustomNPCs.getInstance().getLogger().warning("Could not translate " + key + " to " + locale);
        }

        return message;
    }

    public static Component[] lore(Locale locale, String key, TagResolver... args) {
        return vlore(locale, key, 37, args);
    }

    public static Component[] vlore(Locale locale, String key, int width, TagResolver... args) {
        return ComponentWrapper.wrap(get(locale, key, args), width)
                .toArray(Component[]::new);
    }

    public static Component format(String str, TagResolver tags, Object... args) {
        return MINI.deserialize(str.formatted(args), tags)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static Component get(Locale locale, String key, TagResolver... arguments) {
        String message = translatedString(locale, key);

        TagResolver resolver = TagResolver.builder()
                .resolver(getTagResolver(locale))
                .resolvers(arguments)
                .build();

        return MINI.deserialize(message, resolver)
                .decorationIfAbsent(
                        TextDecoration.ITALIC,
                        TextDecoration.State.FALSE
                );
    }

    public static Component get(Player p, String str, TagResolver... args) {
        return get(p.locale(), str, args);
    }

    public static Component format(Locale locale, String str, Object... args) {
        if (str.isEmpty()) return Component.empty();
        return MINI.deserialize(str.formatted(args), getTagResolver(locale))
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .colorIfAbsent(NamedTextColor.WHITE);
    }

    public static Component format(String str, Object... args) {
        if (str.isEmpty()) return Component.empty();
        return MINI.deserialize(str.formatted(args))
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .colorIfAbsent(NamedTextColor.WHITE);
    }

    public static String toMini(Component component) {
        return MINI.serialize(component);
    }

    public static String toJson(Component component) {
        return JSON.serialize(component);
    }

    public static String papi(Player player, String text) {
        if (CustomNPCs.getInstance().papi) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    public static String flatten(List<Component> list) {
        StringBuilder builder = new StringBuilder();
        for (Component component : list) {
            builder.append(toMini(component));
            builder.append("<newline>");
        }

        return builder.toString();
    }

    public static String plainText(Component comp) {
        return PlainTextComponentSerializer.plainText().serialize(comp);
    }
}
