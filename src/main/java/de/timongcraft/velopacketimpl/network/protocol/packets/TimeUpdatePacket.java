package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration;
import io.netty.buffer.ByteBuf;

@SuppressWarnings("unused")
public class TimeUpdatePacket extends VeloPacket {

    public static void register(boolean encodeOnly) {
        PacketRegistration.of(TimeUpdatePacket.class)
                .direction(ProtocolUtils.Direction.CLIENTBOUND)
                .packetSupplier(TimeUpdatePacket::new)
                .stateRegistry(StateRegistry.PLAY)
                .mapping(0x59, ProtocolVersion.MINECRAFT_1_18_2, encodeOnly)
                .mapping(0x59, ProtocolVersion.MINECRAFT_1_19, encodeOnly)
                .mapping(0x5C, ProtocolVersion.MINECRAFT_1_19_1, encodeOnly)
                .mapping(0x5A, ProtocolVersion.MINECRAFT_1_19_3, encodeOnly)
                .mapping(0x5E, ProtocolVersion.MINECRAFT_1_19_4, encodeOnly)
                .mapping(0x5E, ProtocolVersion.MINECRAFT_1_20, encodeOnly)
                .mapping(0x60, ProtocolVersion.MINECRAFT_1_20_2, encodeOnly)
                .mapping(0x62, ProtocolVersion.MINECRAFT_1_20_3, encodeOnly)
                .mapping(0x64, ProtocolVersion.MINECRAFT_1_20_5, encodeOnly)
                .mapping(0x64, ProtocolVersion.MINECRAFT_1_21, encodeOnly)
                .register();
    }

    private long worldAge;
    /**
     * If negative the client will not advance time on its on
     */
    private long timeOfDay;

    public TimeUpdatePacket() {}

    public TimeUpdatePacket(long timeOfDay) {
        this(0, timeOfDay);
    }

    public TimeUpdatePacket(long worldAge, long timeOfDay) {
        this.worldAge = worldAge;
        this.timeOfDay = timeOfDay;
    }

    @Override
    public void decode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        decoded = true;

        worldAge = buffer.readLong();
        timeOfDay = buffer.readLong();
    }

    @Override
    public void encode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        buffer.writeLong(worldAge);
        buffer.writeLong(timeOfDay);
    }

    @Override
    public boolean handle(MinecraftSessionHandler minecraftSessionHandler) {
        return false;
    }

    public long getWorldAge() {
        return worldAge;
    }

    public void setWorldAge(long worldAge) {
        this.worldAge = worldAge;
    }

    public long getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(long timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

}
