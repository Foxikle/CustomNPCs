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

package dev.foxikle.customnpcs.internal.utils;

import io.github.mqzen.menus.base.Menu;
import io.github.mqzen.menus.base.MenuView;
import io.github.mqzen.menus.misc.button.actions.ButtonClickAction;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class OpenButtonAction implements ButtonClickAction {

    private final String id;
    private final Consumer<Player> action;

    public OpenButtonAction(String id) {
        this.id = id;
        this.action = player -> player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
    }

    public OpenButtonAction(String id, Consumer<Player> beforeOpen) {
        this.id = id;
        this.action = beforeOpen;
    }

    public OpenButtonAction(Menu menu) {
        this(menu.getName());
    }

    @Override
    public String tag() {
        return "OPEN";
    }

    @Override
    public void execute(MenuView<?> menuView, InventoryClickEvent inventoryClickEvent) {
        Player player = (Player) inventoryClickEvent.getWhoClicked();
        if (action != null) {
            action.accept(player);
        }
        menuView.getAPI().openMenu(player, id);
    }
}
