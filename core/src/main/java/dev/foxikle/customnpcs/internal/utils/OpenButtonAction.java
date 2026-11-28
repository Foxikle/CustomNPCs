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

package dev.foxikle.customnpcs.internal.utils;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.menu.MenuUtils;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import studio.mevera.lotus.api.button.ClickAction;
import studio.mevera.lotus.api.data.DataRegistry;
import studio.mevera.lotus.api.menu.Menu;
import studio.mevera.lotus.api.menu.MenuView;

import java.util.function.Consumer;

@AllArgsConstructor
public class OpenButtonAction implements ClickAction {

    private final Menu<Component> menu;
    private final DataRegistry data;
    private final Consumer<MenuView<?,?>> action;


    public OpenButtonAction(String id, DataRegistry data) {
        this.menu = CustomNPCs.getInstance().getLotus().registeredMenu(id).orElseThrow();
        this.action = v -> Utils.sound(v, Sound.UI_BUTTON_CLICK);
        this.data = data;
    }

    public OpenButtonAction(@NotNull Menu<Component> menu, DataRegistry data) {
        this.menu = menu;
        this.action = v -> Utils.sound(v, Sound.UI_BUTTON_CLICK);
        this.data = data;
    }

    @Override
    public void onClick(@NotNull MenuView<?, ?> view, @NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (action != null) {
            action.accept(view);
        }
        MenuUtils.open(player, menu, data);
    }
}
