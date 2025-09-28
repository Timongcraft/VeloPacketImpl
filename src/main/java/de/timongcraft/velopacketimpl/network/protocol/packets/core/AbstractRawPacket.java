package de.timongcraft.velopacketimpl.network.protocol.packets.core;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.util.DeferredByteBufHolder;
import io.netty.buffer.ByteBuf;

public class AbstractRawPacket extends DeferredByteBufHolder implements VeloPacket {

    private boolean decoded;

    protected AbstractRawPacket() {
        super(null);
    }

    @Override
    public final void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        this.decoded = true;
        replace(buf.readRetainedSlice(buf.readableBytes()));
    }

    @Override
    public final void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        buf.writeBytes(content());
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