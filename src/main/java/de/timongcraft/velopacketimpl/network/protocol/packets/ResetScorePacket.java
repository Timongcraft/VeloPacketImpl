package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import de.timongcraft.velopacketimpl.utils.annotations.Since;
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration;
import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;

/**
 * (latest) Resource Id: 'minecraft:reset_score'
 */
@SuppressWarnings("unused")
@Since(ProtocolVersion.MINECRAFT_1_20_3)
public class ResetScorePacket extends VeloPacket {

    public static void register(boolean encodeOnly) {
        PacketRegistration.of(ResetScorePacket.class)
                .direction(ProtocolUtils.Direction.CLIENTBOUND)
                .packetSupplier(ResetScorePacket::new)
                .stateRegistry(StateRegistry.PLAY)
                .mapping(0x42, ProtocolVersion.MINECRAFT_1_20_3, encodeOnly)
                .mapping(0x44, ProtocolVersion.MINECRAFT_1_20_5, encodeOnly)
                .mapping(0x49, ProtocolVersion.MINECRAFT_1_21_2, encodeOnly)
                .register();
    }

    private String entityName;
    private @Nullable String objectiveName;

    public ResetScorePacket() {}

    public ResetScorePacket(String entityName) {
        this(entityName, null);
    }

    public ResetScorePacket(String entityName, @Nullable String objectiveName) {
        this.entityName = entityName;
        this.objectiveName = objectiveName;
    }

    @Override
    public void decode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        decoded = true;

        entityName = ProtocolUtils.readString(buffer);
        if (buffer.readBoolean())
            objectiveName = ProtocolUtils.readString(buffer);
    }

    @Override
    public void encode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        ProtocolUtils.writeString(buffer, entityName);
        buffer.writeBoolean(objectiveName != null);
        if (objectiveName != null)
            ProtocolUtils.writeString(buffer, objectiveName);
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return false;
    }

    public String entityName() {
        return entityName;
    }

    public ResetScorePacket entityName(String entityName) {
        this.entityName = entityName;
        return this;
    }

    public @Nullable String objectiveName() {
        return objectiveName;
    }

    public ResetScorePacket objectiveName(@Nullable String objectiveName) {
        this.objectiveName = objectiveName;
        return this;
    }

}