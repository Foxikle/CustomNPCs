/*
 * Copyright (c) 2026. Foxikle
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

package dev.foxikle.customnpcs.api.callbacks;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.internal.utils.Utils;
import dev.foxikle.customnpcs.internal.utils.await.Await;
import dev.foxikle.customnpcs.internal.utils.await.AwaitAction;
import lombok.AllArgsConstructor;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import studio.mevera.lotus.api.button.ClickAction;
import studio.mevera.lotus.api.menu.MenuView;

@AllArgsConstructor
public class ActionCreationCallback implements ClickAction {
    private final Action action;

    @Override
    public void onClick(@NotNull MenuView<?, ?> view, @NotNull InventoryClickEvent event) {
        event.setCancelled(true);
        Action impl = action.getDefaultValue().get();
        AwaitAction aa = view.data().get(Await.CREATION).orElseThrow();
        Utils.sound(view, Sound.UI_BUTTON_CLICK);
        view.lotus().openMenu(view.viewer(), action.getMenu(), view.data()
                .put(Await.CREATION, aa)
                .put(Action.KEY, impl));

    }
}
