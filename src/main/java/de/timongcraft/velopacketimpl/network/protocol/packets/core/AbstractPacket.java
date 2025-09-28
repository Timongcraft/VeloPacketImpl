package de.timongcraft.velopacketimpl.network.protocol.packets.core;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;

public abstract class AbstractPacket implements VeloPacket {

    private boolean decoded;

    @Override
    public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
        this.decoded = true;
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return false;
    }

    @Override
    public final boolean isDecoded() {
        return decoded;
    }

}