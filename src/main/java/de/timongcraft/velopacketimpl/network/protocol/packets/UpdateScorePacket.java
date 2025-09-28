package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import de.timongcraft.velopacketimpl.network.protocol.packets.core.AbstractPacket;
import de.timongcraft.velopacketimpl.utils.ComponentUtils;
import de.timongcraft.velopacketimpl.utils.Either;
import de.timongcraft.velopacketimpl.utils.annotations.Since;
import de.timongcraft.velopacketimpl.utils.annotations.Until;
import de.timongcraft.velopacketimpl.utils.network.protocol.ExProtocolUtils;
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;

import static com.velocitypowered.api.network.ProtocolVersion.*;

/**
 * (latest) Resource Id: 'minecraft:set_score'
 */
@SuppressWarnings("unused")
public class UpdateScorePacket extends AbstractPacket {

    public static void register(boolean encodeOnly) {
        PacketRegistration.of(UpdateScorePacket.class)
                .direction(ProtocolUtils.Direction.CLIENTBOUND)
                .packetSupplier(UpdateScorePacket::new)
                .stateRegistry(StateRegistry.PLAY)
                .mapping(0x56, MINECRAFT_1_18_2, encodeOnly)
                .mapping(0x59, MINECRAFT_1_19_1, encodeOnly)
                .mapping(0x57, MINECRAFT_1_19_3, encodeOnly)
                .mapping(0x5B, MINECRAFT_1_19_4, encodeOnly)
                .mapping(0x5D, MINECRAFT_1_20_2, encodeOnly)
                .mapping(0x5F, MINECRAFT_1_20_3, encodeOnly)
                .mapping(0x61, MINECRAFT_1_20_5, encodeOnly)
                .mapping(0x68, MINECRAFT_1_21_2, encodeOnly)
                .mapping(0x67, MINECRAFT_1_21_5, encodeOnly)
                .mapping(0x6C, MINECRAFT_1_21_9, encodeOnly)
                .register();
    }

    private String entityName;
    @Until(MINECRAFT_1_20_2)
    private Action action;
    private String objectiveName;
    private int value;
    @Since(MINECRAFT_1_20_3)
    private @Nullable Either<ComponentHolder, Component> displayName;
    @Since(MINECRAFT_1_20_3)
    private @Nullable ComponentUtils.NumberFormat numberFormat;

    public UpdateScorePacket() {}

    @Until(MINECRAFT_1_20_2)
    public UpdateScorePacket(String entityName, String objectiveName) {
        this(entityName, Action.REMOVE_SCORE, objectiveName, -1);
    }

    @Until(MINECRAFT_1_20_2)
    public UpdateScorePacket(String entityName, Action action, String objectiveName, int value) {
        this.entityName = entityName;
        this.action = action;
        this.objectiveName = objectiveName;
        this.value = value;
    }

    @Since(MINECRAFT_1_20_3)
    public UpdateScorePacket(String entityName, String objectiveName, int value) {
        this(entityName, objectiveName, value, (Component) null, null);
    }

    @Since(MINECRAFT_1_20_3)
    public UpdateScorePacket(String entityName, String objectiveName, int value, @Nullable Component displayName, @Nullable ComponentUtils.NumberFormat numberFormat) {
        this.entityName = entityName;
        this.objectiveName = objectiveName;
        this.value = value;
        this.displayName = displayName != null ? Either.secondary(displayName) : null;
        this.numberFormat = numberFormat;
    }

    @ApiStatus.Internal
    @Since(MINECRAFT_1_20_3)
    public UpdateScorePacket(String entityName, String objectiveName, int value, @Nullable ComponentHolder displayName, @Nullable ComponentUtils.NumberFormat numberFormat) {
        this.entityName = entityName;
        this.objectiveName = objectiveName;
        this.value = value;
        this.displayName = displayName != null ? Either.primary(displayName) : null;
        this.numberFormat = numberFormat;
    }

    @Override
    public void decode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        super.decode(buffer, direction, protocolVersion);

