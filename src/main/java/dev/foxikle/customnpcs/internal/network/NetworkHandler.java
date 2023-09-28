package dev.foxikle.customnpcs.internal.network;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;

/**
 * The class to make a fake ServerGamePacketListenerImpl for the NPCs
 */
public class NetworkHandler extends ServerGamePacketListenerImpl {
    /**
     * <p> Creates a fake ServerGamePacketListenerImpl for NPCs
     * </p>
     * @param server The server
     * @param connection The connection
     * @param npc The NPC
     */
    public NetworkHandler(MinecraftServer server, Connection connection, ServerPlayer npc) {
        super(server, connection, npc, CommonListenerCookie.createInitial(npc.gameProfile));
    }
    /**
     * <p> Overrides the default ServerGamePacketListenerImpl's send packet method
     * </p>
     * @param packet The packet that won't be sent.
     */
    @Override
    public void send(@NotNull Packet<?> packet) {}
}
