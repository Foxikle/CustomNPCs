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

import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.utils.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import studio.mevera.lotus.api.button.Button;
import studio.mevera.lotus.api.content.Content;
import studio.mevera.lotus.api.menu.Menu;
import studio.mevera.lotus.api.menu.MenuView;
import studio.mevera.lotus.api.slot.Capacity;
import studio.mevera.lotus.api.slot.Slot;

public class EquipmentMenu implements Menu<Component> {
    @Override
    public @NonNull String name() {
        return MenuUtils.NPC_EQUIPMENT;
    }

    @Override
    public @NotNull Component title(MenuView<Component, ?> view) {
        return Msg.get(view.viewer(), "menus.equipment.title");
    }

    @Override
    public @NotNull Capacity capacity(@NonNull MenuView<Component, ?> view) {
        return Capacity.ofRows(6);
    }

    @Override
    public @NotNull Content content(MenuView<Component, ?> view) {
        InternalNpc npc = view.data().require(InternalNpc.KEY);

        return Content.builder(view.capacity())
                .fillAll(MenuItems.MENU_GLASS)
            .set(Slot.of(8), Button.clickable(MenuItems.importArmor(view), (menuView, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();
                    npc.getEquipment().importFromEntityEquipment(p.getEquipment());
                    p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);

                menuView.content().set(Slot.of(13), MenuItems.helmetSlot(view));
                menuView.content().set(Slot.of(21), MenuItems.offhandSlot(view));
                menuView.content().set(Slot.of(22), MenuItems.chestplateSlot(view));
                menuView.content().set(Slot.of(23), MenuItems.handSlot(view));
                menuView.content().set(Slot.of(31), MenuItems.leggingsSlot(view));
                menuView.content().set(Slot.of(40), MenuItems.bootsSlot(view));
                }))
            .set(Slot.of(13), MenuItems.helmetSlot(view))
            .set(Slot.of(21), MenuItems.offhandSlot(view))
            .set(Slot.of(22), MenuItems.chestplateSlot(view))
            .set(Slot.of(23), MenuItems.handSlot(view))
            .set(Slot.of(31), MenuItems.leggingsSlot(view))
            .set(Slot.of(40), MenuItems.bootsSlot(view))
            .set(Slot.of(49), MenuItems.toMain(view))
                .build();
    }
}
