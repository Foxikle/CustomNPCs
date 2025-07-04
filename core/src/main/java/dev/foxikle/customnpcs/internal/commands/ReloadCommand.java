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
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.annotations.Description;
import dev.velix.imperat.annotations.Permission;
import dev.velix.imperat.annotations.SubCommand;
import dev.velix.imperat.annotations.Usage;
import dev.velix.imperat.command.AttachmentMode;

import java.util.Locale;

@SubCommand(value = "reload", attachment = AttachmentMode.MAIN)
@Permission("customnpcs.commands.reload")
@Description("Reloads the CustomNPCs and its configuration.")
public class ReloadCommand {

    @Usage
    public void usage(BukkitSource source) {
        final CustomNPCs plugin = CustomNPCs.getInstance();
        plugin.setReloading(true);
        Locale locale = Locale.getDefault();
        if (!source.isConsole()) source.asPlayer().locale();
        source.reply(Msg.translate(locale, "customnpcs.commands.reload.start"));

        plugin.reloadConfig();
        plugin.onDisable();
        plugin.onEnable();
        plugin.setReloading(false);

        source.reply(Msg.translate(locale, "customnpcs.commands.reload.end"));
    }

}
