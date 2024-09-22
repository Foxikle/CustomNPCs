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

package dev.foxikle.customnpcs.api;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

/**
 * A class providing an interface using non NMS objects to use NPCs
 */
public class NPCApi {
    /**
     * A static instance of the plugin for API use.
     */
    protected static CustomNPCs plugin = JavaPlugin.getPlugin(CustomNPCs.class);

    /**
     * Initiailizes the API
     *
     * @deprecated since it's no longer necessary
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.8")
    public static void initialize() {
        plugin = JavaPlugin.getPlugin(CustomNPCs.class);
    }

    /**
     * Gets the NPC object by ID.
     *
     * @param uuid the id of the NPC
     * @return the NPC object associated with the NPC
     * @throws NullPointerException     if the specified UUID is null
     * @throws IllegalArgumentException if an NPC doesn't exist by that UUID
     */
    public static NPC getNPC(UUID uuid) throws NullPointerException, IllegalArgumentException {
        return new NPC(plugin.getNPCByID(uuid));
    }
}
