/*
 * Copyright (c) 2024-2025. Foxikle
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
import dev.foxikle.customnpcs.internal.utils.Utils;
import io.github.mqzen.menus.base.Content;
import io.github.mqzen.menus.base.Menu;
import io.github.mqzen.menus.misc.Capacity;
import io.github.mqzen.menus.misc.DataRegistry;
import io.github.mqzen.menus.misc.button.Button;
import io.github.mqzen.menus.misc.button.actions.ButtonClickAction;
import io.github.mqzen.menus.misc.itembuilder.ItemBuilder;
import io.github.mqzen.menus.titles.MenuTitle;
import io.github.mqzen.menus.titles.MenuTitles;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DeleteLineMenu implements Menu {
    @Override
    public String getName() {
        return MenuUtils.NPC_DELETE_LINE;
    }

    @Override
    public @NotNull MenuTitle getTitle(DataRegistry dataRegistry, Player player) {
        return MenuTitles.createModern(Msg.translate(player.locale(), "customnpcs.menus.hologram.delete.title"));
    }

    @Override
    public @NotNull Capacity getCapacity(DataRegistry dataRegistry, Player player) {
        return Capacity.ofRows(3);
    }

    @Override
    public @NotNull Content getContent(DataRegistry dataRegistry, Player player, Capacity capacity) {
        CustomNPCs plugin = CustomNPCs.getInstance();
        InternalNpc npcFor = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());
        int index = HologramMenu.editingIndicies.getOrDefault(player.getUniqueId(), -1);

        if (npcFor == null || index < 0 || npcFor.getSettings().getHolograms().length <= index) {
            player.sendMessage(Msg.translate(player.locale(), "customnpcs.error.npc-menu-expired"));
            player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1, 1);
            return Content.empty(capacity);
        }

        return Content.builder(capacity).apply(content -> content.fill(MenuItems.MENU_GLASS))
                .setButton(11, Button.clickable(
                        ItemBuilder.modern(Material.RED_STAINED_GLASS_PANE)
                                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.hologram.delete.confirm"))
                                .setLore(Msg.lore(player.locale(), "customnpcs.menus.hologram.delete.confirm.lore", npcFor.getSettings().getHolograms()[index]))
                                .build(), ButtonClickAction.plain((menuView, inventoryClickEvent) -> {

                            Player p = (Player) inventoryClickEvent.getWhoClicked();
                            InternalNpc npc = plugin.getEditingNPCs().getIfPresent(p.getUniqueId());

                            if (npc == null) {
                                p.closeInventory();
                                p.sendMessage(Msg.translate(player.locale(), "customnpcs.error.npc-menu-expired"));
                                p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1, 1);
                                return;
                            }

                            String[] raw = npc.getSettings().getRawHolograms();
                            List<String> mutable = Utils.list(raw);
                            if (index >= raw.length) {
                                p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1, 1);
                                p.sendMessage(Msg.translate(player.locale(), "customnpcs.error.npc-menu-expired"));
                                return;
                            }
                            mutable.remove(index);
                            player.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1F, 1F);
                            npc.getSettings().setRawHolograms(mutable.toArray(new String[0]));
                            plugin.getLotus().openMenu(p, MenuUtils.NPC_HOLOGRAMS);
                        }))).setButton(15, Button.clickable(
                        ItemBuilder.modern(Material.LIME_STAINED_GLASS_PANE)
                                .setDisplay(Msg.translate(player.locale(), "customnpcs.items.go_back"))
                                .setLore(Msg.translate(player.locale(), "customnpcs.menus.hologram.delete.to_safety"))
                                .build(), ButtonClickAction.plain((menuView, inventoryClickEvent) -> {
                            InternalNpc npc = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());
                            Player p = (Player) inventoryClickEvent.getWhoClicked();
                            p.playSound(p, Sound.UI_BUTTON_CLICK, 1, 1);

                            if (npc == null) {
                                player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                                player.sendMessage(Msg.translate(player.locale(), "customnpcs.error.npc-menu-expired"));
                                return;
                            }

                            plugin.getLotus().openMenu(p, MenuUtils.NPC_HOLOGRAMS);
                        }))).build();
    }
}
