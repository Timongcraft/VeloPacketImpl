package de.timongcraft.velopacketimpl.network.protocol.packets.core;

import com.velocitypowered.proxy.protocol.MinecraftPacket;

public interface VeloPacket extends MinecraftPacket {

    boolean isDecoded();

}