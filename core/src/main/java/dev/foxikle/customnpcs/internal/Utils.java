package dev.foxikle.customnpcs.internal;

import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class Utils {
    @SafeVarargs
    public static <E> List<E> list(E... vararg) {
        return new ArrayList<>(List.of(vararg));
    }

    public static String style(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    private static int getTotalExperience(int level) {
        int xp = 0;

        if (level >= 0 && level <= 15) {
            xp = (int) Math.round(Math.pow(level, 2) + 6 * level);
        } else if (level > 15 && level <= 30) {
            xp = (int) Math.round((2.5 * Math.pow(level, 2) - 40.5 * level + 360));
        } else if (level > 30) {
            xp = (int) Math.round(((4.5 * Math.pow(level, 2) - 162.5 * level + 2220)));
        }
        return xp;
    }

    public static int getTotalExperience(Player player) {
        return Math.round(player.getExp() * player.getExpToLevel()) + getTotalExperience(player.getLevel());
    }

    public static void setTotalExperience(Player player, int amount) {
        int level = 0;
        int xp = 0;
        float a = 0;
        float b = 0;
        float c = -amount;

        if (amount > getTotalExperience(0) && amount <= getTotalExperience(15)) {
            a = 1;
            b = 6;
        } else if (amount > getTotalExperience(15) && amount <= getTotalExperience(30)) {
            a = 2.5f;
            b = -40.5f;
            c += 360;
        } else if (amount > getTotalExperience(30)) {
            a = 4.5f;
            b = -162.5f;
            c += 2220;
        }
        level = (int) Math.floor((-b + Math.sqrt(Math.pow(b, 2) - (4 * a * c))) / (2 * a));
        xp = amount - getTotalExperience(level);
        player.setLevel(level);
        player.setExp(0);
        player.giveExp(xp);
    }

    /**
     * Calculates the Location for the NPCs to look at.
     * @param npc the NPC
     * @return the location to look at
     */
    public static Location calcLocation(InternalNpc npc) {
        Location loc = npc.getCurrentLocation();
        double yaw = npc.getSettings().getDirection();
        double heading = -Math.toRadians(yaw);
        // trig to calculate the position
        loc.add(5 * Math.sin(heading),
                1.6 + -5 * Math.sin(Math.toRadians(npc.getSpawnLoc().getPitch())),
                5 * Math.cos(heading)
        );

        return loc;
    }
}
