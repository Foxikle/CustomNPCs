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

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.FileManager;
import dev.foxikle.customnpcs.internal.commands.suggestion.NpcBrokenSuggester;
import dev.foxikle.customnpcs.internal.commands.suggestion.NpcSuggester;
import dev.foxikle.customnpcs.internal.commands.suggestion.SoundSuggester;
import dev.foxikle.customnpcs.internal.commands.suggestion.WorldSuggester;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.menu.MenuUtils;
import dev.foxikle.customnpcs.internal.utils.BrokenReason;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.Utils;
import dev.foxikle.customnpcs.internal.utils.WaitingType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

@SuppressWarnings("UnstableApiUsage")
public class NpcCommandRegistrar {

    private static final String PERMISSION_CREATE = "customnpcs.create";
    private static final String PERMISSION_EDIT = "customnpcs.edit";
    private static final String PERMISSION_DELETE = "customnpcs.delete";
    private static final String PERMISSION_CLONE = "customnpcs.commands.clone";
    private static final String PERMISSION_MOVEHERE = "customnpcs.commands.movehere";
    private static final String PERMISSION_GOTO = "customnpcs.commands.goto";
    private static final String PERMISSION_MANAGE = "customnpcs.commands.manage";
    private static final String PERMISSION_HELP = "customnpcs.commands.help";
    private static final String PERMISSION_RELOAD = "customnpcs.commands.reload";
    private static final String PERMISSION_WIKI = "customnpcs.command.wiki";
    private static final String PERMISSION_DISABLE_TIP = "customnpcs.command.disabletip";
    private static final String PERMISSION_DEBUG = "customnpcs.edit";
    private static final String PERMISSION_FIXCONFIG = "customnpcs.commands.fix_config";
    private static final String PERMISSION_SETSOUND = "customnpcs.edit";


