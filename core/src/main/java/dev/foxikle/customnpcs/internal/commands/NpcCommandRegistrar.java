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

import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.foxikle.customnpcs.conditions.Selector;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.commands.enums.FixConfigWorldStrategy;
import dev.foxikle.customnpcs.internal.commands.suggestion.NpcBrokenSuggester;
import dev.foxikle.customnpcs.internal.commands.suggestion.NpcSuggester;
import dev.foxikle.customnpcs.internal.commands.suggestion.SoundSuggester;
import dev.foxikle.customnpcs.internal.commands.suggestion.WorldSuggester;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.menu.MenuUtils;
import dev.foxikle.customnpcs.internal.storage.FileStorage;
import dev.foxikle.customnpcs.internal.storage.StorableNPC;
import dev.foxikle.customnpcs.internal.storage.StorageManager;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.Utils;
import dev.foxikle.customnpcs.internal.utils.WaitingType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minestom.server.codec.Transcoder;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;
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
    private static final String PERMISSION_MOVEDATA = "customnpcs.commands.movedata";
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
        registerMoveData(npcNode);

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
                            new ArrayList<>(),
                            new ArrayList<>(),
                            Selector.ONE
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
                            CustomNPCs plugin = CustomNPCs.getInstance();
                            InternalNpc finalNpc = plugin.getNPCByID(uuid);
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                InternalNpc newNpc = plugin.createNPC(
                                        player.getWorld(),
                                        finalNpc.getSpawnLoc(),
                                        finalNpc.getEquipment(),
                                        finalNpc.getSettings(),
                                        finalNpc.getUniqueID(),
                                        null,
                                        finalNpc.getActions(),
                                        finalNpc.getInjectionConditions(),
                                        finalNpc.getInjectionSelector()
                                );
                                plugin.getEditingNPCs().put(player.getUniqueId(), newNpc);
                                plugin.getLotus().openMenu(player, MenuUtils.NPC_MAIN);
                            }, 1);
                            return 1;
                        })
                        .build())
                .build();
        npcNode.addChild(editNode);
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
        LiteralCommandNode<CommandSourceStack> n = LiteralArgumentBuilder.<CommandSourceStack>literal("fixconfig")
                .requires(sender -> sender.getSender().hasPermission(PERMISSION_FIXCONFIG))
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    Locale locale = getLocale(sender);
                    sender.sendMessage(Msg.translate(locale, "customnpcs.commands.fix_config.usage"));
                    return 0;
                })
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("world")
                        .executes(NpcCommandRegistrar::executeFixConfig)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("world", word())
                                .suggests(WorldSuggester.SUGGESTIONS)
                                .executes(NpcCommandRegistrar::executeFixConfig)
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("strategy", word())
                                        .suggests((ctx, builder) -> {
                                            builder.suggest("NONE");
                                            builder.suggest("SAFE_LOCATION");
                                            return builder.buildFuture();
                                        })
                                        .executes(NpcCommandRegistrar::executeFixConfig)
                                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("target",
                                                        greedyString())
                                                .suggests(NpcBrokenSuggester.SUGGESTIONS)
                                                .executes(NpcCommandRegistrar::executeFixConfig)
                                        )
                                )
                        )
                )
                .build();
        npcNode.addChild(n);
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

    private static void registerMoveData(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> moveDataNode =
                LiteralArgumentBuilder.<CommandSourceStack>literal("movedata")
                        .requires(sender -> sender.getSender().hasPermission(PERMISSION_MOVEDATA))
                        .executes(ctx -> {
                            ctx.getSource().getSender().sendMessage(
                                    Msg.translate(
                                            CommandUtils.getLocale(ctx.getSource().getSender()),
                                            "customnpcs.commands.movedata.invalid_operation"
                                    )
                            );
                            return 1;
                        })
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("operation", word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("MERGE_LOCAL");
                                    builder.suggest("MERGE_REMOTE");
                                    builder.suggest("OVERWRITE");
                                    return builder.buildFuture();
                                })

                                // /npc movedata <operation>
                                .executes(ctx -> {
                                    String operation = StringArgumentType.getString(ctx, "operation");
                                    if (!(operation.equalsIgnoreCase("MERGE_LOCAL") || operation.equalsIgnoreCase(
                                            "MERGE_REMOTE") || operation.equalsIgnoreCase("OVERWRITE"))) {
                                        ctx.getSource().getSender().sendMessage(Msg.translate(
                                                CommandUtils.getLocale(ctx.getSource().getSender()),
                                                "customnpcs.commands.movedata.invalid_operation"));
                                        return 0;
                                    }
                                    ctx.getSource().getSender().sendMessage(
                                            Msg.translate(CommandUtils.getLocale(ctx.getSource().getSender()),
                                                    "customnpcs.commands.movedata.need_confirm"));

                                    return 1;
                                })

                                // /npc movedata <operation> confirm
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("operation",
                                                greedyString())
                                        .executes(ctx -> executeMoveData(
                                                ctx.getSource().getSender(),
                                                StringArgumentType.getString(ctx, "operation"),
                                                Utils.list(StringArgumentType.getString(ctx, "flags").toLowerCase().split(" "))
                                        ))
                                )
                        )
                        .build();

        npcNode.addChild(moveDataNode);
    }

    private static int executeMoveData(CommandSender source, String operation, List<String> flags) {
        if (!(operation.equalsIgnoreCase("MERGE_LOCAL") || operation.equalsIgnoreCase("MERGE_REMOTE") || operation.equalsIgnoreCase("OVERWRITE"))) {
            source.sendMessage(Msg.translate(
                    CommandUtils.getLocale(source),
                    "customnpcs.commands.movedata.invalid_operation"
            ));
            return 0;
        }

        if (!(flags.contains("--confirm"))) {
            source.sendMessage(Msg.translate(CommandUtils.getLocale(source),
                    "customnpcs.commands.movedata.need_confirm"
            ));
            return 0;
        }

        switch (operation.toUpperCase()) {
            case "MERGE_LOCAL" -> merge(source, true);
            case "MERGE_REMOTE" -> merge(source, false);
            case "OVERWRITE" -> overwrite(source);
        }

        return 1;
    }

    private static boolean start(CommandSender source) {
        source.sendMessage(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata" +
                ".operation_queued"));

        if ((CustomNPCs.getInstance().getStorageManager().getStorage() instanceof FileStorage)) {
            CustomNPCs.getInstance().getLogger().log(Level.WARNING, "The current file provider is already File " +
                    "storage!");
            source.sendMessage(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata" +
                    ".operation_failure"));
            return true;
        }

        if (!FileStorage.FILE.exists()) {
            CustomNPCs.getInstance().getLogger().log(Level.SEVERE, "The local NPC data file does not exist!",
                    new IllegalStateException());
            source.sendMessage(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata" +
                    ".operation_failure"));
            return true;
        }
        return false;
    }

    private static void merge(CommandSender source, boolean discardRemote) {
        CustomNPCs plugin = CustomNPCs.getInstance();
        if (start(source)) {
            return;
        }

        StorageManager sm = plugin.getStorageManager();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            byte[] currentData;
            try (FileInputStream fis = new FileInputStream(FileStorage.FILE)) {
                currentData = fis.readAllBytes();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "An error occurred while reading local NPC file", e);
                source.sendMessage(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata" +
                        ".operation_failure"));
                return;
            }

            List<StorableNPC> local;
            try {
                local = StorageManager.NPCS_CODEC.decode(Transcoder.JSON,
                        JsonParser.parseString(new String(currentData))).orElseThrow();
            } catch (IllegalStateException e) {
                throw new RuntimeException(e);
            }
            sm.getAllNpcs().whenComplete((remote, throwable) -> {
                if (throwable != null) {
                    plugin.getLogger().log(Level.SEVERE, "An error occurred while fetching remote NPC data!",
                            throwable);
                    source.sendMessage(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata" +
                            ".operation_failure"));
                    return;
                }

                List<StorableNPC> merged = Stream.concat(remote.stream(), local.stream())
                        .collect(Collectors.toMap(
                                StorableNPC::getUniqueID, npc -> npc, (t, t2) -> discardRemote ? t : t2)
                        ).values().stream().toList();
                finalizeMove(source, plugin, sm, merged);
            });
        });
    }

    private static void overwrite(CommandSender source) {
        CustomNPCs plugin = CustomNPCs.getInstance();
        if (start(source)) {
            return;
        }

        StorageManager sm = plugin.getStorageManager();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            byte[] currentData;
            try (FileInputStream fis = new FileInputStream(FileStorage.FILE)) {
                currentData = fis.readAllBytes();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "An error occurred while reading local NPC file", e);
                source.sendMessage(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata" +
                        ".operation_failure"));
                return;
            }

            List<StorableNPC> toWrite;
            try {
                toWrite = StorageManager.NPCS_CODEC.decode(Transcoder.JSON,
                        JsonParser.parseString(new String(currentData))).orElseThrow();
            } catch (IllegalStateException e) {
                throw new RuntimeException(e);
            }
            finalizeMove(source, plugin, sm, toWrite);
        });
    }

    private static void finalizeMove(CommandSender source, CustomNPCs plugin, StorageManager sm,
                                     List<StorableNPC> toWrite) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            sm.resetTracked();

            // remove the npcs
            plugin.getNPCs().forEach(InternalNpc::remove);
            plugin.npcs.clear();

            for (StorableNPC npc : toWrite) {
                sm.track(npc);
                sm.loadStorable(npc);
            }

            sm.saveNpcs().whenComplete((aBoolean, throwable1) -> {
                if (throwable1 != null) {
                    plugin.getLogger().log(Level.SEVERE, "An error occurred while writing remote NPC data!",
                            throwable1);
                    source.sendMessage(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata" +
                            ".operation_failure"));
                    return;
                }
                source.sendMessage(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata" +
                        ".operation_success"));
            });
        });
    }

    private static int executeFixConfig(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        CustomNPCs plugin = CustomNPCs.getInstance();
        StorageManager fileManager = plugin.getStorageManager();
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

        FixConfigWorldStrategy strat = FixConfigWorldStrategy.parse(strategyArg);
        if (strat == null) {
            sender.sendMessage("INVALID_STRATEGY");
            return 0;
        }

        AtomicInteger totalFixed = new AtomicInteger(0);
        AtomicInteger movedbyStrategy = new AtomicInteger(0);
        AtomicInteger failedToFix = new AtomicInteger(0);
        AtomicInteger nonExistentNpcs = new AtomicInteger(0);

        if (targetArg.isEmpty() || targetArg.equalsIgnoreCase("all")) {
            for (UUID uuid : fileManager.getBrokenNPCs().keySet()) {
                StorableNPC npc = fileManager.getBrokenNPCs().get(uuid);

                StorableNPC.StorableLocation loc = npc.getSpawnLoc();
                String locString;


                if (loc != null) {
                    loc = loc.withWorld(w.getName());
                    locString = "(" + loc.x() + "," + loc.y() + "," + loc.z() + ")";
                } else {
                    loc = StorableNPC.StorableLocation.convert(new Location(w, 0, 0, 0, 0, 0));
                    locString = "(0, 0, 0)";
                    plugin.getLogger().warning("Fixed an NPC whose location data was wiped by Bukkit's configuration " +
                            "API. Its location was set to (0,0,0)");
                    sender.sendMessage(Msg.translate(locale, "customnpcs.commands.fix_config.bukkit_wiped_data"));
                }

                Location loc2 = loc.convert();
                if (strat == FixConfigWorldStrategy.SAFE_LOCATION) {
                    if (w.getBlockAt(loc2).isSolid() || w.getBlockAt(loc2.add(0, 1, 0)).isSolid()) {
                        RayTraceResult traceResult = w.rayTraceBlocks(loc2.add(0, 329 - loc.y(), 0),
                                new Vector(0, -1, 0), 320D, FluidCollisionMode.NEVER);

                        if (traceResult == null) {
                            plugin.getLogger().warning("Failed to fix npc " + uuid + " at " + locString + " -- " +
                                    "Location cannot be made safe.");
                            failedToFix.incrementAndGet();
                            continue;
                        }
                        loc2.setY(traceResult.getHitBlock().getY() + 1);
                    }
                    movedbyStrategy.incrementAndGet();
                }

                npc.setSpawnLoc(StorableNPC.StorableLocation.convert(loc2));
                fileManager.getValidNPCs().add(npc.getUniqueID());
                fileManager.getBrokenNPCs().remove(npc.getUniqueID());
                fileManager.track(npc);
                totalFixed.incrementAndGet();
            }
            fileManager.saveNpcs();
        } else {
            UUID uuid = null;
            for (Map.Entry<UUID, StorableNPC> entry : fileManager.getBrokenNPCs().entrySet()) {
                if (entry.getValue().getUniqueID().toString().equals(targetArg)) {
                    uuid = entry.getKey();
                    break;
                }
            }

            if (uuid == null) {
                sender.sendMessage(Msg.translate(locale, "customnpcs.commands.invalid_name_or_uuid"));
                return 0;
            }

            StorableNPC npc = fileManager.getBrokenNPCs().get(uuid);

            StorableNPC.StorableLocation loc = npc.getSpawnLoc();
            String locString;
            Location loc2;

            if (loc != null) {
                loc = loc.withWorld(w.getName());
                locString = "(" + loc.x() + "," + loc.y() + "," + loc.z() + ")";
            } else {
                loc = StorableNPC.StorableLocation.convert(new Location(w, 0, 0, 0, 0, 0));
                locString = "(0, 0, 0)";
                plugin.getLogger().warning("Fixed an NPC whose location data was wiped by Bukkit's configuration API." +
                        " Its location was set to (0,0,0)");
                sender.sendMessage(Msg.translate(locale, "customnpcs.commands.fix_config.bukkit_wiped_data"));
            }
            loc2 = loc.convert();

            if (strat == dev.foxikle.customnpcs.internal.commands.enums.FixConfigWorldStrategy.SAFE_LOCATION) {
                if (w.getBlockAt(loc2).isSolid() || w.getBlockAt(loc2.add(0, 1, 0)).isSolid()) {
                    RayTraceResult traceResult = w.rayTraceBlocks(loc2.add(0, 329 - loc2.y(), 0),
                            new Vector(0, -1, 0), 320D, FluidCollisionMode.NEVER);

                    if (traceResult == null) {
                        plugin.getLogger().warning("Failed to fix npc " + uuid + " at " + locString + " -- Location " +
                                "cannot be made safe.");
                        failedToFix.incrementAndGet();
                        return 0;
                    }
                    loc2.setY(traceResult.getHitBlock().getY() + 1);
                }
                movedbyStrategy.incrementAndGet();
            }
            npc.setSpawnLoc(StorableNPC.StorableLocation.convert(loc2));
            fileManager.getValidNPCs().add(npc.getUniqueID());
            fileManager.getBrokenNPCs().remove(npc.getUniqueID());
            fileManager.track(npc);
            totalFixed.incrementAndGet();
        }

        fileManager.saveNpcs();
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