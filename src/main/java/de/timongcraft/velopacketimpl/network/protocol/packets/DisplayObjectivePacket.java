package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration;
import io.netty.buffer.ByteBuf;

/**
 * (latest) Resource Id: 'minecraft:set_display_objective'
 */
@SuppressWarnings("unused")
public class DisplayObjectivePacket extends VeloPacket {

    public static void register(boolean encodeOnly) {
        PacketRegistration.of(DisplayObjectivePacket.class)
                .direction(ProtocolUtils.Direction.CLIENTBOUND)
                .packetSupplier(DisplayObjectivePacket::new)
                .stateRegistry(StateRegistry.PLAY)
                .mapping(0x4C, ProtocolVersion.MINECRAFT_1_18_2, encodeOnly)
                .mapping(0x4F, ProtocolVersion.MINECRAFT_1_19_1, encodeOnly)
                .mapping(0x4D, ProtocolVersion.MINECRAFT_1_19_3, encodeOnly)
                .mapping(0x51, ProtocolVersion.MINECRAFT_1_19_4, encodeOnly)
                .mapping(0x53, ProtocolVersion.MINECRAFT_1_20_2, encodeOnly)
                .mapping(0x55, ProtocolVersion.MINECRAFT_1_20_3, encodeOnly)
                .mapping(0x57, ProtocolVersion.MINECRAFT_1_20_5, encodeOnly)
                .mapping(0x5C, ProtocolVersion.MINECRAFT_1_21_2, encodeOnly)
                .register();
    }

    private int position;
    private String scoreName;

    public DisplayObjectivePacket() {}

    public DisplayObjectivePacket(int position, String scoreName) {
        position(position);
        this.scoreName = scoreName;
    }

    @Override
    public void decode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        decoded = true;

        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_20_2)) {
            position = ProtocolUtils.readVarInt(buffer);
        } else {
            position = buffer.readByte();
        }
        scoreName = ProtocolUtils.readString(buffer);
    }

    @Override
    public void encode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_20_2)) {
            ProtocolUtils.writeVarInt(buffer, position);
        } else {
            buffer.writeByte(position);
        }

        if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_20) && scoreName.length() > 16)
            throw new IllegalStateException("score name can only be 16 chars long");
        ProtocolUtils.writeString(buffer, scoreName);
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return false;
    }

    public int position() {
        return position;
    }

    public DisplayObjectivePacket position(int position) {
        if (position < 0 || position > 18)
            throw new IllegalStateException("position can only be 0-18");
        this.position = position;
        return this;
    }

    public String scoreName() {
        return scoreName;
    }

    public DisplayObjectivePacket scoreName(String scoreName) {
        this.scoreName = scoreName;
        return this;
    }

}