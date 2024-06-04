package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import de.timongcraft.velopacketimpl.utils.annotations.Until;
import de.timongcraft.velopacketimpl.utils.network.Location;
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class SynchronizePlayerPositionPacket extends VeloPacket {

    public static void register(boolean encodeOnly) {
        PacketRegistration.of(SynchronizePlayerPositionPacket.class)
                .direction(ProtocolUtils.Direction.CLIENTBOUND)
                .packetSupplier(SynchronizePlayerPositionPacket::new)
                .stateRegistry(StateRegistry.PLAY)
                .mapping(0x38, ProtocolVersion.MINECRAFT_1_18_2, encodeOnly)
                .mapping(0x36, ProtocolVersion.MINECRAFT_1_19, encodeOnly)
                .mapping(0x39, ProtocolVersion.MINECRAFT_1_19_1, encodeOnly)
                .mapping(0x38, ProtocolVersion.MINECRAFT_1_19_3, encodeOnly)
                .mapping(0x3C, ProtocolVersion.MINECRAFT_1_19_4, encodeOnly)
                .mapping(0x3C, ProtocolVersion.MINECRAFT_1_20, encodeOnly)
                .mapping(0x3E, ProtocolVersion.MINECRAFT_1_20_2, encodeOnly)
                .mapping(0x3E, ProtocolVersion.MINECRAFT_1_20_3, encodeOnly)
                .mapping(0x40, ProtocolVersion.MINECRAFT_1_20_5, encodeOnly)
                .register();
    }

    private Location location;
    private List<Flag> flags;
    private int teleportId;
    @Until(ProtocolVersion.MINECRAFT_1_19_3)
    private boolean dismountVehicle;

    @Override
    public void decode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        decoded = true;

        location = Location.read(buffer);

        int flagsBitmask = buffer.readUnsignedByte();
        flags = new ArrayList<>();
        for (Flag flag : Flag.values()) {
            if ((flag.getBitmask() & flagsBitmask) == flag.getBitmask())
                flags.add(flag);
        }

        teleportId = ProtocolUtils.readVarInt(buffer);

        if (protocolVersion.noGreaterThan(ProtocolVersion.MINECRAFT_1_19_3))
            dismountVehicle = buffer.readBoolean();
    }

    @Override
    public void encode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        location.write(buffer);

        int flagsBitmask = 0;
        for (Flag flag : flags)
            flagsBitmask |= flag.getBitmask();
        buffer.writeByte(flagsBitmask);

        ProtocolUtils.writeVarInt(buffer, teleportId);

        if (protocolVersion.noGreaterThan(ProtocolVersion.MINECRAFT_1_19_3))
            buffer.writeBoolean(dismountVehicle);
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return false;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<Flag> getFlags() {
        return flags;
    }

    public void setFlags(List<Flag> flags) {
        this.flags = flags;
    }

    public int getTeleportId() {
        return teleportId;
    }

    public void setTeleportId(int teleportId) {
        this.teleportId = teleportId;
    }

    public enum Flag {
        X(0x01), Y(0x02), Z(0x04),
        PITCH(0x08), YAW(0x10);

        private final int bitmask;

        Flag(int bitmask) {
            this.bitmask = bitmask;
        }

        public int getBitmask() {
            return bitmask;
        }
    }

}