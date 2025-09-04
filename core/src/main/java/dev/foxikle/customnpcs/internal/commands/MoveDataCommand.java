/*
 * Copyright (c) 2025. Foxikle
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
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.proto.NpcOuterClass;
import dev.foxikle.customnpcs.internal.proto.ProtoWrapper;
import dev.foxikle.customnpcs.internal.storage.FileStorage;
import dev.foxikle.customnpcs.internal.storage.StorageManager;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.annotations.*;
import dev.velix.imperat.command.AttachmentMode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SubCommand(value = "movedata", attachment = AttachmentMode.MAIN)
@Permission("customnpcs.commands.movedata")
public class MoveDataCommand {

    private static final Map<UUID, Boolean> run_already = new HashMap<>();
    private static boolean console_confirmed = false;

    @Usage
    public void usage(BukkitSource source, @Named("operation") @Suggest({"MERGE_REMOTE", "MERGE_LOCAL", "OVERWRITE"}) @Default("MERGE_LOCAL") String operation) {
        if (!checkValidOperation(operation)) {
            source.reply(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata.invalid_operation"));
            return;
        }
        if (source.isConsole()) {
            if (!console_confirmed) {
                console_confirmed = true;
                source.reply(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata.need_confirm"));
                return;
            } else {
                console_confirmed = false;
                if (operation.equals("MERGE_LOCAL")) {
                    merge(source, true);
                } else if (operation.equals("MERGE_REMOTE")) {
                    merge(source, false);
                } else {
                    overwrite(source);
                }
            }
        }

        Player player = source.asPlayer();
        if (!run_already.containsKey(player.getUniqueId())) {
            run_already.put(player.getUniqueId(), true);
            source.reply(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata.need_confirm"));
            return;
        }

        run_already.remove(player.getUniqueId());

        if (operation.equals("MERGE_LOCAL")) {
            merge(source, true);
        } else if (operation.equals("MERGE_REMOTE")) {
            merge(source, false);
        } else {
            overwrite(source);
        }

    }

    private boolean checkValidOperation(String operation) {
        return operation.equalsIgnoreCase("MERGE_LOCAL") || operation.equalsIgnoreCase("MERGE_REMOTE") || operation.equalsIgnoreCase("OVERWRITE");
    }

    private boolean start(BukkitSource source) {
        source.reply(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata.operation_queued"));

        if ((CustomNPCs.getInstance().getStorageManager().getStorage() instanceof FileStorage)) {
            CustomNPCs.getInstance().getLogger().log(Level.WARNING, "The current file provider is already File storage!");
            source.reply(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata.operation_failure"));
            return true;
        }

        if (!FileStorage.FILE.exists()) {
            CustomNPCs.getInstance().getLogger().log(Level.SEVERE, "The local NPC data file does not exist!", new IllegalStateException());
            source.reply(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata.operation_failure"));
            return true;
        }
        return false;
    }

    private void merge(BukkitSource source, boolean discardRemote) {
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
                source.reply(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata.operation_failure"));
                return;
            }

            List<NpcOuterClass.Npc> local = ProtoWrapper.deserializeProtoList(currentData);
            sm.getAllNpcs().whenComplete((remote, throwable) -> {
                if (throwable != null) {
                    plugin.getLogger().log(Level.SEVERE, "An error occurred while fetching remote NPC data!", throwable);
                    source.reply(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata.operation_failure"));
                    return;
                }

                List<NpcOuterClass.Npc> merged = Stream.concat(remote.stream(), local.stream())
                        .collect(Collectors.toMap(
                                NpcOuterClass.Npc::getUuid, npc -> npc, (t, t2) -> discardRemote ? t : t2)
                        ).values().stream().toList();
                finalizeMove(source, plugin, sm, merged);
            });
        });
    }

    /**
     * Overwrites the currently stored data with whatever is in the local file
     *
     * @param source the source to reply to
     */
    private void overwrite(BukkitSource source) {
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
                source.reply(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata.operation_failure"));
                return;
            }

            List<NpcOuterClass.Npc> toWrite = ProtoWrapper.deserializeProtoList(currentData);
            finalizeMove(source, plugin, sm, toWrite);
        });
    }

    private void finalizeMove(BukkitSource source, CustomNPCs plugin, StorageManager sm, List<NpcOuterClass.Npc> toWrite) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            sm.resetTracked();

            // remove the npcs
            plugin.getNPCs().forEach(InternalNpc::remove);
            plugin.npcs.clear();

            for (NpcOuterClass.Npc npc : toWrite) {
                sm.track(npc);
                sm.loadProto(npc);
            }

            sm.saveNpcs().whenComplete((aBoolean, throwable1) -> {
                if (throwable1 != null) {
                    plugin.getLogger().log(Level.SEVERE, "An error occurred while writing remote NPC data!", throwable1);
                    source.reply(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata.operation_failure"));
                    return;
                }
                source.reply(Msg.translate(CommandUtils.getLocale(source), "customnpcs.commands.movedata.operation_success"));
            });
        });
    }
}
