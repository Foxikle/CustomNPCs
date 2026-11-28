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

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.internal.translations.Arg;
import dev.foxikle.customnpcs.internal.utils.ItemBuilder;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.await.Await;
import dev.foxikle.customnpcs.internal.utils.await.AwaitAction;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNullByDefault;
import studio.mevera.lotus.api.button.Button;
import studio.mevera.lotus.api.content.Content;
import studio.mevera.lotus.api.menu.Menu;
import studio.mevera.lotus.api.menu.MenuView;
import studio.mevera.lotus.api.slot.Capacity;
import studio.mevera.lotus.api.slot.Slot;

@NotNullByDefault
public class DeleteActionMenu implements Menu<Component> {
    @Override
    public String name() {
        return MenuUtils.NPC_DELETE_ACTION;
    }

    @Override
    public Component title(MenuView<Component, ?> view) {
        return Msg.get(view.viewer(), "menus.action.delete.title");
    }

    @Override
    public Capacity capacity(MenuView<Component, ?> view) {
        return Capacity.ofRows(3);
    }

    @Override
    public Content content(MenuView<Component, ?> view) {
        Player player = view.viewer();

        Action action = view.data().require(Action.KEY);
        AwaitAction aa = view.data().require(Await.DELETE);

        return Content.builder(view.capacity())
            .fillAll(MenuItems.MENU_GLASS)
            .set(Slot.of(11), Button.clickable(
                ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)
                    .displayName(Msg.get(player, "menus.action.delete.confirm"))
                    .lore(Msg.lore(player.locale(), "menus.action.delete.confirm.lore",
                        Arg.arg(action.getId() + "#" + action.displayId()))
                    )
                    .build(), (_, _) -> aa.complete(action)))
            .set(Slot.of(15), Button.clickable(ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE)
                    .displayName(Msg.get(player, "items.go_back"))
                    .lore(Msg.get(player, "menus.delete.to_safety")).build(),
                (_, _) -> aa.cancel())).build();
    }
}
