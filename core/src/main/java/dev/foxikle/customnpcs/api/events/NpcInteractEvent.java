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

package dev.foxikle.customnpcs.api.events;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings("unused")
public class NpcInteractEvent extends NpcEvent {

    /**
     * The constructor for the event
     *
     * @param player the player associated with the action
     * @param npc    the npc involved in the event
     */
    public NpcInteractEvent(Player player, InternalNpc npc) {
        super(player, npc);
    }

    /**
     * Gets the actions that will be executed upon interaction
     *
     * @return the actions that pass their associated conditions
     */
    public List<Action> getExecutingActions() {
        return npc.getActions().stream()
                .filter(action -> action.processConditions(player))
                .toList();
    }

    /**
     * Gets the actions that failed their condition checks.
     *
     * @return the list of actions that will not be executed this interaction because
     * the player interacting does not fufill the conditions.
     */
    public List<Action> getFailingActions() {
        return npc.getActions().stream()
                .filter(action -> !action.processConditions(player))
                .toList();
    }
}
