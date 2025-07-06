package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;

public abstract class VeloPacket implements MinecraftPacket {

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return false;
    }

    protected boolean decoded;

    public boolean isDecoded() {
        return decoded;
    }

}