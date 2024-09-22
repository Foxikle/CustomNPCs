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

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import io.github.mqzen.menus.base.Content;
import io.github.mqzen.menus.base.Menu;
import io.github.mqzen.menus.base.MenuView;
import io.github.mqzen.menus.misc.Capacity;
import io.github.mqzen.menus.misc.DataRegistry;
import io.github.mqzen.menus.titles.MenuTitle;
import io.github.mqzen.menus.titles.MenuTitles;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.NotNull;

public class ActionCustomizerMenu implements Menu {
    /**
     * What's going to happen on opening of the menu's inventory
     *
     * @param playerMenuView the holder of the inventory opening
     * @param event          the inventory open event
     */
    @Override
    public void onOpen(MenuView<?> playerMenuView, InventoryOpenEvent event) {
        event.setCancelled(true);
        Player player = playerMenuView.getPlayer().orElse(null);
        if (player == null) return;
        CustomNPCs plugin = CustomNPCs.getInstance();
        Action action = plugin.editingActions.get(player.getUniqueId());
        if (action == null) return;

        plugin.getLotus().openMenu(player, action.getMenu());
    }

    /**
     * Creates the content for the menu
     *
     * @param extraData the data container for this menu for extra data
     * @param opener    the player opening this menu
     * @param capacity  the capacity set by the user above
     * @return the content of the menu to add (this includes items)
     */
    @Override
    public @NotNull Content getContent(DataRegistry extraData, Player opener, Capacity capacity) {
        return Content.builder(capacity)
                .apply(content -> content.fill(MenuItems.MENU_GLASS))
                .build();
    }

    /**
     * @param extraData the data container for this menu for extra data
     * @param opener    the player who is opening this menu
     * @return the capacity/size for this menu
     */
    @Override
    public @NotNull Capacity getCapacity(DataRegistry extraData, Player opener) {
        return Capacity.ofRows(5);
    }

    /**
     * @param extraData the data container for this menu for extra data
     * @param opener    the player who is opening this menu
     * @return the title for this menu
     */
    @Override
    public @NotNull MenuTitle getTitle(DataRegistry extraData, Player opener) {
        return MenuTitles.createModern(Component.empty());
    }

    /**
     * @return The unique name for this menu
     */
    @Override
    public String getName() {
        return MenuUtils.NPC_ACTION_CUSTOMIZER;
    }
}
