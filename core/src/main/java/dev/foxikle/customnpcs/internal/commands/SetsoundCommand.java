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

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.actions.defaultImpl.PlaySound;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.WaitingType;
import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.annotations.*;
import dev.velix.imperat.command.AttachmentMode;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;

@SubCommand(value = "setsound", attachment = AttachmentMode.MAIN)
@Permission("customnpcs.edit")
@Description("Sets the sound of the playsound action!")
public class SetsoundCommand {

    @Usage
    public void usage(
            BukkitSource source,
            @Default("minecraft:ui.button.click") @Named("sound") @SuggestionProvider("sound") @Greedy String soundRaw) {

        if (source.isConsole()) {
            source.reply("You can't do this :P");
            return;
        }

        final Player p = source.asPlayer();
        final CustomNPCs plugin = CustomNPCs.getInstance();
        // converts `ui button click` to `UI_BUTTON_CLICK`, like {@link Sound#UI_BUTTON_CLICK}
        String formatted = soundRaw.trim().toLowerCase();

        if (plugin.isWaiting(p, WaitingType.SOUND)) {
            if (Registry.SOUNDS.get(NamespacedKey.fromString(formatted)) == null) {
                p.sendMessage(Msg.translate(p.locale(), "customnpcs.commands.setsound.unknown_sound"));
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.waiting.remove(p.getUniqueId());
                Action actionImpl = plugin.editingActions.get(p.getUniqueId());
                if (actionImpl instanceof PlaySound action) {
                    action.setSound(formatted);
                } else
                    throw new IllegalArgumentException("Action " + actionImpl.getClass().getName() + " is not of type PlaySound");
                p.sendMessage(Msg.translate(p.locale(), "customnpcs.commands.setsound.success", Component.text(formatted)));
                plugin.getLotus().openMenu(p, actionImpl.getMenu());
            });
        } else {
            p.sendMessage(Msg.translate(p.locale(), "customnpcs.commands.setsound.was_not_waiting"));
        }

    }

}
