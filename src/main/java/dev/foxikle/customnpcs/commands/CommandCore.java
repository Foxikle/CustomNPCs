package dev.foxikle.customnpcs.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.foxikle.customnpcs.Action;
import dev.foxikle.customnpcs.CustomNPCs;
import dev.foxikle.customnpcs.menu.MenuCore;
import dev.foxikle.customnpcs.NPC;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.bukkit.ChatColor.RED;

public class CommandCore implements CommandExecutor, TabCompleter {
    
    private final CustomNPCs plugin;

    public CommandCore(CustomNPCs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!label.equalsIgnoreCase("npc")) return false;
        if(sender instanceof Player player) {
            if (args.length == 0) {
                player.performCommand("npc help");
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("help")) {
                    if(!player.hasPermission("customnpcs.commands.help")){
                        player.sendMessage(RED + "You lack the propper permissions to execute this.");
                        return true;
                    }
                    player.sendMessage(ChatColor.translateAlternateColorCodes('§', """
                            §2§m                     §r§3§l Custom NPCs §r§7[§8v1.3-PRE3§7] §r§2§m                      \s
                            §r                                 §r§6By Foxikle \n
                            
                            """));
                    BaseComponent[] space = new ComponentBuilder(" : ").color(net.md_5.bungee.api.ChatColor.WHITE).create();
                    ComponentBuilder help = new ComponentBuilder("\n\n  -  /npc help").color(net.md_5.bungee.api.ChatColor.YELLOW).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Displays this message").color(net.md_5.bungee.api.ChatColor.AQUA).create())).append(space).append(new ComponentBuilder("Displays this message").color(net.md_5.bungee.api.ChatColor.AQUA).create());
                    ComponentBuilder manage = new ComponentBuilder("\n  -  /npc manage").color(net.md_5.bungee.api.ChatColor.YELLOW).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Displays the current NPCs").color(net.md_5.bungee.api.ChatColor.AQUA).create())).append(space).append(new ComponentBuilder("Displays the current NPCs").color(net.md_5.bungee.api.ChatColor.AQUA).create());
                    ComponentBuilder create = new ComponentBuilder("\n  -  /npc create").color(net.md_5.bungee.api.ChatColor.YELLOW).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Displays a menu to create an NPC").color(net.md_5.bungee.api.ChatColor.AQUA).create())).append(space).append(new ComponentBuilder("Displays a menu to create an NPC").color(net.md_5.bungee.api.ChatColor.AQUA).create());
                    ComponentBuilder delete = new ComponentBuilder("\n  -  /npc delete <UUID>").color(net.md_5.bungee.api.ChatColor.YELLOW).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Deletes the specified NPC").color(net.md_5.bungee.api.ChatColor.AQUA).create())).append(space).append(new ComponentBuilder("Deletes the specified NPC").color(net.md_5.bungee.api.ChatColor.AQUA).create());
                    ComponentBuilder edit   = new ComponentBuilder("\n  -  /npc edit <UUID>").color(net.md_5.bungee.api.ChatColor.YELLOW).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Displays a menu to edit the NPC").color(net.md_5.bungee.api.ChatColor.AQUA).create())).append(space).append(new ComponentBuilder("Displays a menu to edit the NPC").color(net.md_5.bungee.api.ChatColor.AQUA).create());
                    ComponentBuilder remove   = new ComponentBuilder("\n  -  /npc clear_holograms").color(net.md_5.bungee.api.ChatColor.YELLOW).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Forcibly removes NPC holograms.").color(net.md_5.bungee.api.ChatColor.AQUA).create())).append(space).append(new ComponentBuilder("Forcibly removes NPC holograms.").color(net.md_5.bungee.api.ChatColor.AQUA).create());
                    ComponentBuilder reload   = new ComponentBuilder("\n  -  /npc reload").color(net.md_5.bungee.api.ChatColor.YELLOW).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Reloads the plugin.").color(net.md_5.bungee.api.ChatColor.AQUA).create())).append(space).append(new ComponentBuilder("Reloads the NPCs and plugin.").color(net.md_5.bungee.api.ChatColor.AQUA).create());

                    ComponentBuilder close  = new ComponentBuilder("\n§2§m                                                                                ");
                    help.append(manage.create()).append(create.create()).append(delete.create()).append(edit.create()).append(remove.create()).append(reload.create()).append(close.create());
                    player.spigot().sendMessage(help.create());
                } else if (args[0].equalsIgnoreCase("manage")) {
                    if(!player.hasPermission("customnpcs.commands.manage")){
                        player.sendMessage(RED + "You lack the propper permissions to manage npcs.");
                        return true;
                    }
                    if(plugin.getNPCs().isEmpty()) {
                        player.sendMessage(RED + "There are no npcs to manage!");
                        return true;
                    }
                    player.sendMessage(ChatColor.translateAlternateColorCodes('§', """
                            §2§m                           §r§3§l Manage NPCs  §r§2§m                           \s
                            §r                                 \n
                            
                            """));
                    ComponentBuilder message = new ComponentBuilder();
                    for (NPC npc : plugin.getNPCs()) {
                        if (npc.isResilient()) {
                            ComponentBuilder name = new ComponentBuilder("  " + npc.getHologramName() + " §r▸").event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, npc.getUUID().toString())).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to copy UUID").color(net.md_5.bungee.api.ChatColor.YELLOW).create())).append(new ComponentBuilder(" ").color(net.md_5.bungee.api.ChatColor.WHITE).bold(false).create())
                                    .append(new ComponentBuilder(" [EDIT]").color(net.md_5.bungee.api.ChatColor.YELLOW).bold(true).event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc edit " + npc.getUUID().toString())).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to edit npc").color(net.md_5.bungee.api.ChatColor.YELLOW).create())).create()).append(new ComponentBuilder(" ").color(net.md_5.bungee.api.ChatColor.WHITE).bold(false).create())
                                    .append(new ComponentBuilder(" [DELETE]").color(net.md_5.bungee.api.ChatColor.RED).bold(true).event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc delete " + npc.getUUID().toString())).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to delete npc").color(net.md_5.bungee.api.ChatColor.YELLOW).create())).create()).append(new ComponentBuilder(" ").color(net.md_5.bungee.api.ChatColor.WHITE).bold(false).create())
                                    .append("\n");
                            message.append(name.create());
                        }
                    }
                    player.spigot().sendMessage(message.create());


                } else if (args[0].equalsIgnoreCase("new")) {
                    player.performCommand("npc create");
                } else if (args[0].equalsIgnoreCase("list")) {
                    player.performCommand("npc manage");
                } else if (args[0].equalsIgnoreCase("clear_holograms")) {
                    if(player.hasPermission("customnpcs.commands.removeHolograms")){
                        AtomicInteger stands = new AtomicInteger();
                        player.getWorld().getEntities().forEach(entity -> {
                            if(entity.getScoreboardTags().contains("npcHologram")){
                                entity.remove();
                                stands.getAndIncrement();
                            }
                        });
                        player.sendMessage((stands.get() == 1) ? ChatColor.GREEN + "Successfully removed " + stands.get() + " npc hologram." : ChatColor.GREEN + "Successfully removed " + stands.get() + " npc holograms.");
                    } else {
                        player.sendMessage(RED + "You lack the propper permissions to remove npc holograms.");
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("create")) {
                    if(!player.hasPermission("customnpcs.create")){
                        player.sendMessage(RED + "You lack the propper permissions to create npcs.");
                        return true;
                    }
                    GameProfile profile = new GameProfile(UUID.randomUUID(), "nothing");
                    profile.getProperties().removeAll("textures");
                    profile.getProperties().put("textures", new Property("textures", null, null));
                    MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
                    ServerLevel nmsWorld = ((CraftWorld) player.getWorld()).getHandle();
                    NPC npc = new NPC(plugin, nmsServer, nmsWorld, profile,  player.getLocation(), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), true, true,  "not set", UUID.randomUUID(), "", "", "not set", 180, null, new ArrayList<>());
                    MenuCore mc = new MenuCore(npc, plugin);
                    plugin.menuCores.put(player, mc);
                    plugin.pages.put(player, 0);
                    player.openInventory(mc.getMainMenu());
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if(!player.hasPermission("customnpcs.commands.reload")){
                        player.sendMessage(RED + "You lack the propper permissions to reload npcs.");
                        return true;
                    }
                    player.sendMessage(ChatColor.YELLOW + "Reloading NPCs!");
                    try {
                        Bukkit.getScoreboardManager().getMainScoreboard().getTeam("npc").unregister();
                    } catch (IllegalArgumentException ignored) {}
                    HandlerList.unregisterAll(plugin);
                    List<NPC> npcs = new ArrayList<>(plugin.npcs.values());
                    for (NPC npc : npcs) {
                        plugin.npcs.remove(npc.getUUID());
                        npc.remove();
                    }
                    plugin.npcs.clear();
                    plugin.holograms.clear();
                    plugin.onEnable();
                    player.sendMessage(ChatColor.GREEN + "NPCs successfully reloaded.");
                }
            } else if (args.length >= 2) {
                if (args[0].equalsIgnoreCase("setsound")) {
                    if(plugin.soundWaiting.contains(player)) {
                        try{
                            Sound.valueOf(args[1]);
                        } catch (IllegalArgumentException ex) {
                            player.sendMessage(RED + "Unrecognised sound, please use tab completions.");
                            return true;
                        }
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            plugin.soundWaiting.remove(player);
                            List<String> argsCopy = plugin.editingActions.get(player).getArgsCopy();
                            Action action = plugin.editingActions.get(player);
                            List<String> currentArgs = action.getArgs();
                            currentArgs.clear();
                            currentArgs.add(0, argsCopy.get(0));
                            currentArgs.add(1, argsCopy.get(1));
                            currentArgs.add(2, args[1]);
                            player.sendMessage(ChatColor.GREEN + "Successfully set sound to be '" + ChatColor.RESET + args[1] + ChatColor.GREEN + "'");
                            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(plugin.menuCores.get(player).getActionCustomizerMenu(action)));
                        });
                    } else {
                        player.sendMessage(ChatColor.RED + "Unccessfully set NPC sound. I wasn't waiting for a response. Please contact Foxikle if you think this is a mistake.");
                    }
                } else {
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(args[1]);
                    } catch (IllegalArgumentException ignored) {
                        player.sendMessage(RED + "Invalid UUID provided.");
                        return false;
                    }
                    if (args[0].equalsIgnoreCase("delete")) {
                        if (!player.hasPermission("customnpcs.delete")) {
                            player.sendMessage(RED + "You lack the propper permissions to delete npcs.");
                            return true;
                        }
                        if (plugin.npcs.keySet().contains(uuid)) {
                            NPC npc = plugin.getNPCByID(uuid);
                            npc.remove();
                            npc.delete();
                            plugin.npcs.remove(npc.getUUID());
                            player.sendMessage(ChatColor.GREEN + "Successfully deleted the NPC: " + npc.getHologramName());

                        } else {
                            player.sendMessage(RED + "The UUID provided does not match any NPC.");
                        }
                    } else if (args[0].equalsIgnoreCase("edit")) {
                        if (!player.hasPermission("customnpcs.edit")) {
                            player.sendMessage(RED + "You lack the propper permissions to edit npcs.");
                            return true;
                        }
                        if (plugin.npcs.containsKey(uuid)) {
                            NPC npc = plugin.getNPCByID(uuid);
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                GameProfile profile = new GameProfile(uuid, npc.isClickable() ? "§e§lClick" : "noclick");
                                profile.getProperties().removeAll("textures");
                                profile.getProperties().put("textures", new Property("textures", null, null));
                                MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
                                ServerLevel nmsWorld = ((CraftWorld) player.getWorld()).getHandle();
                                List<String> actionStrs = new ArrayList<>();
                                npc.getActions().forEach(action -> actionStrs.add(action.serialize()));
                                NPC newNpc = new NPC(plugin, nmsServer, nmsWorld, profile, npc.getSpawnLoc(), npc.getHandItem(), npc.getItemInOffhand(), npc.getHeadItem(), npc.getChestItem(), npc.getLegsItem(), npc.getBootsItem(), npc.isClickable(), npc.isResilient(), npc.getHologramName(), uuid, npc.getValue(), npc.getSignature(), npc.getSkinName(), npc.getFacingDirection(), null, actionStrs);
                                MenuCore mc = new MenuCore(newNpc, plugin);
                                plugin.menuCores.put(player, mc);
                                plugin.pages.put(player, 0);
                                player.openInventory(mc.getMainMenu());
                            }, 1);
                        } else {
                            player.sendMessage(RED + "The UUID provided does not match any NPC.");
                        }
                    } else {
                        sender.sendMessage(RED + "Unrecognised sub-command. Use '/npc help' for a list of supported commands.");
                    }
                }
            }
        } else if(args[0].equalsIgnoreCase("reload")) {
            if(args.length >= 2) {
                if (args[1].equalsIgnoreCase("silent")) {
                    try {
                        Bukkit.getScoreboardManager().getMainScoreboard().getTeam("npc").unregister();
                    } catch (IllegalArgumentException ignored) {
                    }
                    HandlerList.unregisterAll(plugin);
                    List<NPC> npcs = new ArrayList<>(plugin.npcs.values());
                    for (NPC npc : npcs) {
                        plugin.npcs.remove(npc.getUUID());
                        npc.remove();
                    }
                    plugin.npcs.clear();
                    plugin.holograms.clear();
                    plugin.onEnable();
                }
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Reloading NPCs!");
                try {
                    Bukkit.getScoreboardManager().getMainScoreboard().getTeam("npc").unregister();
                } catch (IllegalArgumentException ignored) {}
                HandlerList.unregisterAll(plugin);
                List<NPC> npcs = new ArrayList<>(plugin.npcs.values());
                for (NPC npc : npcs) {
                    plugin.npcs.remove(npc.getUUID());
                    npc.remove();
                }
                plugin.npcs.clear();
                plugin.holograms.clear();
                plugin.onEnable();
                sender.sendMessage(ChatColor.GREEN + "NPCs successfully reloaded.");
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();
        if(args.length == 1){
            list.add("help");
            list.add("manage");
            list.add("create");
            list.add("delete");
            list.add("edit");
            list.add("reload");
            list.add("clear_holograms");
            if(plugin.soundWaiting.contains((Player) sender)) list.add("setsound");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("setsound")) {
                for (Sound sound : Sound.values()) {
                    list.add(sound.name());
                }
                return list;
            }
            plugin.npcs.keySet().forEach(uuid -> list.add(uuid.toString()));
        }
        return list;
    }
}
