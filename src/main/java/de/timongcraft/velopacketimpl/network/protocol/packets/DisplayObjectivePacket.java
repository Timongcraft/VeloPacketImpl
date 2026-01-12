package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.PacketCodec;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.ProtocolUtils.Direction;
import de.timongcraft.velopacketimpl.utils.packet.PacketRegistriesUtils;
import de.timongcraft.velopacketimpl.utils.packet.PacketRangeFactory;
import io.netty.buffer.ByteBuf;

import static com.velocitypowered.api.network.ProtocolVersion.*;
import static de.timongcraft.velopacketimpl.utils.packet.PacketRegistriesUtils.MultiVersionPacketProtocolState.PLAY;

/**
 * (latest) Resource ID: 'minecraft:set_display_objective'
 */
@SuppressWarnings("unused")
public class DisplayObjectivePacket implements MinecraftPacket {

    public static DisplayObjectivePacket of(int position, String scoreName) {
        return new DisplayObjectivePacket(position, scoreName);
    }

    public static void register(boolean encodeOnly) {
        PacketRegistriesUtils.register(PLAY, Direction.CLIENTBOUND,
                DisplayObjectivePacket.class, Codec.INSTANCE,
                PacketRangeFactory.entry(0x4C, MINECRAFT_1_18_2, encodeOnly),
                PacketRangeFactory.entry(0x4F, MINECRAFT_1_19_1, encodeOnly),
                PacketRangeFactory.entry(0x4D, MINECRAFT_1_19_3, encodeOnly),
                PacketRangeFactory.entry(0x51, MINECRAFT_1_19_4, encodeOnly),
                PacketRangeFactory.entry(0x53, MINECRAFT_1_20_2, encodeOnly),
                PacketRangeFactory.entry(0x55, MINECRAFT_1_20_3, encodeOnly),
                PacketRangeFactory.entry(0x57, MINECRAFT_1_20_5, encodeOnly),
                PacketRangeFactory.entry(0x5C, MINECRAFT_1_21_2, encodeOnly),
                PacketRangeFactory.entry(0x5B, MINECRAFT_1_21_5, encodeOnly),
                PacketRangeFactory.entry(0x60, MINECRAFT_1_21_9, encodeOnly));
    }

    private final int position;
    private final String scoreName;

    private DisplayObjectivePacket(int position, String scoreName) {
        this.position = position;
        this.scoreName = scoreName;
    }

    public static class Codec implements PacketCodec<DisplayObjectivePacket> {

        public static final Codec INSTANCE = new Codec();

        @Override
        public DisplayObjectivePacket decode(ByteBuf buf, Direction direction, ProtocolVersion version) {
            int position;
            if (version.noLessThan(MINECRAFT_1_20_2)) {
                position = ProtocolUtils.readVarInt(buf);
            } else {
                position = buf.readByte();
            }

            String scoreName = ProtocolUtils.readString(buf);

            return new DisplayObjectivePacket(position, scoreName);
        }

        @Override
        public void encode(DisplayObjectivePacket packet, ByteBuf buf, Direction direction, ProtocolVersion version) {
            if (version.noLessThan(MINECRAFT_1_20_2)) {
                ProtocolUtils.writeVarInt(buf, packet.position);
            } else {
                buf.writeByte(packet.position);
            }

            if (version.lessThan(MINECRAFT_1_20) && packet.scoreName.length() > 16) {
                throw new IllegalStateException("score name can only be 16 chars long");
            }
            ProtocolUtils.writeString(buf, packet.scoreName);
        }

    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return false;
    }

    public int position() {
        return position;
    }

    public String scoreName() {
        return scoreName;
    }

}