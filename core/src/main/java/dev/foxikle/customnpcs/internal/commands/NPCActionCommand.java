package dev.foxikle.customnpcs.internal.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.Utils;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
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
                            String title = String.join(" ", args);
                            if(plugin.papi) {
                                title = PlaceholderAPI.setPlaceholders(player, title);
                            }
                            player.showTitle(Title.title(plugin.getMiniMessage().deserialize(title), Component.empty(), Title.Times.times(Duration.ofMillis(in * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(out * 50L))));
                        }
                    }
                    case "SEND_MESSAGE" -> {
                        if (!args.isEmpty()) {
                            if(plugin.papi) {
                                player.sendMessage(plugin.getMiniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, String.join(" ", args))));
                            } else {
                                player.sendMessage(plugin.getMiniMessage().deserialize(String.join(" ", args)));
                            }
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
                    case "RUN_COMMAND" -> player.performCommand(String.join(" ", args));
                    case "ACTION_BAR" -> {
                        if(plugin.papi) {
                            player.sendActionBar(plugin.getMiniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, String.join(" ", args))));
                        } else {
                            player.sendActionBar(plugin.getMiniMessage().deserialize(String.join(" ", args)));
                        }
                    }
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
                            player.teleportAsync(new Location(player.getWorld(), x, y, z, yaw, pitch));
                        }
                    }
                    case "SEND_TO_SERVER" -> {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("ConnectOther");
                        out.writeUTF(player.getName());
                        out.writeUTF(args.get(0));
                        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                    }
                    case "ADD_EFFECT" -> { // duration, strength, hide particles, effect
                        if(args.size() >= 4) {
                            int duration = Integer.parseInt(args.get(0));
                            args.remove(0);
                            int strenth = Integer.parseInt(args.get(0));
                            args.remove(0);
                            boolean hideParticles = Boolean.parseBoolean(args.get(0));
                            args.remove(0);
                            PotionEffectType type = PotionEffectType.getByName(args.get(0));
                            player.addPotionEffect(new PotionEffect(type, duration, strenth, true, !hideParticles));
                        }
                    }
                    case "REMOVE_EFFECT" -> { // effect
                        if(!args.isEmpty()) {
                            PotionEffectType type = PotionEffectType.getByName(args.get(0));
                            player.removePotionEffect(type);
                        }
                    }
                    case "GIVE_EXP" -> { // amount, levels
                        if(args.size() >= 2) {
                            int amount = Integer.parseInt(args.get(0));
                            args.remove(0);
                            boolean isLevels = Boolean.parseBoolean(args.get(0));

                            if(isLevels) {
                                player.giveExpLevels(amount);
                            } else {
                                player.giveExp(amount, true);
                            }
                        }
                    }
                    case "REMOVE_EXP" -> { // amount, levels
                        if(args.size() >= 2) {
                            int amount = Integer.parseInt(args.get(0));
                            args.remove(0);
                            boolean isLevels = Boolean.parseBoolean(args.get(0));

                            if(isLevels) {
                                if(amount >= player.getLevel()) {
                                    player.setLevel(0);
                                } else {
                                    player.setLevel(player.getLevel() - amount);
                                }
                            } else {
                                Utils.setTotalExperience(player, Utils.getTotalExperience(player) - amount);
                            }
                        }
                    }
                    case "TOGGLE_FOLLOWING" -> { //UUID of NPC
                        if(!args.isEmpty()) {
                            if (plugin.npcs.containsKey(UUID.fromString(args.get(0)))) {
                                UUID npcId = UUID.fromString(args.get(0));
                                InternalNpc npc = plugin.getNPCByID(npcId);
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
            sender.sendMessage(Component.text("You cannot do this!", NamedTextColor.RED));
        }
        return false;
    }
}
