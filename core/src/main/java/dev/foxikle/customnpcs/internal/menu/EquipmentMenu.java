/*
 * Copyright (c) 2024-2026. Foxikle
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
import dev.foxikle.customnpcs.internal.utils.ItemBuilder;
import dev.foxikle.customnpcs.internal.utils.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import studio.mevera.lotus.api.button.Button;
import studio.mevera.lotus.api.content.Content;
import studio.mevera.lotus.api.data.DataRegistry;
import studio.mevera.lotus.api.menu.Menu;
import studio.mevera.lotus.api.menu.MenuView;
import studio.mevera.lotus.api.slot.Capacity;
import studio.mevera.lotus.api.slot.Slot;

public class EquipmentMenu implements Menu<Component> {
    @Override
    public String name() {
        return MenuUtils.NPC_EQUIPMENT;
    }

    @Override
    public @NotNull Component title(MenuView<Component, ?> view) {
        return Msg.get(view.viewer(), "menus.equipment.title");
    }

    //TODO: Verify this was ported properly
//    @Override
//    public void onPostClick(MenuView<?> playerMenuView, InventoryClickEvent event) {
//        if (event.getClickedInventory() == null) return;
//        if (event.getClickedInventory() == event.getWhoClicked().getInventory()) {
//            event.setCancelled(false); // allow clicking in own inventory
//        }
//    }

    @Override
    public @NotNull Capacity capacity(MenuView<Component, ?> view) {
        return Capacity.ofRows(6);
    }

    @Override
    public @NotNull Content content(MenuView<Component, ?> view) {
        Player player = view.viewer();
        CustomNPCs plugin = CustomNPCs.getInstance();
        InternalNpc npc = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());
        if (npc == null) {
            return MenuUtils.invalidNpc(view);
        }

        return Content.builder(view.capacity())
                .fillAll(MenuItems.MENU_GLASS)
                .set(Slot.of(8), Button.clickable(MenuItems.importArmor(player), (menuView, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();
                    npc.getEquipment().importFromEntityEquipment(p.getEquipment());
                    p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);

                    menuView.content().set(Slot.of(13), MenuItems.helmetSlot(npc, player));
                    menuView.content().set(Slot.of(21), MenuItems.offhandSlot(npc, player));
                    menuView.content().set(Slot.of(22), MenuItems.chestplateSlot(npc, player));
                    menuView.content().set(Slot.of(23), MenuItems.handSlot(npc, player));
                    menuView.content().set(Slot.of(31), MenuItems.leggingsSlot(npc, player));
                    menuView.content().set(Slot.of(40), MenuItems.bootsSlot(npc, player));
                }))
                .set(Slot.of(13), MenuItems.helmetSlot(npc, player))
                .set(Slot.of(21), MenuItems.offhandSlot(npc, player))
                .set(Slot.of(22), MenuItems.chestplateSlot(npc, player))
                .set(Slot.of(23), MenuItems.handSlot(npc, player))
                .set(Slot.of(31), MenuItems.leggingsSlot(npc, player))
                .set(Slot.of(40), MenuItems.bootsSlot(npc, player))
                .set(Slot.of(49), MenuItems.toMain(player))
                .build();
    }
}
