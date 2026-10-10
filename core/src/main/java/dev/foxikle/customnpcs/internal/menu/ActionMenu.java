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
import dev.foxikle.customnpcs.internal.utils.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import studio.mevera.lotus.api.button.Button;
import studio.mevera.lotus.api.content.Content;
import studio.mevera.lotus.api.menu.Menu;
import studio.mevera.lotus.api.menu.MenuView;
import studio.mevera.lotus.api.slot.Capacity;
import studio.mevera.lotus.api.slot.Slot;
import studio.mevera.lotus.paper.ItemBuilder;

public class ActionMenu implements Menu<Component> {
    @Override
    public @NonNull String name() {
        return MenuUtils.NPC_ACTIONS;
    }

    @Override
    public @NotNull Component title(@NotNull MenuView<Component, ?> view) {
        return Msg.get(view.viewer(), "menus.actions.title");
    }

    @Override
    public @NotNull Capacity capacity(@NotNull MenuView<Component, ?> view) {
        return Capacity.ofRows(6);
    }

    @Override
    public @NotNull Content content(@NotNull MenuView<Component, ?> view) {
        CustomNPCs plugin = CustomNPCs.getInstance();
        InternalNpc npc = plugin.getEditingNPCs().getIfPresent(view.viewer().getUniqueId());
        if (npc == null) {
            return MenuUtils.invalidNpc(view);
        }

        return Content.builder(view.capacity())
                .fillBorder(MenuItems.MENU_GLASS)
                .apply(content -> MenuItems.currentActions(npc, view.viewer()).forEach(content::add))
                .set(Slot.of(45), MenuItems.toMain(view.viewer()))
                .build();
    }
}
