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

import dev.foxikle.customnpcs.api.NPC;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class NpcEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    protected final InternalNpc npc;
    @Setter
    private boolean cancelled;

    /**
     * The constructor for the event
     *
     * @param player the player associated with the action
     * @param npc    the npc involved in the event
     */
    public NpcEvent(Player player, InternalNpc npc) {
        super(player, false);
        this.npc = npc;
    }

    /**
     * A constructor for an NPC event with more control over its async status.
     *
     * @param player The player associated with the event
     * @param npc    The npc involved in the event
     * @param async  If this event is running on an async thread.
     */
    public NpcEvent(Player player, InternalNpc npc, boolean async) {
        super(player, async);
        this.npc = npc;
    }

    /**
     * Returns an API wrapped NPC object.
     *
     * @return The API npc object, with better deprecation.
     */
    public NPC apiNPC() {
        return new NPC(npc);
    }

    /**
     * @return the handler list of this event
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
