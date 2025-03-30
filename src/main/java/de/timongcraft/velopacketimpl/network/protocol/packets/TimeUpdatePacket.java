package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration;
import io.netty.buffer.ByteBuf;

/**
 * (latest) Resource Id: 'minecraft:set_time'
 */
@SuppressWarnings("unused")
public class TimeUpdatePacket extends VeloPacket {

    public static void register(boolean encodeOnly) {
        PacketRegistration.of(TimeUpdatePacket.class)
                .direction(ProtocolUtils.Direction.CLIENTBOUND)
                .packetSupplier(TimeUpdatePacket::new)
                .stateRegistry(StateRegistry.PLAY)
                .mapping(0x59, ProtocolVersion.MINECRAFT_1_18_2, encodeOnly)
                .mapping(0x5C, ProtocolVersion.MINECRAFT_1_19_1, encodeOnly)
                .mapping(0x5A, ProtocolVersion.MINECRAFT_1_19_3, encodeOnly)
                .mapping(0x5E, ProtocolVersion.MINECRAFT_1_19_4, encodeOnly)
                .mapping(0x60, ProtocolVersion.MINECRAFT_1_20_2, encodeOnly)
                .mapping(0x62, ProtocolVersion.MINECRAFT_1_20_3, encodeOnly)
                .mapping(0x64, ProtocolVersion.MINECRAFT_1_20_5, encodeOnly)
                .mapping(0x6B, ProtocolVersion.MINECRAFT_1_21_2, encodeOnly)
                .mapping(0x6A, ProtocolVersion.MINECRAFT_1_21_5, encodeOnly)
                .register();
    }

    private long worldAge;
    /**
     * As this is a cross version packet impl this will always be positive.
     * To stop the time form advancing:
     *
     * @see #tickDayTime
     */
    private long timeOfDay;
    private boolean tickDayTime;

    public TimeUpdatePacket() {}

    public TimeUpdatePacket(long timeOfDay, boolean tickDayTime) {
        this(0, timeOfDay, tickDayTime);
    }

    public TimeUpdatePacket(long worldAge, long timeOfDay, boolean tickDayTime) {
        this.worldAge = worldAge;
        setTimeOfDay(timeOfDay);
        this.tickDayTime = tickDayTime;
    }

    @Override
    public void decode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        decoded = true;

        worldAge = buffer.readLong();
        setTimeOfDay(buffer.readLong());

        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_21_2))
            tickDayTime = buffer.readBoolean();
    }

    @Override
    public void encode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        buffer.writeLong(worldAge);

        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_21_2)) {
            buffer.writeLong(timeOfDay);
        } else {
            // handle that timeOfDay is always positive
            buffer.writeLong(tickDayTime ? timeOfDay : -timeOfDay);
        }

        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_21_2))
            buffer.writeBoolean(tickDayTime);
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
        if (timeOfDay >= 0) {
            this.timeOfDay = timeOfDay;
            tickDayTime = true;
        } else {
            // old/negative time handling might be removed in the future

            // make timeOfDay always positive, as tickDayTime should be used to determine if the time advancing
            this.timeOfDay = -timeOfDay;
            tickDayTime = false;
        }
    }

    public boolean isTickDayTime() {
        return tickDayTime;
    }

    public void setTickDayTime(boolean tickDayTime) {
        this.tickDayTime = tickDayTime;
    }

}