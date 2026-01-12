package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.PacketCodec;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import de.timongcraft.velopacketimpl.utils.packet.PacketRegistriesUtils;
import de.timongcraft.velopacketimpl.utils.packet.PacketRangeFactory;
import io.netty.buffer.ByteBuf;

import static com.velocitypowered.api.network.ProtocolVersion.*;
import static de.timongcraft.velopacketimpl.utils.packet.PacketRegistriesUtils.MultiVersionPacketProtocolState.PLAY;

/**
 * (latest) Resource ID: 'minecraft:set_time'
 *
 */
@SuppressWarnings("unused")
public class TimeUpdatePacket implements MinecraftPacket {

    /**
     * @param timeOfDay Always positive, to stop the time from advancing, see {@link #tickDayTime}.
     */
    public static TimeUpdatePacket of(long timeOfDay, boolean tickDayTime) {
        return of(0, timeOfDay, tickDayTime);
    }

    /**
     * @param timeOfDay Always positive, to stop the time from advancing, see {@link #tickDayTime}.
     */
    public static TimeUpdatePacket of(long worldAge, long timeOfDay, boolean tickDayTime) {
        return new TimeUpdatePacket(worldAge, timeOfDay, tickDayTime);
    }

    public static void register(boolean encodeOnly) {
        PacketRegistriesUtils.register(PLAY, ProtocolUtils.Direction.CLIENTBOUND,
                TimeUpdatePacket.class, Codec.INSTANCE,
                PacketRangeFactory.entry(0x59, MINECRAFT_1_18_2, encodeOnly),
                PacketRangeFactory.entry(0x5C, MINECRAFT_1_19_1, encodeOnly),
                PacketRangeFactory.entry(0x5A, MINECRAFT_1_19_3, encodeOnly),
                PacketRangeFactory.entry(0x5E, MINECRAFT_1_19_4, encodeOnly),
                PacketRangeFactory.entry(0x60, MINECRAFT_1_20_2, encodeOnly),
                PacketRangeFactory.entry(0x62, MINECRAFT_1_20_3, encodeOnly),
                PacketRangeFactory.entry(0x64, MINECRAFT_1_20_5, encodeOnly),
                PacketRangeFactory.entry(0x6B, MINECRAFT_1_21_2, encodeOnly),
                PacketRangeFactory.entry(0x6A, MINECRAFT_1_21_5, encodeOnly),
                PacketRangeFactory.entry(0x6F, MINECRAFT_1_21_9, encodeOnly));
    }

    private final long worldAge;
    private final long timeOfDay;
    private final boolean tickDayTime;

    private TimeUpdatePacket(long worldAge, long timeOfDay, boolean tickDayTime) {
        this.worldAge = worldAge;
        this.timeOfDay = timeOfDay;
        this.tickDayTime = tickDayTime;
    }

    public static class Codec implements PacketCodec<TimeUpdatePacket> {

        public static final Codec INSTANCE = new Codec();

        @Override
        public TimeUpdatePacket decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
            long worldAge = buf.readLong();
            long timeOfDay = buf.readLong();
            boolean tickDayTime = false;

            if (version.noLessThan(MINECRAFT_1_21_2)) {
                tickDayTime = buf.readBoolean();
            } else {
                if (timeOfDay >= 0) {
                    tickDayTime = true;
                } else {
                    // make timeOfDay variable always positive, tickDayTime should now be used to determine if the time advancing
                    timeOfDay = -timeOfDay;
                }
            }

            return new TimeUpdatePacket(worldAge, timeOfDay, tickDayTime);
        }

        @Override
        public void encode(TimeUpdatePacket packet, ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
            buf.writeLong(packet.worldAge);

            if (version.noLessThan(MINECRAFT_1_21_2)) {
                buf.writeLong(packet.timeOfDay);

                buf.writeBoolean(packet.tickDayTime);
            } else {
                buf.writeLong(packet.tickDayTime ? packet.timeOfDay : -packet.timeOfDay);
            }
        }

    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return false;
    }

    public long worldAge() {
        return worldAge;
    }

    public long timeOfDay() {
        return timeOfDay;
    }

    public boolean tickDayTime() {
        return tickDayTime;
    }

}