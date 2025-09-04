/*
 * Copyright (c) 2024-2025. Foxikle
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

import dev.foxikle.customnpcs.conditions.Condition;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * A class representing an event called upon the injection of an NPC.
 */
@Getter
@SuppressWarnings("unused")
public class NpcInjectEvent extends NpcEvent {

    private final double distanceSquared;
    private final Map<Condition, Boolean> conditions;

    /**
     * The constructor for the event
     *
     * @param player the player associated with the action
     * @param npc    the npc involved in the event
     * @deprecated Use the constructor with the conditions
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.9.0")
    public NpcInjectEvent(Player player, InternalNpc npc, double distanceSquared) {
        super(player, npc, true);
        this.distanceSquared = distanceSquared;
        conditions = new HashMap<>();
    }

    /**
     * The constructor for the event
     *
     * @param player the player associated with the action
     * @param npc    the npc involved in the event
     */
    public NpcInjectEvent(Player player, InternalNpc npc, double distanceSquared, Map<Condition, Boolean> conditions) {
        super(player, npc, true);
        this.distanceSquared = distanceSquared;
        this.conditions = conditions;
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
