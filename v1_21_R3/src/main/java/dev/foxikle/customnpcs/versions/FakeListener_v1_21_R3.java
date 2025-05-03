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

package dev.foxikle.customnpcs.versions;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;

/**
 * A fake packet listener for the NPCs
 */
public class FakeListener_v1_21_R3 extends ServerGamePacketListenerImpl {
    /**
     * <p> Creates a fake ServerGamePacketListenerImpl for NPCs
     * </p>
     *
     * @param server     The server
     * @param connection The connection
     * @param npc        The NPC
     */
    public FakeListener_v1_21_R3(MinecraftServer server, Connection connection, ServerPlayer npc) {
        super(server, connection, npc, CommonListenerCookie.createInitial(npc.gameProfile, false));
    }

    /**
     * <p> Overrides the default ServerGamePacketListenerImpl's send packet method
     * </p>
     *
     * @param packet The packet that won't be sent.
     */
    @Override
    public void send(@NotNull Packet<?> packet) {
    }
}
