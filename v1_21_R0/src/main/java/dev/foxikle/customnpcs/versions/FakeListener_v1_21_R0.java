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
public class FakeListener_v1_21_R0 extends ServerGamePacketListenerImpl {
    /**
     * <p> Creates a fake ServerGamePacketListenerImpl for NPCs
     * </p>
     * @param server The server
     * @param connection The connection
     * @param npc The NPC
     */
    public FakeListener_v1_21_R0(MinecraftServer server, Connection connection, ServerPlayer npc) {
        super(server, connection, npc, CommonListenerCookie.createInitial(npc.gameProfile, false));
    }
    /**
     * <p> Overrides the default ServerGamePacketListenerImpl's send packet method
     * </p>
     * @param packet The packet that won't be sent.
     */
    @Override
    public void send(@NotNull Packet<?> packet) {}
}
