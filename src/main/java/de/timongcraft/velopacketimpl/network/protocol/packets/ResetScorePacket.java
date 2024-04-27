package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import de.timongcraft.velopacketimpl.utils.annotations.Since;
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration;
import io.netty.buffer.ByteBuf;

@SuppressWarnings("unused")
@Since(ProtocolVersion.MINECRAFT_1_20_3)
public class ResetScorePacket implements MinecraftPacket {

    public static void register(boolean encodeOnly) {
        PacketRegistration.of(ResetScorePacket.class)
                .direction(ProtocolUtils.Direction.CLIENTBOUND)
                .packetSupplier(ResetScorePacket::new)
                .stateRegistry(StateRegistry.PLAY)
                .mapping(0x42, ProtocolVersion.MINECRAFT_1_20_3, encodeOnly)
                .mapping(0x44, ProtocolVersion.MINECRAFT_1_20_5, encodeOnly)
                .register();
    }

    private String entityName;
    private boolean hasObjectiveName;
    private String objectiveName;

    public ResetScorePacket() {}

    public ResetScorePacket(String entityName) {
        this(entityName, null);
    }

    public ResetScorePacket(String entityName, String objectiveName) {
        this.entityName = entityName;
        if (objectiveName != null) {
            this.hasObjectiveName = true;
            this.objectiveName = objectiveName;
        }
    }

    @Override
    public void decode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        entityName = ProtocolUtils.readString(buffer);
        hasObjectiveName = buffer.readBoolean();
        if (hasObjectiveName)
            objectiveName = ProtocolUtils.readString(buffer);
    }

    @Override
    public void encode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        ProtocolUtils.writeString(buffer, entityName);
        buffer.writeBoolean(hasObjectiveName);
        if (hasObjectiveName)
            ProtocolUtils.writeString(buffer, objectiveName);
    }

    @Override
    public boolean handle(MinecraftSessionHandler minecraftSessionHandler) {
        return false;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public boolean isHasObjectiveName() {
        return hasObjectiveName;
    }

    public void setHasObjectiveName(boolean hasObjectiveName) {
        this.hasObjectiveName = hasObjectiveName;
    }

    public String getObjectiveName() {
        return objectiveName;
    }

    public void setObjectiveName(String objectiveName) {
        this.objectiveName = objectiveName;
    }

}
