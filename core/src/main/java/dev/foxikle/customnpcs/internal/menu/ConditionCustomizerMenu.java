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

import dev.foxikle.customnpcs.conditions.Condition;
import dev.foxikle.customnpcs.conditions.TextCondition;
import dev.foxikle.customnpcs.internal.utils.Msg;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNullByDefault;
import studio.mevera.lotus.api.content.Content;
import studio.mevera.lotus.api.menu.Menu;
import studio.mevera.lotus.api.menu.MenuView;
import studio.mevera.lotus.api.slot.Capacity;
import studio.mevera.lotus.api.slot.Slot;

@NotNullByDefault
public class ConditionCustomizerMenu implements Menu<Component> {
    @Override
    public String name() {
        return MenuUtils.NPC_CONDITION_CUSTOMIZER;
    }

    @Override
    public Component title(MenuView<Component, ?> view) {
        return Msg.get(view.viewer(), "menus.condition_customizer.title");
    }

    @Override
    public Capacity capacity(MenuView<Component, ?> view) {
        return Capacity.ofRows(3);
    }

    @Override
    public Content content(MenuView<Component, ?> view) {
        Condition condition = view.data().getOrNull(Condition.KEY);
        if (condition instanceof TextCondition text) {
            return getText(view, text, view.capacity());
        }
        return Content.builder(view.capacity())
                .fillAll(MenuItems.MENU_GLASS)
            .set(Slot.of(18), MenuItems.toNewCondition(view))
            .set(Slot.of(22), MenuItems.saveCondition(view))
            .set(Slot.of(11), MenuItems.comparatorSwitcher(condition, view, 11))
            .set(Slot.of(13), MenuItems.targetValueSelector(view))
            .set(Slot.of(15), MenuItems.valueSwitcher(condition, view, 15, 13))
                .build();
    }

    private Content getText(MenuView<?, ?> view, TextCondition condition, Capacity capacity) {
        return Content.builder(capacity)
                .fillAll(MenuItems.MENU_GLASS)
            .set(Slot.of(18), MenuItems.toNewCondition(view))
            .set(Slot.of(22), MenuItems.saveCondition(view))
            .set(Slot.of(10), MenuItems.comparatorSwitcher(condition, view, 10))
            .set(Slot.of(12), MenuItems.targetValueSelector(view))
            .set(Slot.of(14), MenuItems.valueSwitcher(condition, view, 14, 12))
            .set(Slot.of(16), MenuItems.toggleTextConditionInversion(condition, view))
                .build();
    }
}
