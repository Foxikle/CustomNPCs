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

package dev.foxikle.customnpcs.internal.menu;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.utils.Msg;
import io.github.mqzen.menus.base.Content;
import io.github.mqzen.menus.base.Menu;
import io.github.mqzen.menus.misc.Capacity;
import io.github.mqzen.menus.misc.DataRegistry;
import io.github.mqzen.menus.misc.button.Button;
import io.github.mqzen.menus.misc.button.actions.impl.CloseMenuAction;
import io.github.mqzen.menus.misc.itembuilder.ItemBuilder;
import io.github.mqzen.menus.titles.MenuTitle;
import io.github.mqzen.menus.titles.MenuTitles;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ExtraSettingsMenu implements Menu {
    @Override
    public String getName() {
        return MenuUtils.NPC_EXTRA_SETTINGS;
    }

    @Override
    public @NotNull MenuTitle getTitle(DataRegistry dataRegistry, Player player) {
        return MenuTitles.createModern(Msg.translate(player.locale(), "customnpcs.menus.extra.title"));
    }

    @Override
    public @NotNull Capacity getCapacity(DataRegistry dataRegistry, Player player) {
        return Capacity.ofRows(3);
    }

    @Override
    public @NotNull Content getContent(DataRegistry dataRegistry, Player player, Capacity capacity) {
        CustomNPCs plugin = CustomNPCs.getInstance();
        InternalNpc npc = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());
        if (npc == null) {
            return Content.builder(capacity)
                    .setButton(22, Button.clickable(
                            ItemBuilder.modern(Material.RED_STAINED_GLASS_PANE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.main.error.no_npc"))
                                    .setLore(Msg.lore(player.locale(), "customnpcs.menus.main.error.no_npc.lore"))
                                    .build(),
                            new CloseMenuAction()
                    ))
                    .build();
        }

        return Content.builder(capacity)
                .apply(content -> content.fill(MenuItems.MENU_GLASS))
                .setButton(11, MenuItems.interactableHologram(npc, player))
                .setButton(13, MenuItems.interactableText(player))
                .setButton(15, MenuItems.upsideDown(npc, player))
                .setButton(18, MenuItems.toMain(player))
                .build();
    }
}
