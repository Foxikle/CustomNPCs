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

package dev.foxikle.customnpcs.internal.commands;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.FileManager;
import dev.foxikle.customnpcs.internal.commands.enums.FixConfigWorldStrategy;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.annotations.*;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@SubCommand(value = "fixconfig", attachDirectly = true)
@Permission()
public class FixConfigCommand {

    @Usage
    public void execute(BukkitSource source) {
        Locale locale = CommandUtils.getLocale(source);
        source.reply(Msg.translate(locale, "customnpcs.commands.fix_config.usage"));
    }

    @Usage
    public void execute(BukkitSource source,
                        @Named("mode") @Suggest({"world"}) String mode,
                        @SuggestionProvider("worlds") @Default("world") @Named("world") String world,
                        @Suggest({"NONE", "SAFE_LOCATION"}) @Default("NONE") @Named("strategy") String strategy,
                        @Suggest({"all"}) @SuggestionProvider("broken_npc") @Default("all") @Named("target") String target
    ) {
        Locale locale = CommandUtils.getLocale(source);
        int totalFixed = 0;
        int movedbyStrategy = 0;
        int failedToFix = 0;
        int nonExistentNpcs = 0;

        if (!mode.equalsIgnoreCase("world")) {
            execute(source);
            return;
        }

        if (Bukkit.getWorld(world) == null) {
            // invalid world
            source.reply("INVALID_WORLD");
            return;
        }

        World w = Bukkit.getWorld(world);
        assert w != null;

        FixConfigWorldStrategy strat = FixConfigWorldStrategy.parse(strategy);
        if (strat == null) {
            //invalid strategy
            source.reply("INVALID_STRATEGY");
            return;
        }

        CustomNPCs plugin = CustomNPCs.getInstance();
        FileManager fileManager = plugin.getFileManager();


        if (target.equalsIgnoreCase("all")) {
            // apply it to all NPCs

            for (UUID uuid : fileManager.getBrokenNPCs().keySet()) {
                YamlConfiguration yml = fileManager.getNpcYaml();
                ConfigurationSection parent = yml.getConfigurationSection(uuid.toString());
                if (parent == null) {
                    nonExistentNpcs++;
                    continue;
                }

                ConfigurationSection location = parent.getConfigurationSection("location");

                Location loc;
                String locString;

                //Bukkit's terrible config api didn't wipe the section
                if (location != null) {
                    assert location != null : "Location is null";

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
                    plugin.getLogger().warning("Fixed an NPC whose location data was wiped by Bukkit's configuration API. Its location was set to (0,0,0)");
                    source.reply(Msg.translate(locale, "customnpcs.commands.fix_config.bukkit_wiped_data"));
                }


                if (strat == FixConfigWorldStrategy.SAFE_LOCATION) {

                    // in a wall
                    if (w.getBlockAt(loc).isSolid() || w.getBlockAt(loc.add(0, 1, 0)).isSolid()) {
                        RayTraceResult traceResult = w.rayTraceBlocks(loc.add(0, 329 - loc.y(), 0),
                                new Vector(0, -1, 0), 320D, FluidCollisionMode.NEVER);

                        if (traceResult == null) {
                            // The location cannot be safe

                            plugin.getLogger().warning("Failed to fix npc " + uuid + " at " + locString + " -- Location cannot be made safe.");
                            failedToFix++;
                            continue;
                        }
                        loc.setY(traceResult.getHitBlock().getY() + 1);
                    }
                    movedbyStrategy++;

                }

                parent.set("location", loc);
                totalFixed++;
                fileManager.saveNpcFile(yml);
            }
        } else {
            // find npc by flag
            try {


                if (!fileManager.getBrokenNPCs().containsValue(target)) {
                    // it dont exist
                    source.reply(Msg.translate(locale, "customnpcs.commands.invalid_name_or_uuid "));
                    return;
                }
                UUID uuid = null;
                for (Map.Entry<UUID, String> entry : fileManager.getBrokenNPCs().entrySet()) {
                    if (entry.getValue().equals(target)) {
                        uuid = entry.getKey();
                        break;
                    }
                }

                if (uuid == null) {
                    source.reply(Msg.translate(locale, "customnpcs.commands.invalid_name_or_uuid"));
                    return;
                }

                YamlConfiguration yml = fileManager.getNpcYaml();
                ConfigurationSection parent = yml.getConfigurationSection(uuid.toString());
                if (parent == null) {
                    nonExistentNpcs++;
                    // hacky hacky way to do this
                    throw new RuntimeException("Catch me!");
                }
                ConfigurationSection location = parent.getConfigurationSection("location");


                Location loc;
                String locString;

                //Bukkit's terrible config api didn't wipe the section
                if (location != null) {
                    assert location != null : "Location is null";

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
                    plugin.getLogger().warning("Fixed an NPC whose location data was wiped by Bukkit's configuration API. Its location was set to (0,0,0)");
                    source.reply(Msg.translate(locale, "customnpcs.commands.fix_config.bukkit_wiped_data"));
                }


                if (strat == FixConfigWorldStrategy.SAFE_LOCATION) {

                    // in a wall
                    if (w.getBlockAt(loc).isSolid() || w.getBlockAt(loc.add(0, 1, 0)).isSolid()) {
                        RayTraceResult traceResult = w.rayTraceBlocks(loc.add(0, 329 - loc.y(), 0),
                                new Vector(0, -1, 0), 320D, FluidCollisionMode.NEVER);

                        if (traceResult == null) {
                            // The location cannot be safe
                            plugin.getLogger().warning("Failed to fix npc " + uuid + " at " + locString + " -- Location cannot be made safe.");
                            failedToFix++;
                            throw new RuntimeException("Catch me!");
                        }
                        loc.setY(traceResult.getHitBlock().getY() + 1);
                    }
                    movedbyStrategy++;

                }

                parent.set("location", loc);
                totalFixed++;

                fileManager.saveNpcFile(yml);
            } catch (Exception ignored) {
            }

        }
        source.reply(Msg.translate(locale, "customnpcs.commands.fix_config.report", totalFixed, movedbyStrategy, failedToFix, nonExistentNpcs));
    }
}
