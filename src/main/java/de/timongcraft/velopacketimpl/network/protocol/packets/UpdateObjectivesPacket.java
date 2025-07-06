package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import de.timongcraft.velopacketimpl.utils.ComponentUtils;
import de.timongcraft.velopacketimpl.utils.Either;
import de.timongcraft.velopacketimpl.utils.annotations.Since;
import de.timongcraft.velopacketimpl.utils.network.protocol.ExProtocolUtils;
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;

import static com.velocitypowered.api.network.ProtocolVersion.*;

/**
 * (latest) Resource Id: 'minecraft:set_objective'
 */
@SuppressWarnings("unused")
public class UpdateObjectivesPacket extends VeloPacket {

    public static void register(boolean encodeOnly) {
        PacketRegistration.of(UpdateObjectivesPacket.class)
                .direction(ProtocolUtils.Direction.CLIENTBOUND)
                .packetSupplier(UpdateObjectivesPacket::new)
                .stateRegistry(StateRegistry.PLAY)
                .mapping(0x53, MINECRAFT_1_18_2, encodeOnly)
                .mapping(0x56, MINECRAFT_1_19_1, encodeOnly)
                .mapping(0x54, MINECRAFT_1_19_3, encodeOnly)
                .mapping(0x58, MINECRAFT_1_19_4, encodeOnly)
                .mapping(0x5A, MINECRAFT_1_20_2, encodeOnly)
                .mapping(0x5C, MINECRAFT_1_20_3, encodeOnly)
                .mapping(0x5E, MINECRAFT_1_20_5, encodeOnly)
                .mapping(0x64, MINECRAFT_1_21_2, encodeOnly)
                .mapping(0x63, MINECRAFT_1_21_5, encodeOnly)
                .register();
    }

    private String objectiveName;
    private Mode mode;
    private Either<ComponentHolder, Component> objectiveValue;
    private Type renderType;
    @Since(MINECRAFT_1_20_3)
    private ComponentUtils.NumberFormat numberFormat;

    public UpdateObjectivesPacket() {}

    public UpdateObjectivesPacket(String objectiveName, Mode mode) {
        this(objectiveName, mode, null, null);
    }

    public UpdateObjectivesPacket(String objectiveName, Mode mode, Component objectiveValue, Type renderType) {
        this.objectiveName = objectiveName;
        this.mode = mode;
        this.objectiveValue = Either.secondary(objectiveValue);
        this.renderType = renderType;
    }

    @Since(MINECRAFT_1_20_3)
    public UpdateObjectivesPacket(String objectiveName, Mode mode, Component objectiveValue, Type renderType, @Nullable ComponentUtils.NumberFormat numberFormat) {
        this.objectiveName = objectiveName;
        this.mode = mode;
        this.objectiveValue = Either.secondary(objectiveValue);
        this.renderType = renderType;
        this.numberFormat = numberFormat;
    }

    @ApiStatus.Internal
    @Since(MINECRAFT_1_20_3)
    public UpdateObjectivesPacket(String objectiveName, Mode mode, ComponentHolder objectiveValue, Type renderType, @Nullable ComponentUtils.NumberFormat numberFormat) {
        this.objectiveName = objectiveName;
        this.mode = mode;
        this.objectiveValue = Either.primary(objectiveValue);
        this.renderType = renderType;
        this.numberFormat = numberFormat;
    }

    @Override
    public void decode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        decoded = true;

        objectiveName = ProtocolUtils.readString(buffer);
        mode = Mode.values()[buffer.readByte()]; // handled as byte in vanilla
        if (mode != Mode.REMOVE_SCOREBOARD) {
            objectiveValue = Either.primary(ExProtocolUtils.readComponentHolder(buffer, protocolVersion));
            renderType = ExProtocolUtils.readEnumByOrdinal(buffer, Type.class);
            if (protocolVersion.noLessThan(MINECRAFT_1_20_3)) {
                numberFormat = ExProtocolUtils.readOpt(buffer, () ->
                        switch (ExProtocolUtils.readEnumByOrdinal(buffer, ComponentUtils.NumberFormatType.class)) {
                            case BLANK -> ComponentUtils.NumberFormatBlank.getInstance();
                            case STYLED -> throw new IllegalStateException("Styled number format not implemented");
                            case FIXED -> new ComponentUtils.NumberFormatFixed(ExProtocolUtils.readComponentHolder(buffer, protocolVersion));
                        });
            }
        }
    }

    @Override
    public void encode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        if (protocolVersion.lessThan(MINECRAFT_1_20) && objectiveName.length() > 16) {
            throw new IllegalStateException("objective name can only be 16 chars long");
        }
        ProtocolUtils.writeString(buffer, objectiveName);
        buffer.writeByte(mode.ordinal()); // handled as byte in vanilla
        if (mode != Mode.REMOVE_SCOREBOARD) {
            ExProtocolUtils.writeInternalComponent(buffer, protocolVersion, objectiveValue);
            ExProtocolUtils.writeEnumOrdinal(buffer, renderType);
            if (protocolVersion.noLessThan(MINECRAFT_1_20_3)) {
                ExProtocolUtils.writeOpt(buffer, numberFormat, format -> {
                    ExProtocolUtils.writeEnumOrdinal(buffer, format.getType());
                    if (format instanceof ComponentUtils.NumberFormatFixed numberFormatFixed) {
                        numberFormatFixed.write(buffer, protocolVersion);
                    } /*else if (numberFormat instanceof ComponentUtils.NumberFormatStyled) {
                        throw new IllegalStateException("Styled number format not implemented");
                    }*/
                });
            }
        }
    }

    public String objectiveName() {
        return objectiveName;
    }

    public UpdateObjectivesPacket objectiveName(String objectiveName) {
        this.objectiveName = objectiveName;
        return this;
    }

    public Mode mode() {
        return mode;
    }

    public UpdateObjectivesPacket mode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public Component objectiveValue() {
        return ComponentUtils.getComponent(objectiveValue);
    }

    public UpdateObjectivesPacket objectiveValue(Component objectiveValue) {
        this.objectiveValue = Either.secondary(objectiveValue);
        return this;
    }

    @ApiStatus.Internal
    public UpdateObjectivesPacket objectiveValue(ComponentHolder objectiveValueHolder) {
        this.objectiveValue = Either.primary(objectiveValueHolder);
        return this;
    }

    public Type type() {
        return renderType;
    }

    public UpdateObjectivesPacket type(Type type) {
        this.renderType = type;
        return this;
    }

    @Since(MINECRAFT_1_20_3)
    public @Nullable ComponentUtils.NumberFormat numberFormat() {
        return numberFormat;
    }

    @Since(MINECRAFT_1_20_3)
    public UpdateObjectivesPacket numberFormat(@Nullable ComponentUtils.NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
        return this;
    }

    public enum Mode {
        CREATE_SCOREBOARD, REMOVE_SCOREBOARD, UPDATE_SCOREBOARD
    }

    public enum Type {
        INTEGER, HEARTS
    }

}