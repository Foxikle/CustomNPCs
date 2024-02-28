package dev.foxikle.customnpcs.internal.commands;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.data.Settings;
import dev.foxikle.customnpcs.internal.CustomNPCs;
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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            if (args.length == 0) {
                player.performCommand("npc help");
                return true;
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("help")) {
                    if (!player.hasPermission("customnpcs.commands.help")) {
                        player.sendMessage(Utils.style("&cYou lack the propper permissions to execute this."));
                        return true;
                    }
                    player.sendMessage(Utils.style("""
                            &2&m                      &r&3&l Custom NPCs &r&7[&8v${version}&7] &r&2&m                      \s
                            &r                                 &r&6By Foxikle \n
                                                        
                            """).replace("${version}", plugin.getDescription().getVersion()));


                    player.sendMessage(getHelpComponent());
                } else if (args[0].equalsIgnoreCase("manage")) {
                    if (!player.hasPermission("customnpcs.commands.manage")) {
                        player.sendMessage(Utils.style("&cYou lack the propper permissions to manage npcs."));
                        return true;
                    }
                    if (plugin.getNPCs().isEmpty()) {
                        player.sendMessage(Utils.style("&cThere are no npcs to manage!"));
                        return true;
                    }
                    player.sendMessage(Utils.style("""
                            &2&m                           &r&3&l Manage NPCs  &r&2&m                           \s
                            &r                                 \n
                                                        
                            """));
                    Component message = Component.empty();
                    for (InternalNpc npc : plugin.getNPCs()) {
                        if (npc.getSettings().isResilient()) {
                            Component name = Component.text("  ")
                                    .append(plugin.getMiniMessage().deserialize(npc.getSettings().getName())
                                            .hoverEvent(HoverEvent.showText(Component.text("Click to copy UUID", NamedTextColor.GREEN)))
                                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, npc.getUniqueID().toString()))
                                    ).append(Component.text(" [EDIT]", NamedTextColor.YELLOW, TextDecoration.BOLD)
                                            .hoverEvent(HoverEvent.showText(Component.text("Click to edit NPC", NamedTextColor.DARK_AQUA)))
                                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/npc edit " + npc.getUniqueID()))
                                    ).append(Component.text(" [DELETE]", NamedTextColor.RED, TextDecoration.BOLD)
                                            .hoverEvent(HoverEvent.showText(Component.text("Click to permanantly delete NPC", NamedTextColor.DARK_RED, TextDecoration.BOLD)))
                                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc delete " + npc.getUniqueID()))
                                    ).appendNewline();
                            message = message.append(name);
                        }
                    }
                    player.sendMessage(message);


                } else if (args[0].equalsIgnoreCase("new")) {
                    player.performCommand("npc create");
                } else if (args[0].equalsIgnoreCase("list")) {
                    player.performCommand("npc manage");
                } else if (args[0].equalsIgnoreCase("clear_holograms")) {
                    if (player.hasPermission("customnpcs.commands.removeHolograms")) {
                        AtomicInteger stands = new AtomicInteger();
                        player.getWorld().getEntities().forEach(entity -> {
                            if (entity.getScoreboardTags().contains("npcHologram")) {
                                entity.remove();
                                stands.getAndIncrement();
                            }
                        });
                        player.sendMessage((stands.get() == 1) ? Utils.style("&aSuccessfully removed 1 npc hologram.") : Utils.style("&aSuccessfully removed " + stands.get() + " npc holograms."));
                    } else {
                        player.sendMessage(Utils.style("&cYou lack the propper permissions to remove npc holograms."));
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("create")) {
                    if (!player.hasPermission("customnpcs.create")) {
                        player.sendMessage(Utils.style("&cYou lack the propper permissions to create npcs."));
                        return true;
                    }
                    UUID uuid = UUID.randomUUID();
                    InternalNpc npc = plugin.createNPC(player.getWorld(), player.getLocation(), new Equipment(), new Settings(), uuid, null, new ArrayList<>());
                    MenuCore mc = new MenuCore(npc, plugin);
                    plugin.menuCores.put(player, mc);
                    plugin.pages.put(player, 0);
                    mc.getMainMenu().open(player);
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (!player.hasPermission("customnpcs.commands.reload")) {
                        player.sendMessage(Utils.style("&cYou lack the propper permissions to reload npcs."));
                        return true;
                    }
                    player.sendMessage(Utils.style("&eReloading NPCs!"));
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
                    player.sendMessage(Utils.style("&aNPCs successfully reloaded."));
                } else if (args[0].equalsIgnoreCase("wiki") || args[0].equalsIgnoreCase("docs")) {
                    player.sendMessage(
                            Component.text("Click ", NamedTextColor.YELLOW)
                                    .append(Component.text("[HERE]", NamedTextColor.AQUA)
                                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://docs.foxikle.dev")))
                                    .append(Component.text(" for the CustomNPCs Wiki!", NamedTextColor.YELLOW))
                    );
                }
            } else {
                if (args[0].equalsIgnoreCase("setsound")) {
                    if (plugin.soundWaiting.contains(player)) {
                        try {
                            Sound.valueOf(args[1]);
                        } catch (IllegalArgumentException ex) {
                            player.sendMessage(Utils.style("&cUnrecognised sound, please use tab completions."));
                            return true;
                        }
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            plugin.soundWaiting.remove(player);
                            List<String> argsCopy = plugin.editingActions.get(player).getArgsCopy();
                            Action action = plugin.editingActions.get(player);
                            List<String> arg = action.getArgs();
                            arg.clear();
                            arg.add(0, argsCopy.get(0));
                            arg.add(1, argsCopy.get(1));
                            arg.add(2, args[1]);
                            player.sendMessage(Utils.style("&aSuccessfully set sound to be '&r") + args[1] + Utils.style("&a'"));
                            plugin.menuCores.get(player).getActionCustomizerMenu(action).open(player);
                        });
                    } else {
                        player.sendMessage(Utils.style("&cUnccessfully set NPC sound. I wasn't waiting for a response. Please contact Foxikle if you think this is a mistake."));
                    }
                } else {
                    UUID uuid = null;
                    InternalNpc npc;
                    try {
                        uuid = UUID.fromString(args[1]);
                        if (plugin.getNPCByID(uuid) == null) {
                            player.sendMessage(Utils.style("&cThe UUID provided does not match any NPC."));
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
                            player.sendMessage(Utils.style("&cInvalid UUID or Name provided."));
                            return false;
                        } else if (uuids.size() > 1) {
                            double value = Double.MAX_VALUE;
                            for (UUID id : uuids) {
                                double ds = plugin.getNPCByID(id).getCurrentLocation().distanceSquared(player.getLocation());
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
                            player.sendMessage(Utils.style("&cThe UUID provided does not match any NPC."));
                            return true;
                        }
                        npc = plugin.getNPCByID(uuid);
                    }
                    switch (args[0].toLowerCase()) {
                        case "delete" -> {
                            if (!player.hasPermission("customnpcs.delete")) {
                                player.sendMessage(Utils.style("&cYou lack the propper permissions to delete npcs."));
                                return true;
                            }
                            MenuUtils.getDeletionConfirmationMenu(npc, null).open(player);
                        }
                        case "edit" -> {
                            if (!player.hasPermission("customnpcs.edit")) {
                                player.sendMessage(Utils.style("&cYou lack the propper permissions to edit npcs."));
                                return true;
                            }

                            InternalNpc finalNpc = npc;
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                InternalNpc newNpc = plugin.createNPC(player.getWorld(), finalNpc.getSpawnLoc(), finalNpc.getEquipment(), finalNpc.getSettings(), finalNpc.getUniqueID(), null, finalNpc.getActions());
                                MenuCore mc = new MenuCore(newNpc, plugin);
                                plugin.menuCores.put(player, mc);
                                mc.getMainMenu().open(player);
                            }, 1);
                        }
                        case "goto" -> {
                            if(!player.hasPermission("customnpcs.commands.goto")) {
                                player.sendMessage(Utils.style("&cYou lack the propper permissions to go to npcs."));
                                return true;
                            }
                            player.teleportAsync(npc.getCurrentLocation());
                        }
                        case "clone" -> {
                            if(!player.hasPermission("customnpcs.commands.clone")) {
                                player.sendMessage(Utils.style("&cYou lack the propper permissions to clone npcs."));
                                return true;
                            }
                            InternalNpc newNpc = npc.clone();
                            newNpc.setSpawnLoc(player.getLocation());
                            newNpc.getSettings().setDirection(player.getLocation().getYaw());
                            newNpc.createNPC();
                            player.sendMessage(Utils.style("&aNPC successfully cloned!"));

                            // runnable
                        }
                        case "movehere" -> {
                            if(!player.hasPermission("customnpcs.commands.movehere")) {
                                player.sendMessage(Utils.style("&cYou lack the propper permissions to clone npcs."));
                                return true;
                            }
                            npc.remove();
                            npc.setSpawnLoc(player.getLocation());
                            npc.getSettings().setDirection(player.getLocation().getYaw());
                            npc.createNPC();
                            player.sendMessage(Utils.style("&e&oPsst! After moving NPCs, you should reload them!"));
                        }
                        default -> sender.sendMessage(Utils.style("&cUnrecognised sub-command. Use '/npc help' for a list of supported commands."));
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

            if (!silent) sender.sendMessage(Utils.style("&eReloading NPCs!"));
            try {
                Bukkit.getScoreboardManager().getMainScoreboard().getTeam("npc").unregister();
            } catch (IllegalArgumentException ignored) {
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
            if (!silent) sender.sendMessage(Utils.style("&aNPCs successfully reloaded."));
        }
        return false;
    }

    @NotNull
    private Component getHelpComponent() {
        Component component = Component.empty();
        component = component.append(Component.text("\n\n- /npc help  ", NamedTextColor.GOLD)
                .hoverEvent(HoverEvent.showText(Component.text("Displays this message.", NamedTextColor.WHITE)))
                .append(Component.text("Displays this message.", NamedTextColor.AQUA))
                .appendNewline()
                .append(Component.text("- /npc manage  ", NamedTextColor.GOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Displays the current NPCs with buttons to edit or delete them.", NamedTextColor.WHITE)))
                )
                .append(Component.text("Displays the current NPCs", NamedTextColor.AQUA))
                .appendNewline()
                .append(Component.text("- /npc create  ", NamedTextColor.GOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Opens the NPC customizer", NamedTextColor.WHITE)))
                )
                .append(Component.text("Creates a new NPC", NamedTextColor.AQUA))
                .appendNewline()
                .append(Component.text("- /npc delete <NPC>  ", NamedTextColor.GOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Permanantly deletes the NPC", NamedTextColor.DARK_RED)))
                )
                .append(Component.text("Permanantly deletes the NPC  ", NamedTextColor.AQUA))
                .appendNewline()
                .append(Component.text("- /npc edit <NPC>  ", NamedTextColor.GOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Brings up the NPC edit dialogue", NamedTextColor.WHITE)))
                )
                .append(Component.text("Edits the specified NPC", NamedTextColor.AQUA))
                .appendNewline()
                .append(Component.text("- /npc create  ", NamedTextColor.GOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Opens the NPC customizer", NamedTextColor.WHITE)))
                )
                .append(Component.text("Creates a new NPC  ", NamedTextColor.AQUA))
                .appendNewline()
                .append(Component.text("- /npc movehere <NPC> ", NamedTextColor.GOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Moves the NPC to your current location. Its pitch is set to your current pitch. Its yaw is also set to your current yaw.", NamedTextColor.WHITE)))
                )
                .append(Component.text("Moves the NPC to you.  ", NamedTextColor.AQUA))
                .appendNewline()
                .append(Component.text("- /npc clone <NPC>  ", NamedTextColor.GOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Clones the NPC, and moves it to your current location. Its pitch is set to your current pitch. Its yaw is also set to your current yaw.", NamedTextColor.WHITE)))
                )
                .append(Component.text("Clones the NPC and moves it to you.  ", NamedTextColor.AQUA))
                .appendNewline()
                .append(Component.text("- /npc clear_holograms  ", NamedTextColor.GOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Deletes ALL NPC holograms. (Includes holograms without NPCs correlated to them)", NamedTextColor.WHITE)))
                )
                .append(Component.text("Forcfully deletes NPC holograms", NamedTextColor.AQUA))
                .appendNewline()
                .append(Component.text("- /npc reload  ", NamedTextColor.GOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Reloads the plugin and config", NamedTextColor.WHITE)))
                )
                .append(Component.text("Reloads CustomNPCs", NamedTextColor.AQUA))
                .appendNewline()
                .append(Component.text("- /npc goto <NPC>  ", NamedTextColor.GOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Teleports you to the specified NPC.", NamedTextColor.WHITE)))
                )
                .append(Component.text("Teleports you to the specifiec NPC.  ", NamedTextColor.AQUA))
                .appendNewline()
                .append(Component.text("                                                                                 ", NamedTextColor.DARK_GREEN, TextDecoration.STRIKETHROUGH)));
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
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("help");
            list.add("manage");
            list.add("create");
            list.add("delete");
            list.add("edit");
            list.add("reload");
            list.add("goto");
            list.add("clear_holograms");
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
            plugin.npcs.forEach((uuid, npc) -> {
                list.add(plugin.getMiniMessage().stripTags(npc.getSettings().getName()));
            });
        }
        return list;
    }
}
