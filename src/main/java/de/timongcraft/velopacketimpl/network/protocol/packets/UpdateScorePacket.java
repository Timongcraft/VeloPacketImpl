package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import de.timongcraft.velopacketimpl.utils.ComponentUtils;
import de.timongcraft.velopacketimpl.utils.annotations.Since;
import de.timongcraft.velopacketimpl.utils.annotations.Until;
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;

import javax.annotation.Nullable;

/**
 * (latest) Resource Id: 'minecraft:set_score'
 */
@SuppressWarnings("unused")
public class UpdateScorePacket extends VeloPacket {

    public static void register(boolean encodeOnly) {
        PacketRegistration.of(UpdateScorePacket.class)
                .direction(ProtocolUtils.Direction.CLIENTBOUND)
                .packetSupplier(UpdateScorePacket::new)
                .stateRegistry(StateRegistry.PLAY)
                .mapping(0x56, ProtocolVersion.MINECRAFT_1_18_2, encodeOnly)
                .mapping(0x59, ProtocolVersion.MINECRAFT_1_19_1, encodeOnly)
                .mapping(0x57, ProtocolVersion.MINECRAFT_1_19_3, encodeOnly)
                .mapping(0x5B, ProtocolVersion.MINECRAFT_1_19_4, encodeOnly)
                .mapping(0x5D, ProtocolVersion.MINECRAFT_1_20_2, encodeOnly)
                .mapping(0x5F, ProtocolVersion.MINECRAFT_1_20_3, encodeOnly)
                .mapping(0x61, ProtocolVersion.MINECRAFT_1_20_5, encodeOnly)
                .mapping(0x68, ProtocolVersion.MINECRAFT_1_21_2, encodeOnly)
                .register();
    }

    private String entityName;
    @Until(ProtocolVersion.MINECRAFT_1_20_2)
    private Action action;
    private String objectiveName;
    private int value;
    @Since(ProtocolVersion.MINECRAFT_1_20_3)
    private @Nullable Component displayName;
    @Since(ProtocolVersion.MINECRAFT_1_20_3)
    private @Nullable ComponentUtils.NumberFormat numberFormat;

    public UpdateScorePacket() {}

    @Until(ProtocolVersion.MINECRAFT_1_20_2)
    public UpdateScorePacket(String entityName, String objectiveName) {
        this(entityName, Action.REMOVE_SCORE, objectiveName, -1);
    }

    @Until(ProtocolVersion.MINECRAFT_1_20_2)
    public UpdateScorePacket(String entityName, Action action, String objectiveName, int value) {
        this.entityName = entityName;
        this.action = action;
        this.objectiveName = objectiveName;
        this.value = value;
    }

    @Since(ProtocolVersion.MINECRAFT_1_20_3)
    public UpdateScorePacket(String entityName, String objectiveName, int value) {
        this(entityName, objectiveName, value, null, null);
    }

    @Since(ProtocolVersion.MINECRAFT_1_20_3)
    public UpdateScorePacket(String entityName, String objectiveName, int value, @Nullable Component displayName, @Nullable ComponentUtils.NumberFormat numberFormat) {
        this.entityName = entityName;
        this.objectiveName = objectiveName;
        this.value = value;
        this.displayName = displayName;
        this.numberFormat = numberFormat;
    }

    @Override
    public void decode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        decoded = true;

        entityName = ProtocolUtils.readString(buffer);
        if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_20_3))
            action = Action.values()[ProtocolUtils.readVarInt(buffer)];
        objectiveName = ProtocolUtils.readString(buffer);
        if ((protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_20_3) && action == Action.CREATE_OR_UPDATE_SCORE) || protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_20_3))
            value = ProtocolUtils.readVarInt(buffer);
        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_20_3)) {
            if (buffer.readBoolean())
                displayName = ComponentHolder.read(buffer, protocolVersion).getComponent();
            if (buffer.readBoolean()) {
                numberFormat = switch (ProtocolUtils.readVarInt(buffer)) {
                    case 0 -> ComponentUtils.NumberFormatBlank.getInstance();
                    case 2 -> new ComponentUtils.NumberFormatFixed(ComponentHolder.read(buffer, protocolVersion));
                    default ->
                            throw new IllegalStateException("Invalid number format: " + ProtocolUtils.readVarInt(buffer));
                };
            }
        }
    }

    @Override
    public void encode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_20) && entityName.length() > 40)
            throw new IllegalStateException("entity name can only be 40 chars long");
        ProtocolUtils.writeString(buffer, entityName);
        if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_20_3))
            ProtocolUtils.writeVarInt(buffer, action.ordinal());
        if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_20) && objectiveName.length() > 16)
            throw new IllegalStateException("objective name can only be 16 chars long");
        ProtocolUtils.writeString(buffer, objectiveName);
        if ((protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_20_3) && action == Action.CREATE_OR_UPDATE_SCORE) || protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_20_3))
            ProtocolUtils.writeVarInt(buffer, value);
        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_20_3)) {
            buffer.writeBoolean(displayName != null);
            if (displayName != null)
                new ComponentHolder(protocolVersion, displayName).write(buffer);
            buffer.writeBoolean(numberFormat != null);
            if (numberFormat != null) {
                ProtocolUtils.writeVarInt(buffer, numberFormat.getType().ordinal());
                if (numberFormat instanceof ComponentUtils.NumberFormatFixed numberFormatFixed) {
                    numberFormatFixed.getContent().write(buffer);
                }
            }
        }
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return false;
    }

    public String entityName() {
        return entityName;
    }

    public UpdateScorePacket entityName(String entityName) {
        this.entityName = entityName;
        return this;
    }

    @Until(ProtocolVersion.MINECRAFT_1_20_2)
    public Action action() {
        return action;
    }

    @Until(ProtocolVersion.MINECRAFT_1_20_2)
    public UpdateScorePacket action(Action action) {
        this.action = action;
        return this;
    }

    public String objectiveName() {
        return objectiveName;
    }

    public UpdateScorePacket objectiveName(String objectiveName) {
        this.objectiveName = objectiveName;
        return this;
    }

    public int value() {
        return value;
    }

    public UpdateScorePacket value(int value) {
        this.value = value;
        return this;
    }

    @Since(ProtocolVersion.MINECRAFT_1_20_3)
    public @Nullable Component displayName() {
        return displayName;
    }

    @Since(ProtocolVersion.MINECRAFT_1_20_3)
    public UpdateScorePacket displayName(@Nullable Component displayName) {
        this.displayName = displayName;
        return this;
    }

    @Since(ProtocolVersion.MINECRAFT_1_20_3)
    public @Nullable ComponentUtils.NumberFormat numberFormat() {
        return numberFormat;
    }

    @Since(ProtocolVersion.MINECRAFT_1_20_3)
    public UpdateScorePacket numberFormat(@Nullable ComponentUtils.NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
        return this;
    }

    public enum Action {
        CREATE_OR_UPDATE_SCORE, REMOVE_SCORE
    }

}