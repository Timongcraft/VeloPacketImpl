package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import de.timongcraft.velopacketimpl.utils.ComponentUtils;
import de.timongcraft.velopacketimpl.utils.Either;
import de.timongcraft.velopacketimpl.utils.annotations.Since;
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;

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
    private Type type;
    @Since(MINECRAFT_1_20_3)
    private ComponentUtils.NumberFormat numberFormat;

    public UpdateObjectivesPacket() {}

    public UpdateObjectivesPacket(String objectiveName, Mode mode) {
        this(objectiveName, mode, null, null);
    }

    public UpdateObjectivesPacket(String objectiveName, Mode mode, Component objectiveValue, Type type) {
        this.objectiveName = objectiveName;
        this.mode = mode;
        this.objectiveValue = Either.secondary(objectiveValue);
        this.type = type;
    }

    @Since(MINECRAFT_1_20_3)
    public UpdateObjectivesPacket(String objectiveName, Mode mode, ComponentHolder objectiveValue, Type type, @Nullable ComponentUtils.NumberFormat numberFormat) {
        this.objectiveName = objectiveName;
        this.mode = mode;
        this.objectiveValue = Either.primary(objectiveValue);
        this.type = type;
        this.numberFormat = numberFormat;
    }

    @Since(MINECRAFT_1_20_3)
    public UpdateObjectivesPacket(String objectiveName, Mode mode, Component objectiveValue, Type type, @Nullable ComponentUtils.NumberFormat numberFormat) {
        this.objectiveName = objectiveName;
        this.mode = mode;
        this.objectiveValue = Either.secondary(objectiveValue);
        this.type = type;
        this.numberFormat = numberFormat;
    }

    @Override
    public void decode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        decoded = true;

        objectiveName = ProtocolUtils.readString(buffer);
        mode = Mode.values()[buffer.readByte()];
        if (mode != Mode.REMOVE_SCOREBOARD) {
            objectiveValue = Either.primary(ComponentHolder.read(buffer, protocolVersion));
            type = Type.values()[ProtocolUtils.readVarInt(buffer)];
            if (protocolVersion.noLessThan(MINECRAFT_1_20_3)) {
                if (buffer.readBoolean()) {
                    numberFormat = switch (ProtocolUtils.readVarInt(buffer)) {
                        case 0 -> ComponentUtils.NumberFormatBlank.getInstance();
                        case 1 -> throw new IllegalStateException("Styled number format not implemented");
                        case 2 -> new ComponentUtils.NumberFormatFixed(ComponentHolder.read(buffer, protocolVersion));
                        default -> throw new IllegalStateException("Invalid number format: " + ProtocolUtils.readVarInt(buffer));
                    };
                }
            }
        }
    }

    @Override
    public void encode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        if (protocolVersion.lessThan(MINECRAFT_1_20) && objectiveName.length() > 16) {
            throw new IllegalStateException("objective name can only be 16 chars long");
        }
        ProtocolUtils.writeString(buffer, objectiveName);
        buffer.writeByte(mode.ordinal());
        if (mode != Mode.REMOVE_SCOREBOARD) {
            if (objectiveValue.isPrimary()) {
                if (ComponentUtils.getVersion(objectiveValue.getPrimary()).equals(protocolVersion)) {
                    new ComponentHolder(protocolVersion, objectiveValue.getPrimary().getComponent()).write(buffer);
                } else {
                    objectiveValue.getPrimary().write(buffer);
                }
            } else {
                new ComponentHolder(protocolVersion, objectiveValue.getSecondary()).write(buffer);
            }
            buffer.writeByte(type.ordinal());
            if (protocolVersion.noLessThan(MINECRAFT_1_20_3)) {
                buffer.writeBoolean(numberFormat != null);
                if (numberFormat != null) {
                    ProtocolUtils.writeVarInt(buffer, numberFormat.getType().ordinal());
                    if (numberFormat instanceof ComponentUtils.NumberFormatFixed numberFormatFixed) {
                        numberFormatFixed.write(buffer, protocolVersion);
                    } /*else if (numberFormat instanceof ComponentUtils.NumberFormatStyled) {
                        throw new IllegalStateException("Styled number format not implemented");
                    }*/
                }
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
        if (objectiveValue.isPrimary()) {
            return objectiveValue.getPrimary().getComponent();
        } else {
            return objectiveValue.getSecondary();
        }
    }

    public UpdateObjectivesPacket objectiveValue(Component objectiveValue) {
        this.objectiveValue = Either.secondary(objectiveValue);
        return this;
    }

    public UpdateObjectivesPacket objectiveValue(ComponentHolder objectiveValue) {
        this.objectiveValue = Either.primary(objectiveValue);
        return this;
    }

    public Type type() {
        return type;
    }

    public UpdateObjectivesPacket type(Type type) {
        this.type = type;
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