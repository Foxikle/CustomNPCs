package dev.foxikle.customnpcs.internal.network;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;

/**
 * The class to make a fake ServerGamePacketListenerImpl for the NPCs
 */
public class NetworkHandler extends ServerGamePacketListenerImpl {
    /**
     * <p> Creates a fake ServerGamePacketListenerImpl for NPCs
     * </p>
     * @param minecraftserver The server
     * @param connection The connection
     * @param entityplayer The NPC
     */
    public NetworkHandler(MinecraftServer minecraftserver, Connection connection, ServerPlayer entityplayer) {
        super(minecraftserver, connection, entityplayer);
    }
    /**
     * <p> Overrides the default ServerGamePacketListenerImpl's send packet method
     * </p>
     * @param packet The packet that won't be sent.
     */
    @Override
    public void send(@NotNull Packet<?> packet) {}
}
