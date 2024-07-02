package dev.foxikle.customnpcs.internal.commands;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.data.Settings;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.Messages;
import dev.foxikle.customnpcs.internal.Utils;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.menu.MenuCore;
import dev.foxikle.customnpcs.internal.menu.MenuUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * The class to handle the core command
 */
public class CommandCore implements CommandExecutor, TabCompleter {
    /**
     * The instance of the main class
     */
    private final CustomNPCs plugin;

    /**
     * Creates the command handler
     *
     * @param plugin the instance of the Main Class
     */
    public CommandCore(CustomNPCs plugin) {
        this.plugin = plugin;
    }

    /**
     * <p>The generic handler for any command
     * </p>
     *
     * @param command The command used
     * @param sender  The sender of the command
     * @param label   The label of the command (/label args[])
     * @param args    The arguments of the commands
     * @return if the command was handled
     * @since 1.3-pre5
     */
    @SuppressWarnings("all") // yes I know `plugin.getPluginMeta().getVersion()` is uNstAblE!
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player p) {
            if (args.length == 0) {
                p.performCommand("npc help");
                return true;
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("help")) {
                    if (!p.hasPermission("customnpcs.commands.help")) {
                        p.sendMessage(Messages.translated("customnpcs.commands.no_permission"));
                        return true;
                    }
                    p.sendMessage(Messages.translated("customnpcs.commands.header", Component.text(plugin.getPluginMeta().getVersion())));

                    p.sendMessage(getHelpComponent());
                } else if (args[0].equalsIgnoreCase("manage")) {
                    if (!p.hasPermission("customnpcs.commands.manage")) {
                        p.sendMessage(Messages.translated("customnpcs.commands.no_permission"));
                        return true;
                    }
                    if (plugin.getNPCs().isEmpty()) {
                        p.sendMessage(Messages.translated("customnpcs.commands.manage.no_npcs"));
                        return true;
                    }
                    p.sendMessage(Messages.translated("customnpcs.commands.manage.header"));
                    Component message = Component.empty();
                    for (InternalNpc npc : plugin.getNPCs()) {
                        if (npc.getSettings().isResilient()) {
                            Component name = Utils.mm("<gray>◆<reset> ")
                                    .append(plugin.getMiniMessage().deserialize(npc.getSettings().getName()).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.manage.copy_uuid")))).clickEvent(net.kyori.adventure.text.event.ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, npc.getUniqueID().toString()))
                                    .append(Messages.translated("customnpcs.commands.manage.button.edit").appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.manage.button.edit.hover"))).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/npc edit " + npc.getUniqueID())))
                                    .append(Messages.translated("customnpcs.commands.manage.button.delete").appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.manage.button.delete.hover"))).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc delete " + npc.getUniqueID())))
                                    .appendNewline().append(Component.text("                                                                                 ", NamedTextColor.DARK_GREEN, TextDecoration.STRIKETHROUGH));
                            message = message.append(name);
                        }
                    }
                    p.sendMessage(message);


                } else if (args[0].equalsIgnoreCase("new")) {
                    p.performCommand("npc create");
                } else if (args[0].equalsIgnoreCase("list")) {
                    p.performCommand("npc manage");
                } else if (args[0].equalsIgnoreCase("clear_holograms")) {
                    if (p.hasPermission("customnpcs.commands.removeHolograms")) {
                        p.sendMessage(Messages.translated("customnpcs.commands.clear_holograms.removed"));
                    } else {
                        p.sendMessage(Messages.translated("customnpcs.commands.no_permission"));
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("create")) {
                    if (!p.hasPermission("customnpcs.create")) {
                        p.sendMessage(Messages.translated("customnpcs.commands.no_permission"));
                        return true;
                    }
                    UUID uuid = UUID.randomUUID();
                    InternalNpc npc = plugin.createNPC(p.getWorld(), p.getLocation(), new Equipment(), new Settings(), uuid, null, new ArrayList<>());
                    MenuCore mc = new MenuCore(npc, plugin);
                    plugin.menuCores.put(p, mc);
                    mc.getMainMenu().open(p);
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (!p.hasPermission("customnpcs.commands.reload")) {
                        p.sendMessage(Messages.translated("customnpcs.commands.no_permission"));
                        return true;
                    }

                    p.sendMessage(Messages.translated("customnpcs.commands.reload.start"));
                    plugin.reloadConfig();

                    try {
                        Bukkit.getScoreboardManager().getMainScoreboard().getTeam("npc").unregister();
                    } catch (IllegalArgumentException ignored) {
                    }

                    List<InternalNpc> npcs = new ArrayList<>(plugin.npcs.values());
                    for (InternalNpc npc : npcs) {
                        plugin.npcs.remove(npc.getUniqueID());
                        npc.remove();
                    }
                    plugin.npcs.clear();
                    plugin.holograms.clear();
                    plugin.onDisable();
                    plugin.onEnable();
                    p.sendMessage(Messages.translated("customnpcs.commands.reload.end"));
                } else if (args[0].equalsIgnoreCase("wiki") || args[0].equalsIgnoreCase("docs")) {
                    p.sendMessage(Messages.translated("customnpcs.commands.wiki")
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://docs.foxikle.dev"))
                            .appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.wiki.hover"))));
                }
            } else {
                if (args[0].equalsIgnoreCase("setsound")) {
                    if (plugin.soundWaiting.contains(p)) {
                        try {
                            Sound.valueOf(args[1]);
                        } catch (IllegalArgumentException ex) {
                            p.sendMessage(Messages.translated("customnpcs.commands.setsound.unknown_sound"));
                            return true;
                        }
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            plugin.soundWaiting.remove(p);
                            List<String> argsCopy = plugin.editingActions.get(p).getArgsCopy();
                            Action action = plugin.editingActions.get(p);
                            List<String> arg = action.getArgs();
                            arg.clear();
                            arg.add(0, argsCopy.get(0));
                            arg.add(1, argsCopy.get(1));
                            arg.add(2, args[1]);
                            p.sendMessage(Messages.translated("customnpcs.commands.setsound.success", Component.text(args[1])));
                            plugin.menuCores.get(p).getActionCustomizerMenu(action).open(p);
                        });
                    } else {
                        p.sendMessage(Messages.translated("customnpcs.commands.setsound.was_not_waiting"));
                    }
                } else {
                    UUID uuid = null;
                    InternalNpc npc;
                    try {
                        uuid = UUID.fromString(args[1]);
                        if (plugin.getNPCByID(uuid) == null) {
                            p.sendMessage(Messages.translated("customnpcs.commands.invalid_uuid"));
                            return true;
                        }
                        npc = plugin.getNPCByID(uuid);
                    } catch (IllegalArgumentException ignored) {
                        List<String> mutArgs = Utils.list(args);
                        mutArgs.remove(0);
                        // check for names instead
                        List<UUID> uuids = new ArrayList<>();
                        plugin.npcs.forEach((id, Npc) -> {
                            if (plugin.getMiniMessage().stripTags(Npc.getSettings().getName()).equalsIgnoreCase(String.join(" ", mutArgs))) {
                                uuids.add(id);
                            }
                        });
                        if (uuids.isEmpty()) {
                            p.sendMessage(Messages.translated("customnpcs.commands.invalid_name_or_uuid"));
                            return false;
                        } else if (uuids.size() > 1) {
                            double value = Double.MAX_VALUE;
                            for (UUID id : uuids) {
                                double ds = Objects.requireNonNull(plugin.getNPCByID(id)).getCurrentLocation().distanceSquared(p.getLocation());
                                if (ds < value) {
                                    uuid = id;
                                    value = ds;
                                }
                            }
                        } else {
                            uuid = uuids.get(0);
                        }

                        if (uuid == null) return true;
                        if (plugin.getNPCByID(uuid) == null) {
                            p.sendMessage(Messages.translated("customnpcs.commands.invalid_uuid"));
                            return true;
                        }
                        npc = plugin.getNPCByID(uuid);
                    }
                    assert npc != null;
                    switch (args[0].toLowerCase()) {
                        case "delete" -> {
                            if (!p.hasPermission("customnpcs.delete")) {
                                p.sendMessage(Messages.translated("customnpcs.commands.no_permission"));
                                return true;
                            }
                            MenuUtils.getDeletionConfirmationMenu(npc, null).open(p);
                        }
                        case "edit" -> {
                            if (!p.hasPermission("customnpcs.edit")) {
                                p.sendMessage(Messages.translated("customnpcs.commands.no_permission"));
                                return true;
                            }

                            InternalNpc finalNpc = npc;
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                InternalNpc newNpc = plugin.createNPC(p.getWorld(), finalNpc.getSpawnLoc(), finalNpc.getEquipment(), finalNpc.getSettings(), finalNpc.getUniqueID(), null, finalNpc.getActions());
                                MenuCore mc = new MenuCore(newNpc, plugin);
                                plugin.menuCores.put(p, mc);
                                mc.getMainMenu().open(p);
                            }, 1);
                        }
                        case "goto" -> {
                            if (!p.hasPermission("customnpcs.commands.goto")) {
                                p.sendMessage(Messages.translated("customnpcs.commands.no_permission"));
                                return true;
                            }
                            p.teleportAsync(npc.getCurrentLocation());
                        }
                        case "clone" -> {
                            if (!p.hasPermission("customnpcs.commands.clone")) {
                                p.sendMessage(Messages.translated("customnpcs.commands.no_permission"));
                                return true;
                            }
                            assert npc != null;
                            InternalNpc newNpc = npc.clone();
                            newNpc.setSpawnLoc(p.getLocation());
                            newNpc.getSettings().setDirection(p.getLocation().getYaw());
                            newNpc.createNPC();
                            p.sendMessage(Messages.translated("customnpcs.commands.clone.success"));

                            // runnable
                        }
                        case "movehere" -> {
                            if (!p.hasPermission("customnpcs.commands.movehere")) {
                                p.sendMessage(Messages.translated("customnpcs.commands.no_permission"));
                                return true;
                            }
                            p.sendMessage(Messages.translated("customnpcs.commands.move.nudge"));
                            assert npc != null;
                            npc.remove();
                            npc.setSpawnLoc(p.getLocation());
                            npc.getSettings().setDirection(p.getLocation().getYaw());
                            npc.createNPC();
                        }
                        default -> sender.sendMessage(Messages.translated("customnpcs.commands.unknown_command"));
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            boolean silent = false;
            if (args.length >= 2) {
                if (args[1].equalsIgnoreCase("silent")) {
                    silent = true;
                }
            }

            if (!silent) sender.sendMessage(Messages.translated("customnpcs.commands.reload.start"));
            try {
                Objects.requireNonNull(Bukkit.getScoreboardManager().getMainScoreboard().getTeam("npc")).unregister();
            } catch (IllegalArgumentException | NullPointerException ignored) {
                plugin.getLogger().info("Failed to unregister the \"npc\" team during reload, this isn't an issue, but might be helpful for debugging purposes.");
            }
            List<InternalNpc> npcs = new ArrayList<>(plugin.npcs.values());
            for (InternalNpc npc : npcs) {
                plugin.npcs.remove(npc.getUniqueID());
                npc.remove();
            }
            plugin.npcs.clear();
            plugin.holograms.clear();
            plugin.onEnable();
            if (!silent) sender.sendMessage(Messages.translated("customnpcs.commands.reload.end"));
        }
        return false;
    }

    @NotNull
    private Component getHelpComponent() {
        Component component = Component.empty();
        component = component.appendNewline()
                .append(Messages.translated("customnpcs.commands.help.help.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.help.aliases"))))
                .append(Messages.translated("customnpcs.commands.help.help.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.help.hover"))))
                .appendNewline()
                .append(Messages.translated("customnpcs.commands.help.manage.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.manage.aliases"))))
                .append(Messages.translated("customnpcs.commands.help.manage.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.manage.hover"))))
                .appendNewline()
                .append(Messages.translated("customnpcs.commands.help.create.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.create.aliases"))))
                .append(Messages.translated("customnpcs.commands.help.create.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.create.hover"))))
                .appendNewline()
                .append(Messages.translated("customnpcs.commands.help.delete.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.delete.aliases"))))
                .append(Messages.translated("customnpcs.commands.help.delete.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.delete.hover"))))
                .appendNewline()
                .append(Messages.translated("customnpcs.commands.help.edit.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.edit.aliases"))))
                .append(Messages.translated("customnpcs.commands.help.edit.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.edit.hover"))))
                .appendNewline()
                .append(Messages.translated("customnpcs.commands.help.movehere.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.movehere.aliases"))))
                .append(Messages.translated("customnpcs.commands.help.movehere.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.movehere.hover"))))
                .appendNewline()
                .append(Messages.translated("customnpcs.commands.help.clone.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.clone.aliases"))))
                .append(Messages.translated("customnpcs.commands.help.clone.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.clone.hover"))))
                .appendNewline()
                .append(Messages.translated("customnpcs.commands.help.reload.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.reload.aliases"))))
                .append(Messages.translated("customnpcs.commands.help.reload.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.reload.hover"))))
                .appendNewline()
                .append(Messages.translated("customnpcs.commands.help.goto.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.goto.aliases"))))
                .append(Messages.translated("customnpcs.commands.help.goto.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.goto.hover"))))
                .appendNewline()
                .append(Messages.translated("customnpcs.commands.help.wiki.syntax").color(NamedTextColor.GOLD).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.wiki.aliases"))))
                .append(Messages.translated("customnpcs.commands.help.wiki.description").color(NamedTextColor.DARK_AQUA).appendSpace().hoverEvent(HoverEvent.showText(Messages.translated("customnpcs.commands.help.wiki.hover"))))
                .appendNewline()
                .append(Component.text("                                                                                 ", NamedTextColor.DARK_GREEN, TextDecoration.STRIKETHROUGH));
        return component;
    }

    /**
     * <p>The generic handler for any tab completion
     * </p>
     *
     * @param command The command used
     * @param sender  The sender of the command
     * @param label   The label of the command (/label args[])
     * @param args    The arguments of the commands
     * @return the options to tab-complete
     * @since 1.3-pre5
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("help");
            list.add("manage");
            list.add("create");
            list.add("delete");
            list.add("edit");
            list.add("reload");
            list.add("goto");
            list.add("wiki");
            list.add("clone");
            list.add("movehere");
            if (plugin.soundWaiting.contains((Player) sender)) list.add("setsound");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("setsound")) {
                for (Sound sound : Sound.values()) {
                    list.add(sound.name());
                }
                return list;
            }
            plugin.npcs.forEach((uuid, npc) -> list.add(plugin.getMiniMessage().stripTags(npc.getSettings().getName())));
        }
        return list;
    }
}
