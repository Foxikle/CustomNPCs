package dev.foxikle.customnpcs.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.foxikle.customnpcs.CustomNPCs;
import dev.foxikle.customnpcs.NPC;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The handler for the Action command
 */
public class NPCActionCommand implements CommandExecutor {
    /**
     * The instance of the main class
     */
    private final CustomNPCs plugin;

    /**
     * The constructor for the action command handler
     * @param plugin main class instance
     */
    public NPCActionCommand(CustomNPCs plugin) {
        this.plugin = plugin;
    }

    /*
    *  ------- EXAMPLE NPC ACTION COMMAND -------
    *   (Always executed as CONSOLE)
    *  /npcaction <Player> <Action> <Args for action>
    *
    *
    * Actions include:
    *   Display Title
    *   Send Message
    *   Play sound
    *   Run external command
    *   Give effect
    *   Teleport player
    *   Send to Bungeecord server
    *   Start following
    *   Stop following
    *   Action bar
    */
    /**
     * <p>The generic handler for any command
     * </p>
     * @param command The command used
     * @param sender The sender of the command
     * @param label The label of the command (/label args[])
     * @param arguments The arguments of the commands
     * @return if the command was handled
     * @since 1.3-pre5
     */
    @Override
    @SuppressWarnings("deprecated")
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        if(sender instanceof ConsoleCommandSender){
            if(arguments.length > 0){
                String subCommand = arguments[1];
                Player player = Bukkit.getPlayer(UUID.fromString(arguments[0]));
                int delay = Integer.parseInt(arguments[2]);
                assert player != null;
                List<String> args = new ArrayList<>(List.of(arguments));
                args.remove(0); // remove player
                args.remove(0); // remove sub command
                args.remove(0); // remove delay
                Bukkit.getScheduler().runTaskLater(plugin, () ->{


                switch (subCommand) {
                    case "DISPLAY_TITLE" -> {
                        if (args.size() >= 4) { // fade in, time, fade out, title, subtitle  //TODO: somehow differentiate title from subtitle
                            int in = Integer.parseInt(args.get(0));
                            args.remove(0);
                            int stay = Integer.parseInt(args.get(0));
                            args.remove(0);
                            int out = Integer.parseInt(args.get(0));
                            args.remove(0);
                            String title = ChatColor.translateAlternateColorCodes('&', String.join(" ", args));
                            player.sendTitle(title, "", in, stay, out);
                        }
                    }
                    case "SEND_MESSAGE" -> {
                        if (args.size() >= 1) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join(" ", args)));
                        }
                    }
                    case "PLAY_SOUND" -> {
                        if (args.size() >= 3) { // pitch, volume, sound
                            float pitch = Float.parseFloat(args.get(0));
                            args.remove(0);
                            float volume = Float.parseFloat(args.get(0));
                            args.remove(0);
                            Sound sound = Sound.valueOf(Sound.class, args.get(0));
                            player.playSound(player.getLocation(), sound, volume, pitch);
                        }
                    }
                    case "RUN_COMMAND" -> player.performCommand(args.toString());
                    case "ACTION_BAR" -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', String.join(" ", args))).create());
                    case "TELEPORT" -> {
                        if(args.size() >= 5) {
                            double x = Double.parseDouble(args.get(0));
                            args.remove(0);
                            double y = Double.parseDouble(args.get(0));
                            args.remove(0);
                            double z = Double.parseDouble(args.get(0));
                            args.remove(0);
                            float pitch = Float.parseFloat(args.get(0));
                            args.remove(0);
                            float yaw = Float.parseFloat(args.get(0));
                            args.remove(0);
                            player.teleport(new Location(player.getWorld(), x, y, z, yaw, pitch));
                        }
                    }
                    case "SEND_TO_SERVER" -> {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("ConnectOther");
                        out.writeUTF(player.getName());
                        out.writeUTF(args.get(0));
                        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                    }
                    case "TOGGLE_FOLLOWING" -> { //UUID of NPC
                        if(args.size() >= 1) {
                            if (plugin.npcs.containsKey(UUID.fromString(args.get(0)))) {
                                UUID npcId = UUID.fromString(args.get(0));
                                NPC npc = plugin.getNPCByID(npcId);
                                if(npc.getTarget() == player){
                                    npc.setTarget(null);
                                } else {
                                    npc.setTarget(player);
                                }
                            }
                        }
                    }
                }
                }, delay);
            }
        } else {
            sender.sendPlainMessage(ChatColor.RED + "You cannot do this!");
        }
        return false;
    }
}
