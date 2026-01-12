package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.PacketCodec;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import de.timongcraft.velopacketimpl.utils.annotations.Since;
import de.timongcraft.velopacketimpl.utils.annotations.Until;
import de.timongcraft.velopacketimpl.utils.network.PlayerPosition;
import de.timongcraft.velopacketimpl.utils.packet.PacketRegistriesUtils;
import de.timongcraft.velopacketimpl.utils.packet.PacketRangeFactory;
import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.Set;

import static com.velocitypowered.api.network.ProtocolVersion.*;
import static de.timongcraft.velopacketimpl.utils.packet.PacketRegistriesUtils.MultiVersionPacketProtocolState.PLAY;

/**
 * (latest) Resource ID: 'minecraft:player_position'
 */
@SuppressWarnings("unused")
public class SynchronizePlayerPositionPacket implements MinecraftPacket {

    @Until(MINECRAFT_1_19_3)
    public static SynchronizePlayerPositionPacket ofLegacy(int teleportId, PlayerPosition pos, Set<Flag> flags, boolean dismountVehicle) {
        return new SynchronizePlayerPositionPacket(teleportId, pos, flags, dismountVehicle);
    }

    public static SynchronizePlayerPositionPacket of(int teleportId, PlayerPosition pos, Set<Flag> flags) {
        return new SynchronizePlayerPositionPacket(teleportId, pos, flags, false);
    }

    public static void register(boolean encodeOnly) {
        PacketRegistriesUtils.register(PLAY, ProtocolUtils.Direction.CLIENTBOUND,
                SynchronizePlayerPositionPacket.class, Codec.INSTANCE,

                PacketRangeFactory.entry(0x38, MINECRAFT_1_18_2, encodeOnly),
                PacketRangeFactory.entry(0x36, MINECRAFT_1_19, encodeOnly),
                PacketRangeFactory.entry(0x39, MINECRAFT_1_19_1, encodeOnly),
                PacketRangeFactory.entry(0x38, MINECRAFT_1_19_3, encodeOnly),
                PacketRangeFactory.entry(0x3C, MINECRAFT_1_19_4, encodeOnly),
                PacketRangeFactory.entry(0x3E, MINECRAFT_1_20_2, encodeOnly),
                PacketRangeFactory.entry(0x40, MINECRAFT_1_20_5, encodeOnly),
                PacketRangeFactory.entry(0x42, MINECRAFT_1_21_2, encodeOnly),
                PacketRangeFactory.entry(0x41, MINECRAFT_1_21_5, encodeOnly),
                PacketRangeFactory.entry(0x46, MINECRAFT_1_21_9, encodeOnly));
    }

    private final int teleportId;
    private final PlayerPosition pos;
    private final Set<Flag> flags;
    @Until(MINECRAFT_1_19_3)
    private final boolean dismountVehicle;

    private SynchronizePlayerPositionPacket(int teleportId, PlayerPosition pos, Set<Flag> flags,
                                           @Until(MINECRAFT_1_19_3) boolean dismountVehicle) {
        this.teleportId = teleportId;
        this.pos = pos;
        this.flags = flags;
        this.dismountVehicle = dismountVehicle;
    }

    public static class Codec implements PacketCodec<SynchronizePlayerPositionPacket> {

        public static final Codec INSTANCE = new Codec();

        @Override
        public SynchronizePlayerPositionPacket decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
            int teleportId;
            PlayerPosition pos;
            Set<Flag> flags;
            @Until(MINECRAFT_1_19_3)
            boolean dismountVehicle = false;

            if (version.noLessThan(MINECRAFT_1_21_2)) {
                teleportId = ProtocolUtils.readVarInt(buf);

                pos = PlayerPosition.read(buf, false);

                flags = Flag.getFlags(buf.readInt());
            } else {
                pos = PlayerPosition.read(buf, true);

                flags = Flag.getFlags(buf.readUnsignedByte());

                teleportId = ProtocolUtils.readVarInt(buf);

                if (version.noGreaterThan(MINECRAFT_1_19_3)) {
                    dismountVehicle = buf.readBoolean();
                }
            }

            return new SynchronizePlayerPositionPacket(teleportId, pos, flags, dismountVehicle);
        }

        @Override
        public void encode(SynchronizePlayerPositionPacket packet, ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
            if (version.noLessThan(MINECRAFT_1_21_2)) {
                ProtocolUtils.writeVarInt(buf, packet.teleportId);

                packet.pos.write(buf, false);

                buf.writeInt(Flag.getBitfield(packet.flags));
            } else {
                packet.pos.write(buf, true);

                buf.writeByte(Flag.getBitfield(packet.flags));

                ProtocolUtils.writeVarInt(buf, packet.teleportId);

                if (version.noGreaterThan(MINECRAFT_1_19_3)) {
                    buf.writeBoolean(packet.dismountVehicle);
                }
            }
        }

    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return false;
    }

    public int teleportId() {
        return teleportId;
    }

    public PlayerPosition pos() {
        return pos;
    }

    public Set<Flag> flags() {
        return flags;
    }

    /**
     * @implNote Returns false for versions >1.19.3
     */
    @Until(MINECRAFT_1_19_3)
    public boolean dismountVehicle() {
        return dismountVehicle;
    }

    public enum Flag {

        X(0),
        Y(1),
        Z(2),
        Y_ROT(3),
        X_ROT(4),

        @Since(MINECRAFT_1_21_2)
        DELTA_X(5),
        @Since(MINECRAFT_1_21_2)
        DELTA_Y(6),
        @Since(MINECRAFT_1_21_2)
        DELTA_Z(7),
        @Since(MINECRAFT_1_21_2)
        ROTATE_DELTA(8);

        private final int shift;

        Flag(int shift) {
            this.shift = shift;
        }

        private int getMask() {
            return 1 << shift;
        }

        private boolean isSet(int mask) {
            return (mask & getMask()) == getMask();
        }

        public static Set<Flag> getFlags(int mask) {
            Set<Flag> flags = EnumSet.noneOf(Flag.class);
            for (Flag positionFlag : Flag.values()) {
                if (positionFlag.isSet(mask)) {
                    flags.add(positionFlag);
                }
            }
            return flags;
        }

        public static int getBitfield(Set<Flag> flags) {
            int mask = 0;
            for (Flag positionFlag : flags) {
                mask |= positionFlag.getMask();
            }
            return mask;
        }

    }

}