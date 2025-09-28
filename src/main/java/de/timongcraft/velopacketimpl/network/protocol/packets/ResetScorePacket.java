package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import de.timongcraft.velopacketimpl.network.protocol.packets.core.AbstractPacket;
import de.timongcraft.velopacketimpl.utils.annotations.Since;
import de.timongcraft.velopacketimpl.utils.network.protocol.ExProtocolUtils;
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration;
import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;

import static com.velocitypowered.api.network.ProtocolVersion.*;

/**
 * (latest) Resource Id: 'minecraft:reset_score'
 */
@SuppressWarnings("unused")
@Since(MINECRAFT_1_20_3)
public class ResetScorePacket extends AbstractPacket {

    public static void register(boolean encodeOnly) {
        PacketRegistration.of(ResetScorePacket.class)
                .direction(ProtocolUtils.Direction.CLIENTBOUND)
                .packetSupplier(ResetScorePacket::new)
                .stateRegistry(StateRegistry.PLAY)
                .mapping(0x42, MINECRAFT_1_20_3, encodeOnly)
                .mapping(0x44, MINECRAFT_1_20_5, encodeOnly)
                .mapping(0x49, MINECRAFT_1_21_2, encodeOnly)
                .mapping(0x48, MINECRAFT_1_21_5, encodeOnly)
                .mapping(0x4D, MINECRAFT_1_21_9, encodeOnly)
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
        super.decode(buffer, direction, protocolVersion);

        entityName = ProtocolUtils.readString(buffer);
        objectiveName = ExProtocolUtils.readOptString(buffer);
    }

    @Override
    public void encode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        ProtocolUtils.writeString(buffer, entityName);
        ExProtocolUtils.writeOptString(buffer, objectiveName);
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