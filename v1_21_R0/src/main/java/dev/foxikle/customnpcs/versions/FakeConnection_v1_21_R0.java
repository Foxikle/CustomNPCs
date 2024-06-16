package dev.foxikle.customnpcs.versions;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;

/**
 * A fake connection for the NPCs
 */
public class FakeConnection_v1_21_R0 extends Connection {
    /**
     * <p> Creates a fake Connection for NPC
     * </p>
     * @param enumprotocoldirection The protocol direction
     */
    public FakeConnection_v1_21_R0(PacketFlow enumprotocoldirection) {
        super(enumprotocoldirection);
    }
}
