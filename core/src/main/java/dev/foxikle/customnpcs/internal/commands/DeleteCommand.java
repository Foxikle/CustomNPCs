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
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.menu.MenuUtils;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.annotations.*;
import dev.velix.imperat.command.AttachmentMode;
import org.bukkit.entity.Player;

import java.util.UUID;

@SubCommand(value = "delete", attachment = AttachmentMode.MAIN)
@Permission("customnpcs.delete")
@Description("Deletes the specified NPC")
public class DeleteCommand {
    @Usage
    public void usage(
            BukkitSource source,
            @Named("npc") @SuggestionProvider("current_npc") @Greedy String npc
    ) {
        if (source.isConsole()) {
            source.reply(Msg.format("You can't do this :P"));
            return;
        }

        UUID uuid = CommandUtils.parseNpc(source, npc);
        if (uuid == null) {
            return;
        }
        if (!CommandUtils.checkNpc(source, uuid)) return;

        final Player p = source.asPlayer();
        final CustomNPCs plugin = CustomNPCs.getInstance();
        final InternalNpc finalNpc = plugin.getNPCByID(uuid);

        assert finalNpc != null;
        plugin.getEditingNPCs().put(p.getUniqueId(), finalNpc);
        plugin.getDeltionReason().put(p.getUniqueId(), false);
        plugin.getLotus().openMenu(p, MenuUtils.NPC_DELETE);
    }
}
