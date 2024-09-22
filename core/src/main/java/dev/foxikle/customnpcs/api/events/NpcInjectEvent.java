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
import org.bukkit.entity.Player;

@Getter
@SuppressWarnings("unused")
public class NpcInjectEvent extends NpcEvent {

    private final double distanceSquared;

    /**
     * The constructor for the event
     *
     * @param player the player associated with the action
     * @param npc    the npc involved in the event
     */
    public NpcInjectEvent(Player player, InternalNpc npc, double distanceSquared) {
        super(player, npc);
        this.distanceSquared = distanceSquared;
    }

    /**
     * Calculates the distance between the npc and the player
     *
     * @return the distance betweem the player that is being injected and the npc that is being injected.
     */
    public double getDistance() {
        return Math.sqrt(distanceSquared);
    }
}
