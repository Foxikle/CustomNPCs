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

package dev.foxikle.customnpcs.internal.utils;

import dev.foxikle.customnpcs.actions.Action;
import io.github.mqzen.menus.misc.button.Button;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ActionRegistry {
    // Editable, Duplicatable, Delayable
    private static final boolean[] DEFAULT_SETTINGS = new boolean[]{true, true, true};

    private final Map<String, Class<? extends Action>> idToClass = new HashMap<>();
    private final Map<Class<? extends Action>, ParameterizedSupplier<Button, Player>> classToButton = new HashMap<>();
    private final Map<Class<? extends Action>, boolean[]> classToSettings = new HashMap<>();

    /**
     * Default constructor
     */
    public ActionRegistry() {
        // default constructor
    }


    public void register(String id, Class<? extends Action> clazz, ParameterizedSupplier<Button, Player> button, boolean canEdit, boolean canDuplicate, boolean canDelay) {
        register(id, clazz, button, new boolean[]{canEdit, canDuplicate, canDelay});
    }

    public void register(String id, Class<? extends Action> clazz, ParameterizedSupplier<Button, Player> button) {
        register(id, clazz, button, DEFAULT_SETTINGS);
    }

    private void register(String id, Class<? extends Action> clazz, ParameterizedSupplier<Button, Player> button, boolean[] settings) {
        idToClass.put(id, clazz);
        classToButton.put(clazz, button);
        classToSettings.put(clazz, settings);
    }

    public boolean canEdit(Class<? extends Action> clazz) {
        if (classToSettings.containsKey(clazz)) return classToSettings.get(clazz)[0];
        throw new IllegalStateException("The class " + clazz.getSimpleName() + " has not been registered.");
    }

    public boolean canEdit(String id) {
        if (idToClass.containsKey(id)) return canEdit(idToClass.get(id));
        throw new IllegalStateException("The action " + id + " has not been registered.");
    }

    public boolean canDuplicate(Class<? extends Action> clazz) {
        if (classToSettings.containsKey(clazz)) return classToSettings.get(clazz)[1];
        throw new IllegalStateException("The class " + clazz.getSimpleName() + " has not been registered.");
    }

    public boolean canDuplicate(String id) {
        if (idToClass.containsKey(id)) return canDuplicate(idToClass.get(id));
        throw new IllegalStateException("The action " + id + " has not been registered.");
    }

    public boolean canDelay(Class<? extends Action> clazz) {
        if (classToSettings.containsKey(clazz)) return classToSettings.get(clazz)[1];
        throw new IllegalStateException("The class " + clazz.getSimpleName() + " has not been registered.");
    }

    public boolean canDelay(String id) {
        if (idToClass.containsKey(id)) return canDelay(idToClass.get(id));
        throw new IllegalStateException("The action " + id + " has not been registered.");
    }

    public Button getButton(Class<? extends Action> clazz, Player player) {
        if (classToButton.containsKey(clazz)) return classToButton.get(clazz).get(player);
        throw new IllegalStateException("The class " + clazz.getSimpleName() + " has not been registered.");
    }

    public Button getButton(String id, Player player) {
        if (idToClass.containsKey(id)) return getButton(idToClass.get(id), player);
        throw new IllegalStateException("The action " + id + " has not been registered.");
    }

    public Collection<ParameterizedSupplier<Button, Player>> getButtons() {
        return classToButton.values();
    }

    public Collection<Button> getButtons(Player locale) {
        return classToButton.values().stream().map(button -> button.get(locale)).toList();
    }

    public Class<? extends Action> getActionClass(String id) {
        return idToClass.get(id);
    }

}
