package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.proxy.protocol.MinecraftPacket;

public abstract class VeloPacket implements MinecraftPacket {

    protected boolean decoded;

    public boolean isDecoded() {
        return decoded;
    }

}