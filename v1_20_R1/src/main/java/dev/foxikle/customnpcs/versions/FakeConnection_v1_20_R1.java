package dev.foxikle.customnpcs.versions;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.PacketFlow;

public class FakeConnection_v1_20_R1 extends Connection {
    /**
     * <p> Creates a fake Connection for NPC
     * </p>
     * @param enumprotocoldirection The protocol direction
     */
    public FakeConnection_v1_20_R1(PacketFlow enumprotocoldirection) {
        super(enumprotocoldirection);
    }

    @Override
    public void setListener(PacketListener packetListener) {

    }
}
