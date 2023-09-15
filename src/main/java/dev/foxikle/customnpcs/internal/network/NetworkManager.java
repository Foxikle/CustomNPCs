package dev.foxikle.customnpcs.internal.network;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;

/**
 * The object that fakes the NPC's connection
 */
public class NetworkManager extends Connection {
    /**
     * <p> Creates a fake Connection for NPCs
     * </p>
     * @param enumprotocoldirection The protocol direction
     */
    public NetworkManager(PacketFlow enumprotocoldirection) {
        super(enumprotocoldirection);
    }
}
