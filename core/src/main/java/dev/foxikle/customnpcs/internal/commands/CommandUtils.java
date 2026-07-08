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

package dev.foxikle.customnpcs.internal.commands;

import com.google.common.collect.Iterables;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.utils.Msg;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@UtilityClass
public class CommandUtils {

    @NotNull
    public static Component getHelpComponent(Locale p) {
        Component component = Msg.translate(p, "customnpcs.commands.header", Component.text(CustomNPCs.getInstance().getPluginMeta().getVersion()));
        component = component.appendNewline()
                .append(Msg.translate(p, "customnpcs.commands.help.help.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.help.aliases"))))
                .append(Msg.translate(p, "customnpcs.commands.help.help.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.help.hover"))))
                .appendNewline()
                .append(Msg.translate(p, "customnpcs.commands.help.manage.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.manage.aliases"))))
                .append(Msg.translate(p, "customnpcs.commands.help.manage.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.manage.hover"))))
                .appendNewline()
                .append(Msg.translate(p, "customnpcs.commands.help.create.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.create.aliases"))))
                .append(Msg.translate(p, "customnpcs.commands.help.create.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.create.hover"))))
                .appendNewline()
                .append(Msg.translate(p, "customnpcs.commands.help.delete.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.delete.aliases"))))
                .append(Msg.translate(p, "customnpcs.commands.help.delete.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.delete.hover"))))
                .appendNewline()
                .append(Msg.translate(p, "customnpcs.commands.help.edit.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.edit.aliases"))))
                .append(Msg.translate(p, "customnpcs.commands.help.edit.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.edit.hover"))))
                .appendNewline()
                .append(Msg.translate(p, "customnpcs.commands.help.movehere.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.movehere.aliases"))))
                .append(Msg.translate(p, "customnpcs.commands.help.movehere.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.movehere.hover"))))
                .appendNewline()
                .append(Msg.translate(p, "customnpcs.commands.help.clone.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.clone.aliases"))))
                .append(Msg.translate(p, "customnpcs.commands.help.clone.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.clone.hover"))))
                .appendNewline()
                .append(Msg.translate(p, "customnpcs.commands.help.reload.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.reload.aliases"))))
                .append(Msg.translate(p, "customnpcs.commands.help.reload.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.reload.hover"))))
                .appendNewline()
                .append(Msg.translate(p, "customnpcs.commands.help.goto.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.goto.aliases"))))
                .append(Msg.translate(p, "customnpcs.commands.help.goto.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.goto.hover"))))
                .appendNewline()
                .append(Msg.translate(p, "customnpcs.commands.help.wiki.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.wiki.aliases"))))
                .append(Msg.translate(p, "customnpcs.commands.help.wiki.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.wiki.hover"))))
                .appendNewline()
                .append(Msg.translate(p, "customnpcs.commands.help.debug.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.debug.aliases"))))
                .append(Msg.translate(p, "customnpcs.commands.help.debug.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.debug.hover"))))
                .appendNewline()
                .append(Msg.translate(p, "customnpcs.commands.help.disabletip.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.disabletip.aliases"))))
                .append(Msg.translate(p, "customnpcs.commands.help.disabletip.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.help.disabletip.hover"))))
                .appendNewline()
                .append(Component.text("                                                                                 ", NamedTextColor.DARK_GREEN, TextDecoration.STRIKETHROUGH));
        return component;
    }

    @NotNull
    public static Component getListComponent(Locale p) {
        CustomNPCs plugin = CustomNPCs.getInstance();
        if (plugin.getNPCs().isEmpty()) {
            return Msg.translate(p, "customnpcs.commands.manage.no_npcs");
        }

        Component message = Msg.translate(p, "customnpcs.commands.manage.header").appendNewline();
        for (InternalNpc npc : plugin.getNPCs()) {
            if (npc.getSettings().isResilient()) {
                Component name = Msg.format("<gray>◆<reset> ")
                        .append(plugin.getMiniMessage().deserialize(npc.getSettings().getName()).appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.manage.copy_uuid")))).clickEvent(ClickEvent.copyToClipboard(npc.getUniqueID().toString()))
                        .append(Msg.translate(p, "customnpcs.commands.manage.button.edit").appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.manage.button.edit.hover"))).clickEvent(ClickEvent.runCommand("/npc edit " + npc.getUniqueID())))
                        .append(Msg.translate(p, "customnpcs.commands.manage.button.delete").appendSpace().hoverEvent(HoverEvent.showText(Msg.translate(p, "customnpcs.commands.manage.button.delete.hover"))).clickEvent(ClickEvent.suggestCommand("/npc delete " + npc.getUniqueID())))
                        .appendNewline();
                message = message.append(name);
            }
        }
        message = message.append(Component.text("                                                                                 ", NamedTextColor.DARK_GREEN, TextDecoration.STRIKETHROUGH));
        return message;
    }

    public static boolean checkNpc(CommandSender source, UUID npc) {
        Locale locale = Locale.getDefault();
        if (source instanceof Player player) locale = player.locale();

        if (npc == null) {
            source.sendMessage(Msg.translate(locale, "customnpcs.commands.invalid_name_or_uuid"));
            return false;
        }

        boolean valid = CustomNPCs.getInstance().npcs.containsKey(npc);

        if (!valid) source.sendMessage(Msg.translate(locale, "customnpcs.commands.invalid_uuid"));
        return valid;
    }

    public static UUID parseNpc(CommandSender source, String data) {
        Locale locale = Locale.getDefault();
        if (source instanceof Player player) locale = player.locale();

        final CustomNPCs plugin = CustomNPCs.getInstance();
        UUID uuid;
        try {
            uuid = UUID.fromString(data);
            if (plugin.getNPCByID(uuid) == null) {
                source.sendMessage(Msg.translate(locale, "customnpcs.commands.invalid_uuid"));
                return null;
            }
        } catch (IllegalArgumentException ignored) {
            if (!(source instanceof Player p)) {
                return null;
            }

            Locale finalLocale = locale;
            Set<UUID> uuids = plugin.npcs.values().stream().map(npc -> {
                if (plugin.getMiniMessage().stripTags(npc.getSettings().getName()).equalsIgnoreCase(data)) {
                    return npc.getUniqueID();
                }
                return null;
            }).collect(Collectors.toSet());
            uuids.removeIf(Objects::isNull);

            if (uuids.isEmpty()) {
                source.sendMessage(Msg.translate(locale, "customnpcs.commands.invalid_name_or_uuid"));
                return null;
            } else if (uuids.size() > 1) {
                double value = Double.MAX_VALUE;
                uuid = null;
                for (UUID id : uuids) {
                    InternalNpc npc = plugin.getNPCByID(id);
                    assert npc != null : "Npc is null when parsing the closest NPC";

                    if (p.getWorld() != npc.getWorld()) continue;

                    double ds = npc.getCurrentLocation().distanceSquared(p.getLocation());
                    if (ds < value) {
                        uuid = id;
                        value = ds;
                    }
                }

                if (uuid == null) {
                    uuid = Iterables.getFirst(uuids, null);
                }
            } else {
                uuid = Iterables.getFirst(uuids, null);
            }

            if (uuid == null) {
                source.sendMessage(Msg.translate(locale, "customnpcs.commands.invalid_name_or_uuid"));
                return null;
            }
            if (plugin.getNPCByID(uuid) == null) {
                source.sendMessage(Msg.translate(p.locale(), "customnpcs.commands.invalid_uuid"));
                return null;
            }
            return uuid;
        }

        InternalNpc npc = plugin.getNPCByID(uuid);

        if (npc == null) {
            source.sendMessage(Msg.translate(locale, "customnpcs.commands.invalid_name_or_uuid"));
            return null;
        }

        boolean valid = CustomNPCs.getInstance().npcs.containsKey(uuid);

        if (!valid) {
            source.sendMessage(Msg.translate(locale, "customnpcs.commands.invalid_uuid"));
            return null;
        } else {
            return uuid;
        }
    }

    public static Locale getLocale(CommandSender source) {
        if (source instanceof Player player) return player.locale();
        return Locale.getDefault();
    }
}