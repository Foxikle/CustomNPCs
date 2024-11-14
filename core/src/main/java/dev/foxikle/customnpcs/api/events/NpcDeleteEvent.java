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

import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class NpcDeleteEvent extends Event implements Cancellable {

    private final DeletionSource deletionSource;
    private final InternalNpc npc;
    @Setter
    private boolean cancelled;
    private final Player player;
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The constructor for the event
     *
     * @param player         the player associated with the action
     * @param npc            the npc involved in the event
     * @param deletionSource What the source of this deletion event is
     */
    public NpcDeleteEvent(@Nullable Player player, InternalNpc npc, DeletionSource deletionSource) {
        super(false);
        this.deletionSource = deletionSource;
        this.npc = npc;
        this.player = player;
    }

    /**
     * @return the handlerlist
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Describes the possible reasons for NPC deletion
     */
    public enum DeletionSource {
        /**
         * If the npc was deleted by running /npc delete
         */
        COMMAND,

        /**
         * If the NPC was deleted by using the button in the main menu
         */
        MENU,

        /**
         * If the npc was deleted via the API
         */
        API,

        /**
         * If, somehow, the plugin isn't sure how this npc was deleted
         */
        UNKNOWN
    }
}
