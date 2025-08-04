/*
 * Copyright (c) 2024. Foxikle
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

import com.google.common.reflect.TypeToken;
import dev.foxikle.customnpcs.actions.conditions.Condition;
import dev.foxikle.customnpcs.data.Settings;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A class holding usful methods
 */
public class Utils {

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

    public static final Type CONDITIONS_LIST = new TypeToken<List<Condition>>() {
    }.getType();

    /**
     * Creates a MUTABLE list
     *
     * @param vararg The vararg of emelments to be added to a mutable list
     * @return The mutable list containing the passed elements.
     */
    @SafeVarargs
    public static <E> List<E> list(E... vararg) {
        return new ArrayList<>(List.of(vararg));
    }

    @SuppressWarnings("deprecation") // stop complaining about ChatColor
    public static String style(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    /**
     * Gets the total number of xp points at a level
     *
     * @param level the level to get the number of points
     * @return the number of points at a specific level
     */
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

    /**
     * Gets the total experience of a player
     *
     * @param player the player
     * @return the number of experience points the player has
     */
    public static int getTotalExperience(Player player) {
        return Math.round(player.getExp() * player.getExpToLevel()) + getTotalExperience(player.getLevel());
    }

    @ApiStatus.Internal
    public static String getNpcName(Settings settings, UUID npcUUID) {
        return settings.isUpsideDown() ? "Dinnerbone" : npcUUID.toString().substring(0, 16);
    }

    /**
     * Sets the player's total experience, including levels
     *
     * @param player the player
     * @param amount the amount of xp
     */
    public static void setTotalExperience(Player player, int amount) {
        int level;
        int xp;
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
     *
     * @param npc the NPC
     * @return the location to look at
     */
    public static Location calcLocation(InternalNpc npc) {
        Location loc = npc.getCurrentLocation();
        double pitch = Math.toRadians(npc.getPitch());
        double yaw = Math.toRadians(npc.getYaw());
        // trig to calculate the position


        double xz = Math.cos(pitch);
        double x = -xz * Math.sin(yaw);
        double y = -Math.sin(pitch) + 1.62;
        double z = xz * Math.cos(yaw);
        loc.add(x, y, z);

        return loc;
    }

    /**
     * Formats the given string as a Minimessage component
     *
     * @param str The input
     * @return the component, parsed in MiniMessage format.
     * @deprecated Use {@link Msg#format(String)} instead.
     */
    @Deprecated
    public static Component mm(String str) {
        return Msg.format(str);
    }

}
