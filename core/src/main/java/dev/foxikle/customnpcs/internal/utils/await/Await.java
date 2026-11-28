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

package dev.foxikle.customnpcs.internal.utils.await;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.api.Skin;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.menu.MenuUtils;
import dev.foxikle.customnpcs.internal.utils.WaitingType;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;
import studio.mevera.lotus.Lotus;
import studio.mevera.lotus.api.data.DataRegistry;
import studio.mevera.lotus.api.data.Key;
import studio.mevera.lotus.api.menu.Menu;

import java.util.concurrent.CompletableFuture;

/**
 * A class used to facilitate callbacks, mostly for menus.
 */
@UtilityClass
public final class Await {

    public static final Key<AwaitAction> CREATION = Key.of("action_creation", AwaitAction.class);
    //todo: implement me!
    public static final Key<AwaitAction> CLONE = Key.of("action_clone", AwaitAction.class);
    public static final Key<AwaitAction> DELETE = Key.of("action_delete", AwaitAction.class);


    /**
     * Awaits the user's creation of an action. This Opens a menu for the player.
     */
    public static CompletableFuture<Result<Action>> createAction(Player p, InternalNpc npc) {
        Lotus<Component> l = CustomNPCs.getInstance().getLotus();
        AwaitAction ac = new AwaitAction();
        Menu<Component> menu = l.registeredMenu(MenuUtils.NPC_NEW_ACTION).orElseThrow();
        l.openMenu(p, menu, DataRegistry.empty().put(CREATION, ac).put(InternalNpc.KEY, npc));
        return ac.future();
    }

    /**
     * Awaits the user's deletion of an action. This Opens a menu for the player.
     */
    public static CompletableFuture<Result<Action>> deleteAction(Player p, InternalNpc npc, Action action) {
        Lotus<Component> l = CustomNPCs.getInstance().getLotus();
        AwaitAction ac = new AwaitAction();
        Menu<Component> menu = l.registeredMenu(MenuUtils.NPC_DELETE_ACTION).orElseThrow();
        l.openMenu(p, menu, DataRegistry.empty()
            .put(Action.KEY, action)
            .put(DELETE, ac)
            .put(InternalNpc.KEY, npc));
        return ac.future();
    }

    public static CompletableFuture<Result<String>> input(Player p, WaitingType type, DataRegistry data) {
        if (type.inputType() != AwaitString.class)
            throw new IllegalArgumentException("WaitingType " + type + " does not have an input type of AwaitString");
        AwaitString as = new AwaitString(type, data);
        return input(as, p);
    }

    public static CompletableFuture<Result<Void>> signal(Player p, WaitingType type, DataRegistry data) {
        if (type.inputType() != AwaitSignal.class)
            throw new IllegalArgumentException("WaitingType " + type + " does not have an input type of AwaitSignal");
        AwaitSignal as = new AwaitSignal(type, data);
        return input(as, p);
    }

    public static CompletableFuture<Result<Skin>> skin(Player p, WaitingType type, DataRegistry data) {
        if (type.inputType() != AwaitSkin.class)
            throw new IllegalArgumentException("WaitingType " + type + " does not have an input type of AwaitSkin");

        AwaitSkin as = new AwaitSkin(type, data);
        return input(as, p);
    }

    private static <T> CompletableFuture<Result<T>> input(AwaitInput<T> input, Player p) {
        CustomNPCs.getInstance().wait(p, input);
        p.closeInventory();
        return input.future();
    }

    public static CompletableFuture<Result<Action>> cloneAction(Player p, InternalNpc npc) {
        return null;
    }
}