        entityName = ProtocolUtils.readString(buffer);
        if (protocolVersion.lessThan(MINECRAFT_1_20_3)) {
            action = ExProtocolUtils.readEnumByOrdinal(buffer, Action.class);
        }
        objectiveName = ProtocolUtils.readString(buffer);
        if ((protocolVersion.lessThan(MINECRAFT_1_20_3) && action == Action.CREATE_OR_UPDATE_SCORE)
                || protocolVersion.noLessThan(MINECRAFT_1_20_3)) {
            value = ProtocolUtils.readVarInt(buffer);
        }
        if (protocolVersion.noLessThan(MINECRAFT_1_20_3)) {
            displayName = ExProtocolUtils.readOpt(buffer, () -> Either.primary(ExProtocolUtils.readComponentHolder(buffer, protocolVersion)));
            numberFormat = ExProtocolUtils.readOpt(buffer, () ->
                    switch (ExProtocolUtils.readEnumByOrdinal(buffer, ComponentUtils.NumberFormatType.class)) {
                        case BLANK -> ComponentUtils.NumberFormatBlank.getInstance();
                        case STYLED -> throw new IllegalStateException("Styled number format not implemented");
                        case FIXED -> new ComponentUtils.NumberFormatFixed(ExProtocolUtils.readComponentHolder(buffer, protocolVersion));
                    }
            );
        }
    }

    @Override
    public void encode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        if (protocolVersion.lessThan(MINECRAFT_1_20) && entityName.length() > 40) {
            throw new IllegalStateException("entity name can only be 40 chars long");
        }
        ProtocolUtils.writeString(buffer, entityName);
        if (protocolVersion.lessThan(MINECRAFT_1_20_3)) {
            ProtocolUtils.writeVarInt(buffer, action.ordinal());
        }
        if (protocolVersion.lessThan(MINECRAFT_1_20) && objectiveName.length() > 16) {
            throw new IllegalStateException("objective name can only be 16 chars long");
        }
        ProtocolUtils.writeString(buffer, objectiveName);
        if ((protocolVersion.lessThan(MINECRAFT_1_20_3)
                && action == Action.CREATE_OR_UPDATE_SCORE)
                || protocolVersion.noLessThan(MINECRAFT_1_20_3)) {
            ProtocolUtils.writeVarInt(buffer, value);
        }
        if (protocolVersion.noLessThan(MINECRAFT_1_20_3)) {
            ExProtocolUtils.writeOpt(buffer, displayName, internalDisplayName ->
                    ExProtocolUtils.writeInternalComponent(buffer, protocolVersion, internalDisplayName));
            ExProtocolUtils.writeOpt(buffer, numberFormat, format -> {
                ExProtocolUtils.writeEnumOrdinal(buffer, format.getType());
                if (format instanceof ComponentUtils.NumberFormatFixed numberFormatFixed) {
                    numberFormatFixed.write(buffer, protocolVersion);
                }
            });
        }
    }

    public String entityName() {
        return entityName;
    }

    public UpdateScorePacket entityName(String entityName) {
        this.entityName = entityName;
        return this;
    }

    @Until(MINECRAFT_1_20_2)
    public Action action() {
        return action;
    }

    @Until(MINECRAFT_1_20_2)
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

    @Since(MINECRAFT_1_20_3)
    public @Nullable Component displayName() {
        if (displayName == null) return null;
        return ComponentUtils.getComponent(displayName);
    }

    @Since(MINECRAFT_1_20_3)
    public UpdateScorePacket displayName(@Nullable Component displayName) {
        this.displayName = Either.secondary(displayName);
        return this;
    }

    @Since(MINECRAFT_1_20_3)
    public UpdateScorePacket displayName(@Nullable ComponentHolder displayName) {
        this.displayName = Either.primary(displayName);
        return this;
    }

    @Since(MINECRAFT_1_20_3)
    public @Nullable ComponentUtils.NumberFormat numberFormat() {
        return numberFormat;
    }

    @Since(MINECRAFT_1_20_3)
    public UpdateScorePacket numberFormat(@Nullable ComponentUtils.NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
        return this;
    }

    @Until(MINECRAFT_1_20_2)
    public enum Action {
        CREATE_OR_UPDATE_SCORE, REMOVE_SCORE
    }

}