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

import dev.foxikle.customnpcs.actions.Action;
import io.github.mqzen.menus.misc.button.Button;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class ActionRegistry {


    private final Map<String, Action> idToInstance = new HashMap<>();
    private final Map<Class<? extends Action>, Action> classToInstance = new HashMap<>();

    public void register(Action action) {
        if (idToInstance.containsKey(action.getId())) {
            throw new IllegalArgumentException("An action with the ID '" + action.getId() + "' has already been " +
                    "registered!");
        }
        if (classToInstance.containsKey(action.getClass())) {
            throw new IllegalArgumentException("An action with the class '" + action.getClass().getName() + "' has " +
                    "already been registered!");
        }
        idToInstance.put(action.getId(), action);
        classToInstance.put(action.getClass(), action);
    }

    public Class<? extends Action> getActionClass(String id) {
        return idToInstance.get(id).getClass();
    }

    public Action getAction(String id) {
        return idToInstance.get(id);
    }

    public Action getAction(Class<? extends Action> action) {
        return classToInstance.get(action);
    }

    public List<Button> getButtons(Player player) {
        List<Button> buttons = new ArrayList<>();
        idToInstance.forEach((_, action) -> buttons.add(action.creationButton(player)));
        return buttons;
    }

    public void clear() {
        idToInstance.clear();
        classToInstance.clear();
    }
}