    public static LiteralCommandNode<CommandSourceStack> buildNode() {
        LiteralCommandNode<CommandSourceStack> npcNode = LiteralArgumentBuilder.<CommandSourceStack>literal("npc")
                .requires(source -> source.getSender().hasPermission(PERMISSION_HELP))
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    Locale locale = getLocale(sender);
                    sender.sendMessage(CommandUtils.getHelpComponent(locale));
                    return 1;
                })
                .build();

        registerCreateCommand(npcNode);
        registerEditCommand(npcNode);
        registerDeleteCommand(npcNode);
        registerCloneCommand(npcNode);
        registerMoveHereCommand(npcNode);
        registerGotoCommand(npcNode);
        registerManageCommand(npcNode);
        registerListCommand(npcNode);
        registerHelpCommand(npcNode);
        registerReloadCommand(npcNode);
        registerWikiCommand(npcNode);
        registerDebugCommand(npcNode);
        registerFixConfigCommand(npcNode);
        registerSetsoundCommand(npcNode);
        registerTipCommand(npcNode);

        return npcNode;
    }

    private static void registerCreateCommand(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> createNode = LiteralArgumentBuilder.<CommandSourceStack>literal("create")
                .requires(sender -> sender.getSender().hasPermission(PERMISSION_CREATE))
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(Msg.format("You can't do this :P"));
                        return 0;
                    }
                    UUID uuid = UUID.randomUUID();
                    CustomNPCs plugin = CustomNPCs.getInstance();
                    InternalNpc npc = plugin.createNPC(
                            player.getWorld(),
                            player.getLocation(),
                            new dev.foxikle.customnpcs.data.Equipment(),
                            new dev.foxikle.customnpcs.data.Settings(),
                            uuid,
                            null,
                            new java.util.ArrayList<>()
                    );
                    plugin.getEditingNPCs().put(player.getUniqueId(), npc);
                    plugin.getLotus().openMenu(player, MenuUtils.NPC_MAIN);
                    return 1;
                })
                .build();
        npcNode.addChild(createNode);
    }

    private static void registerEditCommand(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> editNode = LiteralArgumentBuilder.<CommandSourceStack>literal("edit")
                .requires(sender -> sender.getSender().hasPermission(PERMISSION_EDIT))
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("npc", greedyString())
                        .suggests(NpcSuggester.SUGGESTIONS)
                        .executes(NpcCommandRegistrar::executeEditCommand)
                        .build())
                .build();
        npcNode.addChild(editNode);
    }

    private static int executeEditCommand(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        String npcArg = StringArgumentType.getString(ctx, "npc");
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Msg.format("You can't do this :P"));
            return 0;
        }
        UUID uuid = CommandUtils.parseNpc(player, npcArg);
        if (uuid == null) {
            return 0;
        }
        CustomNPCs plugin = CustomNPCs.getInstance();
        if (plugin.getConfig().getBoolean("EditTip") && Utils.shouldSendEditTip(player)) {
            player.sendMessage(Msg.translate(player.locale(), "customnpcs.commands.manage.button.edit.tip"));
        }
        InternalNpc finalNpc = plugin.getNPCByID(uuid);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            InternalNpc newNpc = plugin.createNPC(
                    player.getWorld(),
                    finalNpc.getSpawnLoc(),
                    finalNpc.getEquipment(),
                    finalNpc.getSettings(),
                    finalNpc.getUniqueID(),
                    null,
                    finalNpc.getActions()
            );
            plugin.getEditingNPCs().put(player.getUniqueId(), newNpc);
            plugin.getLotus().openMenu(player, MenuUtils.NPC_MAIN);
        }, 1);
        return 1;
    }

    private static void registerDeleteCommand(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> deleteNode = LiteralArgumentBuilder.<CommandSourceStack>literal("delete")
                .requires(sender -> sender.getSender().hasPermission(PERMISSION_DELETE))
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("npc", greedyString())
                        .suggests(NpcSuggester.SUGGESTIONS)
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            String npcArg = StringArgumentType.getString(context, "npc");
                            if (!(sender instanceof Player player)) {
                                sender.sendMessage(Msg.format("You can't do this :P"));
                                return 0;
                            }
                            UUID uuid = CommandUtils.parseNpc(player, npcArg);
                            if (uuid == null) {
                                return 0;
                            }
                            if (!CommandUtils.checkNpc(player, uuid)) {
                                return 0;
                            }
                            CustomNPCs plugin = CustomNPCs.getInstance();
                            InternalNpc finalNpc = plugin.getNPCByID(uuid);
                            plugin.getEditingNPCs().put(player.getUniqueId(), finalNpc);
                            plugin.getDeletionReason().put(player.getUniqueId(), false);
                            plugin.getLotus().openMenu(player, MenuUtils.NPC_DELETE);
                            return 1;
                        })
                        .build())
                .build();
        npcNode.addChild(deleteNode);
    }

    private static void registerCloneCommand(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> cloneNode = LiteralArgumentBuilder.<CommandSourceStack>literal("clone")
                .requires(sender -> sender.getSender().hasPermission(PERMISSION_CLONE))
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("npc", greedyString())
                        .suggests(NpcSuggester.SUGGESTIONS)
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            String npcArg = StringArgumentType.getString(context, "npc");
                            if (!(sender instanceof Player player)) {
                                sender.sendMessage(Msg.format("You can't do this :P"));
                                return 0;
                            }
                            UUID uuid = CommandUtils.parseNpc(player, npcArg);
                            if (uuid == null) {
                                return 0;
                            }
                            if (!CommandUtils.checkNpc(player, uuid)) {
                                return 0;
                            }
                            CustomNPCs plugin = CustomNPCs.getInstance();
                            InternalNpc finalNpc = plugin.getNPCByID(uuid);
                            InternalNpc newNpc = finalNpc.clone();
                            newNpc.setSpawnLoc(player.getLocation());
                            newNpc.getSettings().setDirection(player.getLocation().getYaw());
                            newNpc.createNPC();
                            player.sendMessage(Msg.translate(player.locale(), "customnpcs.commands.clone.success"));
                            return 1;
                        })
                        .build())
                .build();
        npcNode.addChild(cloneNode);
    }

    private static void registerMoveHereCommand(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> movehereNode = LiteralArgumentBuilder.<CommandSourceStack>literal(
                        "movehere")
                .requires(sender -> sender.getSender().hasPermission(PERMISSION_MOVEHERE))
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("npc", greedyString())
                        .suggests(NpcSuggester.SUGGESTIONS)
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            String npcArg = StringArgumentType.getString(context, "npc");
                            if (!(sender instanceof Player player)) {
                                sender.sendMessage(Msg.format("<red>You can't do this :P"));
                                return 0;
                            }
                            UUID uuid = CommandUtils.parseNpc(player, npcArg);
                            if (uuid == null) {
                                return 0;
                            }
                            if (!CommandUtils.checkNpc(player, uuid)) {
                                return 0;
                            }
                            CustomNPCs plugin = CustomNPCs.getInstance();
                            InternalNpc finalNpc = plugin.getNPCByID(uuid);
                            player.sendMessage(Msg.translate(player.locale(), "customnpcs.commands.move.nudge"));
                            finalNpc.teleport(player.getLocation());
                            finalNpc.remove();
                            finalNpc.createNPC();
                            Bukkit.getOnlinePlayers().forEach(finalNpc::injectPlayer);
                            return 1;
                        })
                        .build())
                .build();
        npcNode.addChild(movehereNode);
    }

    private static void registerGotoCommand(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> gotoNode = LiteralArgumentBuilder.<CommandSourceStack>literal("goto")
                .requires(sender -> sender.getSender().hasPermission(PERMISSION_GOTO))
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("npc", greedyString())
                        .suggests(NpcSuggester.SUGGESTIONS)
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            String npcArg = StringArgumentType.getString(context, "npc");
                            if (!(sender instanceof Player player)) {
                                sender.sendMessage(Msg.format("You can't do this :P"));
                                return 0;
                            }
                            UUID uuid = CommandUtils.parseNpc(player, npcArg);
                            if (uuid == null) {
                                return 0;
                            }
                            if (!CommandUtils.checkNpc(player, uuid)) {
                                return 0;
                            }
                            CustomNPCs plugin = CustomNPCs.getInstance();
                            InternalNpc finalNpc = plugin.getNPCByID(uuid);
                            player.teleportAsync(finalNpc.getCurrentLocation());
                            return 1;
                        })
                        .build())
                .build();
        npcNode.addChild(gotoNode);
    }

    private static void registerManageCommand(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> manageNode = LiteralArgumentBuilder.<CommandSourceStack>literal("manage")
                .requires(sender -> sender.getSender().hasPermission(PERMISSION_MANAGE))
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    Locale locale = getLocale(sender);
                    sender.sendMessage(CommandUtils.getListComponent(locale));
                    return 1;
                })
                .build();
        npcNode.addChild(manageNode);
    }

    private static void registerListCommand(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> listNode = LiteralArgumentBuilder.<CommandSourceStack>literal("list")
                .requires(sender -> sender.getSender().hasPermission(PERMISSION_MANAGE))
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    Locale locale = getLocale(sender);
                    sender.sendMessage(CommandUtils.getListComponent(locale));
                    return 1;
                })
                .build();
        npcNode.addChild(listNode);
    }

    private static void registerHelpCommand(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> helpNode = LiteralArgumentBuilder.<CommandSourceStack>literal("help")
                .requires(sender -> sender.getSender().hasPermission(PERMISSION_HELP))
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    sender.sendMessage(CommandUtils.getHelpComponent(getLocale(sender)));
                    return 1;
                })
                .build();
        npcNode.addChild(helpNode);
    }

    private static void registerReloadCommand(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> reloadNode = LiteralArgumentBuilder.<CommandSourceStack>literal("reload")
                .requires(sender -> sender.getSender().hasPermission(PERMISSION_RELOAD))
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    CustomNPCs plugin = CustomNPCs.getInstance();
                    plugin.setReloading(true);
                    Locale locale = getLocale(sender);
                    sender.sendMessage(Msg.translate(locale, "customnpcs.commands.reload.start"));
                    plugin.reloadConfig();
                    plugin.onDisable();
                    plugin.onEnable();
                    plugin.setReloading(false);
                    sender.sendMessage(Msg.translate(locale, "customnpcs.commands.reload.end"));
                    return 1;
                })
                .build();
        npcNode.addChild(reloadNode);
    }

    private static void registerWikiCommand(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> wikiNode = LiteralArgumentBuilder.<CommandSourceStack>literal("wiki")
                .requires(sender -> sender.getSender().hasPermission(PERMISSION_WIKI))
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    Locale locale = getLocale(sender);
                    sender.sendMessage(
                            Msg.translate(locale, "customnpcs.commands.wiki")
                                    .clickEvent(ClickEvent.openUrl("https://docs.foxikle.dev"))
                                    .appendSpace()
                                    .hoverEvent(HoverEvent.showText(Msg.translate(locale, "customnpcs.commands.wiki" +
                                            ".hover")))
                    );
                    return 1;
                })
                .build();
        npcNode.addChild(wikiNode);
    }

    private static void registerTipCommand(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> wikiNode = LiteralArgumentBuilder.<CommandSourceStack>literal(
                        "disabletip")
                .requires(sender -> sender.getSender().hasPermission(PERMISSION_DISABLE_TIP))
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("tip", word())
                        .suggests((context, builder) -> {
                            String input = builder.getRemaining().toLowerCase();

                            Stream.of("edit", "name_reference")
                                    .filter(s -> s.toLowerCase().startsWith(input))
                                    .forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            if (!(sender instanceof Player player)) return 1;
                            Locale locale = getLocale(sender);
                            String arg = StringArgumentType.getString(context, "tip");
                            switch (arg) {
                                case "edit" -> {
                                    player.sendMessage(Msg.translate(locale, "customnpcs.commands.disabletip.edit"));
                                    player.getPersistentDataContainer().set(Utils.HIDE_EDIT_TIP,
                                            PersistentDataType.BOOLEAN, true);
                                }
                                case "name_reference" -> {
                                    player.sendMessage(Msg.translate(locale, "customnpcs.commands.disabletip" +
                                            ".name_reference"));
                                    player.getPersistentDataContainer().set(Utils.HIDE_NAME_REFERENCE,
                                            PersistentDataType.BOOLEAN, true);
                                }
                                default ->
                                        player.sendMessage(Msg.translate(locale, "customnpcs.commands.disabletip" +
                                                ".unknown_tip", arg));
                            }
                            return 1;
                        }))
                .build();
        npcNode.addChild(wikiNode);
    }

    private static void registerDebugCommand(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> debugNode = LiteralArgumentBuilder.<CommandSourceStack>literal("debug")
                .requires(sender -> sender.getSender().hasPermission(PERMISSION_DEBUG))
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    CustomNPCs plugin = CustomNPCs.getInstance();
                    if (plugin.isDebug()) {
                        sender.sendMessage(Msg.translate(getLocale(sender), "customnpcs.commands.debug.message" +
                                ".disable"));
                        plugin.setDebug(false);
                    } else {
                        sender.sendMessage(Msg.translate(getLocale(sender), "customnpcs.commands.debug.message" +
                                ".enable"));
                        plugin.setDebug(true);
                    }
                    return 1;
                })
                .build();
        npcNode.addChild(debugNode);
    }

    private static void registerFixConfigCommand(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> fixconfigNode = LiteralArgumentBuilder.<CommandSourceStack>literal(
                        "fixconfig")
                .requires(sender -> sender.getSender().hasPermission(PERMISSION_FIXCONFIG))
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    Locale locale = getLocale(sender);
                    sender.sendMessage(Msg.translate(locale, "customnpcs.commands.fix_config.usage"));
                    return 0;
                })
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("world")
                        .executes(NpcCommandRegistrar::executeFixWorldConfigs)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("world", word())
                                .suggests(WorldSuggester.SUGGESTIONS)
                                .executes(NpcCommandRegistrar::executeFixWorldConfigs)
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("strategy", word())
                                        .suggests((ctx, builder) -> {
                                            builder.suggest("NONE");
                                            builder.suggest("SAFE_LOCATION");
                                            return builder.buildFuture();
                                        })
                                        .executes(NpcCommandRegistrar::executeFixWorldConfigs)
                                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("target",
                                                        greedyString())
                                                .suggests(NpcBrokenSuggester.WORLD)
                                                .executes(NpcCommandRegistrar::executeFixWorldConfigs)
                                        )
                                )
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("lines")
                        .executes(NpcCommandRegistrar::executeFixLineConfigs)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("target", greedyString())
                                .suggests(NpcBrokenSuggester.LINES)
                                .executes(NpcCommandRegistrar::executeFixLineConfigs)
                        )
                )
                .build();
        npcNode.addChild(fixconfigNode);
    }

    private static void registerSetsoundCommand(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> setsoundNode = LiteralArgumentBuilder.<CommandSourceStack>literal(
                        "setsound")
                .requires(sender -> sender.getSender().hasPermission(PERMISSION_SETSOUND))
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("sound", greedyString())
                        .suggests(SoundSuggester.SUGGESTIONS)
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            if (!(sender instanceof Player player)) {
                                sender.sendMessage("You can't do this :P");
                                return 0;
                            }
                            String soundRaw = StringArgumentType.getString(context, "sound");
                            CustomNPCs plugin = CustomNPCs.getInstance();

                            if (plugin.isWaiting(player, WaitingType.SOUND)) {
                                String formatted = soundRaw.trim().toLowerCase();
                                if (Registry.SOUNDS.get(NamespacedKey.fromString(formatted)) == null) {
                                    player.sendMessage(Msg.translate(player.locale(), "customnpcs.commands.setsound" +
                                            ".unknown_sound"));
                                }

                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    plugin.waiting.remove(player.getUniqueId());
                                    dev.foxikle.customnpcs.actions.Action actionImpl =
                                            plugin.editingActions.get(player.getUniqueId());
                                    if (actionImpl instanceof dev.foxikle.customnpcs.actions.defaultImpl.PlaySound action) {
                                        action.setSound(formatted);
                                    } else {
                                        throw new IllegalArgumentException("Action " + actionImpl.getClass().getName() + " is not of type PlaySound");
                                    }
                                    player.sendMessage(Msg.translate(player.locale(), "customnpcs.commands.setsound" +
                                            ".success", Component.text(formatted)));
                                    plugin.getLotus().openMenu(player, actionImpl.getMenu());
                                });
                            } else {
                                player.sendMessage(Msg.translate(player.locale(), "customnpcs.commands.setsound" +
                                        ".was_not_waiting"));
                            }
                            return 1;
                        })
                        .build())
                .build();
        npcNode.addChild(setsoundNode);
    }

    private static int executeFixWorldConfigs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        CustomNPCs plugin = CustomNPCs.getInstance();
        FileManager fileManager = plugin.getFileManager();
        Locale locale = getLocale(sender);

        String worldArg = "";
        String strategyArg = "";
        String targetArg = "";

        try {
            worldArg = StringArgumentType.getString(context, "world");
        } catch (Exception ignored) {
        }

        try {
            strategyArg = StringArgumentType.getString(context, "strategy");
        } catch (Exception ignored) {
        }

        try {
            targetArg = StringArgumentType.getString(context, "target");
        } catch (Exception ignored) {
        }

        if (worldArg.isEmpty()) {
            sender.sendMessage(Msg.translate(locale, "customnpcs.commands.fix_config.usage"));
            return 0;
        }

        if (Bukkit.getWorld(worldArg) == null) {
            sender.sendMessage("INVALID_WORLD");
            return 0;
        }

        World w = Bukkit.getWorld(worldArg);
        assert w != null;

        dev.foxikle.customnpcs.internal.commands.enums.FixConfigWorldStrategy strat =
                dev.foxikle.customnpcs.internal.commands.enums.FixConfigWorldStrategy.parse(strategyArg);
        if (strat == null) {
            sender.sendMessage("INVALID_STRATEGY");
            return 0;
        }

        AtomicInteger totalFixed = new AtomicInteger(0);
        AtomicInteger movedbyStrategy = new AtomicInteger(0);
        AtomicInteger failedToFix = new AtomicInteger(0);
        AtomicInteger nonExistentNpcs = new AtomicInteger(0);

        if (targetArg.isEmpty() || targetArg.equalsIgnoreCase("all")) {
            for (UUID uuid : fileManager.getBrokenNPCs().get(BrokenReason.INVALID_WORLD).keySet()) {
                YamlConfiguration yml = fileManager.getNpcYaml();
                ConfigurationSection parent = yml.getConfigurationSection(uuid.toString());
                if (parent == null) {
                    nonExistentNpcs.incrementAndGet();
                    continue;
                }

                ConfigurationSection location = parent.getConfigurationSection("location");
                Location loc;
                String locString;

                if (location != null) {
                    double x = location.getDouble("x");
                    double y = location.getDouble("y");
                    double z = location.getDouble("z");
                    float pitch = (float) location.getDouble("pitch");
                    float yaw = (float) location.getDouble("yaw");
                    loc = new Location(w, x, y, z, pitch, yaw);
                    locString = "(" + x + "," + y + "," + z + ")";
                } else {
                    loc = new Location(w, 0, 0, 0, 0, 0);
                    locString = "(0, 0, 0)";
                    plugin.getLogger().warning("Fixed an NPC whose location data was wiped by Bukkit's configuration " +
                            "API. Its location was set to (0,0,0)");
                    sender.sendMessage(Msg.translate(locale, "customnpcs.commands.fix_config.bukkit_wiped_data"));
                }

                if (strat == dev.foxikle.customnpcs.internal.commands.enums.FixConfigWorldStrategy.SAFE_LOCATION) {
                    if (w.getBlockAt(loc).isSolid() || w.getBlockAt(loc.add(0, 1, 0)).isSolid()) {
                        RayTraceResult traceResult = w.rayTraceBlocks(loc.add(0, 329 - loc.y(), 0),
                                new Vector(0, -1, 0), 320D, FluidCollisionMode.NEVER);

                        if (traceResult == null) {
                            plugin.getLogger().warning("Failed to fix npc " + uuid + " at " + locString + " -- " +
                                    "Location cannot be made safe.");
                            failedToFix.incrementAndGet();
                            continue;
                        }
                        loc.setY(traceResult.getHitBlock().getY() + 1);
                    }
                    movedbyStrategy.incrementAndGet();
                }

                parent.set("location", loc);
                totalFixed.incrementAndGet();
                fileManager.saveNpcFile(yml);
            }
        } else {
            UUID uuid = null;
            for (Map.Entry<UUID, String> entry :
                    fileManager.getBrokenNPCs().get(BrokenReason.INVALID_WORLD).entrySet()) {
                if (entry.getValue().equals(targetArg)) {
                    uuid = entry.getKey();
                    break;
                }
            }

            if (uuid == null) {
                sender.sendMessage(Msg.translate(locale, "customnpcs.commands.invalid_name_or_uuid"));
                return 0;
            }

            YamlConfiguration yml = fileManager.getNpcYaml();
            ConfigurationSection parent = yml.getConfigurationSection(uuid.toString());
            if (parent == null) {
                return 0;
            }
            ConfigurationSection location = parent.getConfigurationSection("location");
            Location loc;
            String locString;

            if (location != null) {
                double x = location.getDouble("x");
                double y = location.getDouble("y");
                double z = location.getDouble("z");
                float pitch = (float) location.getDouble("pitch");
                float yaw = (float) location.getDouble("yaw");
                loc = new Location(w, x, y, z, pitch, yaw);
                locString = "(" + x + "," + y + "," + z + ")";
            } else {
                loc = new Location(w, 0, 0, 0, 0, 0);
                locString = "(0, 0, 0)";
                plugin.getLogger().warning("Fixed an NPC whose location data was wiped by Bukkit's configuration API." +
                        " Its location was set to (0,0,0)");
                sender.sendMessage(Msg.translate(locale, "customnpcs.commands.fix_config.bukkit_wiped_data"));
            }

            if (strat == dev.foxikle.customnpcs.internal.commands.enums.FixConfigWorldStrategy.SAFE_LOCATION) {
                if (w.getBlockAt(loc).isSolid() || w.getBlockAt(loc.add(0, 1, 0)).isSolid()) {
                    RayTraceResult traceResult = w.rayTraceBlocks(loc.add(0, 329 - loc.y(), 0),
                            new Vector(0, -1, 0), 320D, FluidCollisionMode.NEVER);

                    if (traceResult == null) {
                        plugin.getLogger().warning("Failed to fix npc " + uuid + " at " + locString + " -- Location " +
                                "cannot be made safe.");
                        failedToFix.incrementAndGet();
                        return 0;
                    }
                    loc.setY(traceResult.getHitBlock().getY() + 1);
                }
                movedbyStrategy.incrementAndGet();
            }

            parent.set("location", loc);
            totalFixed.incrementAndGet();
            fileManager.saveNpcFile(yml);
        }

        sender.sendMessage(Msg.translate(locale, "customnpcs.commands.fix_config.report",
                totalFixed.get(), movedbyStrategy.get(), failedToFix.get(), nonExistentNpcs.get()));
        return 1;
    }

    private static int executeFixLineConfigs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        CustomNPCs plugin = CustomNPCs.getInstance();
        FileManager fileManager = plugin.getFileManager();
        Locale locale = getLocale(sender);

        String targetArg = "";

        try {
            targetArg = StringArgumentType.getString(context, "target");
        } catch (Exception ignored) {
        }


        AtomicInteger totalFixed = new AtomicInteger(0);
        AtomicInteger movedbyStrategy = new AtomicInteger(0);
        AtomicInteger failedToFix = new AtomicInteger(0);
        AtomicInteger nonExistentNpcs = new AtomicInteger(0);

        if (targetArg.isEmpty() || targetArg.equalsIgnoreCase("all")) {
            for (UUID uuid : fileManager.getBrokenNPCs().get(BrokenReason.EMPTY_LINES).keySet()) {
                YamlConfiguration yml = fileManager.getNpcYaml();
                ConfigurationSection parent = yml.getConfigurationSection(uuid.toString());
                if (parent == null) {
                    nonExistentNpcs.incrementAndGet();
                    continue;
                }

                parent.set("lines", List.of("FIXME"));
                totalFixed.incrementAndGet();
                fileManager.saveNpcFile(yml);
            }
        } else {
            UUID uuid = null;
            try {
                uuid = UUID.fromString(targetArg);
            } catch (IllegalArgumentException ignored) {
            }


            if (uuid == null) {
                sender.sendMessage(Msg.translate(locale, "customnpcs.commands.invalid_name_or_uuid"));
                return 0;
            }

            YamlConfiguration yml = fileManager.getNpcYaml();
            ConfigurationSection parent = yml.getConfigurationSection(uuid.toString());
            if (parent == null) {
                return 0;
            }

            parent.set("lines", List.of("FIXME"));
            totalFixed.incrementAndGet();
            fileManager.saveNpcFile(yml);
        }

        sender.sendMessage(Msg.translate(locale, "customnpcs.commands.fix_config.report",
                totalFixed.get(), movedbyStrategy.get(), failedToFix.get(), nonExistentNpcs.get()));
        return 1;
    }

    private static Locale getLocale(CommandSender sender) {
        if (sender instanceof Player player) {
            return player.locale();
        }
        return Locale.getDefault();
    }
}