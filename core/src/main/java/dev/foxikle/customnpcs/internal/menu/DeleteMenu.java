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
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public class DeleteMenu implements Menu {
    @Override
    public String getName() {
        return MenuUtils.NPC_DELETE;
    }

    @Override
    public InventoryType getMenuType() {
        return InventoryType.CHEST;
    }

    @Override
    public @NotNull MenuTitle getTitle(DataRegistry dataRegistry, Player player) {
        return MenuTitles.createModern(Msg.translated("customnpcs.menus.delete.title"));
    }

    @Override
    public @NotNull Capacity getCapacity(DataRegistry dataRegistry, Player player) {
        return Capacity.ofRows(3);
    }

    @Override
    public @NotNull Content getContent(DataRegistry dataRegistry, Player player, Capacity capacity) {
        return Content.builder(capacity).apply(content -> content.fill(MenuItems.MENU_GLASS))
                .setButton(11, Button.clickable(
                        ItemBuilder.modern(Material.RED_STAINED_GLASS_PANE)
                                .setDisplay(Msg.translated("customnpcs.menus.delete.items.confirm.name"))
                                .setLore(Utils.mm(""), Msg.translated("customnpcs.menus.delete.items.confirm.lore"))
                                .build(), ButtonClickAction.plain((menuView, inventoryClickEvent) -> {
                            CustomNPCs plugin = CustomNPCs.getInstance();
                            Player player1 = (Player) inventoryClickEvent.getWhoClicked();
                            InternalNpc dontUse = plugin.getEditingNPCs().getIfPresent(player1.getUniqueId());

                            if (dontUse == null) {
                                player1.closeInventory();
                                player1.sendMessage(Msg.translated("customnpcs.error.npc-menu-expired"));
                                player1.playSound(player1, Sound.ENTITY_VILLAGER_NO, 1, 1);
                                return;
                            }

                            // we have to do this because the npcs stored in the cache are clones, and are not actually in the world.
                            InternalNpc npc = plugin.getNPCByID(dontUse.getUniqueID());

                            if (npc == null) {
                                player1.closeInventory();
                                player1.playSound(player1, Sound.ENTITY_VILLAGER_NO, 1, 1);
                                player1.sendMessage(Msg.translated("customnpcs.error.npc-menu-expired"));
                                return;
                            }

                            npc.remove();
                            npc.delete();
                            plugin.npcs.remove(npc.getUniqueID());
                            player1.sendMessage(Msg.translate("customnpcs.delete.success", npc.getSettings().getName()));
                            player1.closeInventory();
                            player1.playSound(player1, Sound.BLOCK_END_PORTAL_SPAWN, 1, 1);
                            npc.getCurrentLocation().getWorld().strikeLightningEffect(npc.getCurrentLocation());
                        }))).setButton(15, Button.clickable(
                        ItemBuilder.modern(Material.LIME_STAINED_GLASS_PANE)
                                .setDisplay(Msg.translated("customnpcs.items.go_back"))
                                .setLore(Msg.translated("customnpcs.menus.delete.items.to_safety"))
                                .build(), ButtonClickAction.plain((menuView, inventoryClickEvent) -> {
                            CustomNPCs plugin = CustomNPCs.getInstance();
                            InternalNpc npc = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());
                            Player player1 = (Player) inventoryClickEvent.getWhoClicked();
                            player1.playSound(player1, Sound.UI_BUTTON_CLICK, 1, 1);
                            if (npc == null) {
                                player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                                player.sendMessage(Msg.translated("customnpcs.error.npc-menu-expired"));
                                return;
                            }

                            Boolean openMenu = plugin.getDeltionReason().getIfPresent(player1.getUniqueId());

                            if (openMenu == null) {
                                player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                                player.sendMessage(Msg.translated("customnpcs.error.forgot_bailout_response"));
                                return;
                            }

                            if (openMenu) {
                                plugin.getLotus().openMenu(player1, MenuUtils.NPC_MAIN);
                                return;
                            }

                            player1.closeInventory();
                        }))).build();
    }
}