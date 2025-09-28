package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import de.timongcraft.velopacketimpl.network.protocol.packets.core.AbstractPacket;
import de.timongcraft.velopacketimpl.utils.annotations.Since;
import de.timongcraft.velopacketimpl.utils.annotations.Until;
import de.timongcraft.velopacketimpl.utils.network.PlayerPosition;
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration;
import io.netty.buffer.ByteBuf;

import java.util.EnumSet;
import java.util.Set;

import static com.velocitypowered.api.network.ProtocolVersion.*;

/**
 * (latest) Resource Id: 'minecraft:player_position'
 */
@SuppressWarnings("unused")
public class SynchronizePlayerPositionPacket extends AbstractPacket {

    public static void register(boolean encodeOnly) {
        PacketRegistration.of(SynchronizePlayerPositionPacket.class)
                .direction(ProtocolUtils.Direction.CLIENTBOUND)
                .packetSupplier(SynchronizePlayerPositionPacket::new)
                .stateRegistry(StateRegistry.PLAY)
                .mapping(0x38, MINECRAFT_1_18_2, encodeOnly)
                .mapping(0x36, MINECRAFT_1_19, encodeOnly)
                .mapping(0x39, MINECRAFT_1_19_1, encodeOnly)
                .mapping(0x38, MINECRAFT_1_19_3, encodeOnly)
                .mapping(0x3C, MINECRAFT_1_19_4, encodeOnly)
                .mapping(0x3E, MINECRAFT_1_20_2, encodeOnly)
                .mapping(0x40, MINECRAFT_1_20_5, encodeOnly)
                .mapping(0x42, MINECRAFT_1_21_2, encodeOnly)
                .mapping(0x41, MINECRAFT_1_21_5, encodeOnly)
                .register();
    }

    private int teleportId;
    private PlayerPosition pos;
    private Set<Flag> flags;
    @Until(MINECRAFT_1_19_3)
    private boolean dismountVehicle;

    @Override
    public void decode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        super.decode(buffer, direction, protocolVersion);

        if (protocolVersion.noGreaterThan(MINECRAFT_1_21)) {
            pos = PlayerPosition.read(buffer, true);

            flags = Flag.getFlags(buffer.readUnsignedByte());

            teleportId = ProtocolUtils.readVarInt(buffer);

            if (protocolVersion.noGreaterThan(MINECRAFT_1_19_3)) {
                dismountVehicle = buffer.readBoolean();
            }
        } else {
            teleportId = ProtocolUtils.readVarInt(buffer);

            pos = PlayerPosition.read(buffer, false);

            flags = Flag.getFlags(buffer.readInt());
        }
    }

    @Override
    public void encode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        if (protocolVersion.noGreaterThan(MINECRAFT_1_21)) {
            pos.write(buffer, true);

            buffer.writeByte(Flag.getBitfield(flags));

            ProtocolUtils.writeVarInt(buffer, teleportId);

            if (protocolVersion.noGreaterThan(MINECRAFT_1_19_3)) {
                buffer.writeBoolean(dismountVehicle);
            }
        } else {
            ProtocolUtils.writeVarInt(buffer, teleportId);

            pos.write(buffer, false);

            buffer.writeInt(Flag.getBitfield(flags));
        }
    }

    public PlayerPosition pos() {
        return pos;
    }

    public SynchronizePlayerPositionPacket pos(PlayerPosition pos) {
        this.pos = pos;
        return this;
    }

    public Set<Flag> flags() {
        return flags;
    }

    public SynchronizePlayerPositionPacket flags(Set<Flag> flags) {
        this.flags = flags;
        return this;
    }

    public int teleportId() {
        return teleportId;
    }

    public SynchronizePlayerPositionPacket teleportId(int teleportId) {
        this.teleportId = teleportId;
        return this;
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