package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.PacketCodec;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import de.timongcraft.velopacketimpl.utils.ComponentUtils;
import de.timongcraft.velopacketimpl.utils.Either;
import de.timongcraft.velopacketimpl.utils.annotations.Since;
import de.timongcraft.velopacketimpl.utils.network.protocol.ExProtocolUtils;
import de.timongcraft.velopacketimpl.utils.packet.PacketRegistriesUtils;
import de.timongcraft.velopacketimpl.utils.packet.PacketRangeFactory;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;

import static com.velocitypowered.api.network.ProtocolVersion.*;
import static de.timongcraft.velopacketimpl.utils.packet.PacketRegistriesUtils.MultiVersionPacketProtocolState.PLAY;

/**
 * (latest) Resource ID: 'minecraft:set_objective'
 */
@SuppressWarnings("unused")
public class UpdateObjectivesPacket implements MinecraftPacket {

    public static UpdateObjectivesPacket ofRemove(String objectiveName) {
        return new UpdateObjectivesPacket(
                objectiveName,
                Mode.REMOVE_SCOREBOARD,
                Either.secondary(Component.empty()),
                null,
                null
        );
    }

    /**
     * @param create true for {@link Mode#CREATE_SCOREBOARD}, false for {@link Mode#UPDATE_SCOREBOARD}
     */
    public static UpdateObjectivesPacket of(String objectiveName, boolean create, Component objectiveValue, Type renderType) {
        return new UpdateObjectivesPacket(
                objectiveName,
                create ? Mode.CREATE_SCOREBOARD : Mode.UPDATE_SCOREBOARD,
                Either.secondary(objectiveValue),
                renderType,
                null
        );
    }

    /**
     * @param create true for {@link Mode#CREATE_SCOREBOARD}, false for {@link Mode#UPDATE_SCOREBOARD}
     */
    @Since(MINECRAFT_1_20_3)
    public static UpdateObjectivesPacket of(String objectiveName, boolean create, Component objectiveValue,
                                            Type renderType, @Nullable ComponentUtils.NumberFormat numberFormat) {
        return new UpdateObjectivesPacket(
                objectiveName,
                create ? Mode.CREATE_SCOREBOARD : Mode.UPDATE_SCOREBOARD,
                Either.secondary(objectiveValue),
                renderType,
                numberFormat
        );
    }

    /**
     * @param create true for {@link Mode#CREATE_SCOREBOARD}, false for {@link Mode#UPDATE_SCOREBOARD}
     */
    @ApiStatus.Internal
    @Since(MINECRAFT_1_20_3)
    public static UpdateObjectivesPacket of(String objectiveName, boolean create, ComponentHolder objectiveValue,
                                                    Type renderType, @Nullable ComponentUtils.NumberFormat numberFormat) {
        return new UpdateObjectivesPacket(
                objectiveName,
                create ? Mode.CREATE_SCOREBOARD : Mode.UPDATE_SCOREBOARD,
                Either.primary(objectiveValue),
                renderType,
                numberFormat
        );
    }

    public static void register(boolean encodeOnly) {
        PacketRegistriesUtils.register(PLAY, ProtocolUtils.Direction.CLIENTBOUND,
                UpdateObjectivesPacket.class, UpdateObjectivesPacket.Codec.INSTANCE,
                PacketRangeFactory.entry(0x53, MINECRAFT_1_18_2, encodeOnly),
                PacketRangeFactory.entry(0x56, MINECRAFT_1_19_1, encodeOnly),
                PacketRangeFactory.entry(0x54, MINECRAFT_1_19_3, encodeOnly),
                PacketRangeFactory.entry(0x58, MINECRAFT_1_19_4, encodeOnly),
                PacketRangeFactory.entry(0x5A, MINECRAFT_1_20_2, encodeOnly),
                PacketRangeFactory.entry(0x5C, MINECRAFT_1_20_3, encodeOnly),
                PacketRangeFactory.entry(0x5E, MINECRAFT_1_20_5, encodeOnly),
                PacketRangeFactory.entry(0x64, MINECRAFT_1_21_2, encodeOnly),
                PacketRangeFactory.entry(0x63, MINECRAFT_1_21_5, encodeOnly),
                PacketRangeFactory.entry(0x68, MINECRAFT_1_21_9, encodeOnly));
    }

    private final String objectiveName;
    private final Mode mode;
    private final Either<ComponentHolder, Component> objectiveValue;
    private final Type renderType;
    @Since(MINECRAFT_1_20_3)
    private final @Nullable ComponentUtils.NumberFormat numberFormat;

    private UpdateObjectivesPacket(String objectiveName, Mode mode, Either<ComponentHolder, Component> objectiveValue, Type renderType, @Nullable ComponentUtils.NumberFormat numberFormat) {
        this.objectiveName = objectiveName;
        this.mode = mode;
        this.objectiveValue = objectiveValue;
        this.renderType = renderType;
        this.numberFormat = numberFormat;
    }

    public static class Codec implements PacketCodec<UpdateObjectivesPacket> {

        public static final UpdateObjectivesPacket.Codec INSTANCE = new UpdateObjectivesPacket.Codec();

        @Override
        public UpdateObjectivesPacket decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
            String objectiveName = ProtocolUtils.readString(buf);
            Mode mode = Mode.values()[buf.readByte()]; // handled as byte in vanilla
            Either<ComponentHolder, Component> objectiveValue;
            Type renderType;
            @Since(MINECRAFT_1_20_3)
            @Nullable ComponentUtils.NumberFormat numberFormat = null;

            if (mode != Mode.REMOVE_SCOREBOARD) {
                objectiveValue = Either.primary(ExProtocolUtils.readComponentHolder(buf, version));
                renderType = ExProtocolUtils.readEnumByOrdinal(buf, Type.class);
                if (version.noLessThan(MINECRAFT_1_20_3)) {
                    numberFormat = ExProtocolUtils.readOpt(buf, () ->
                            switch (ExProtocolUtils.readEnumByOrdinal(buf, ComponentUtils.NumberFormatType.class)) {
                                case BLANK -> ComponentUtils.NumberFormatBlank.getInstance();
                                case STYLED -> throw new IllegalStateException("Styled number format not implemented");
                                case FIXED -> new ComponentUtils.NumberFormatFixed(ExProtocolUtils.readComponentHolder(buf, version));
                            });
                }
            } else {
                objectiveValue = Either.secondary(Component.empty());
                renderType = Type.INTEGER; //see #type()
            }

            return new UpdateObjectivesPacket(objectiveName, mode, objectiveValue, renderType, numberFormat);
        }

        @Override
        public void encode(UpdateObjectivesPacket packet, ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
            if (version.lessThan(MINECRAFT_1_20) && packet.objectiveName.length() > 16) {
                throw new IllegalStateException("objective name can only be 16 chars long");
            }
            ProtocolUtils.writeString(buf, packet.objectiveName);
            buf.writeByte(packet.mode.ordinal()); // handled as byte in vanilla
            if (packet.mode != Mode.REMOVE_SCOREBOARD) {
                ExProtocolUtils.writeInternalComponent(buf, version, packet.objectiveValue);
                ExProtocolUtils.writeEnumOrdinal(buf, packet.renderType);
                if (version.noLessThan(MINECRAFT_1_20_3)) {
                    ExProtocolUtils.writeOpt(buf, packet.numberFormat, format -> {
                        ExProtocolUtils.writeEnumOrdinal(buf, format.getType());
                        if (format instanceof ComponentUtils.NumberFormatFixed numberFormatFixed) {
                            numberFormatFixed.write(buf, version);
                        } /*else if (numberFormat instanceof ComponentUtils.NumberFormatStyled) {
                        throw new IllegalStateException("Styled number format not implemented");
                    }*/
                    });
                }
            }
        }

    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return false;
    }

    public String objectiveName() {
        return objectiveName;
    }

    public Mode mode() {
        return mode;
    }

    public Component objectiveValue() {
        return ComponentUtils.getComponent(objectiveValue);
    }

    /**
     * @implNote Always returns {@link Type#INTEGER} if {@link #mode()} is set to {@link Mode#REMOVE_SCOREBOARD}
     */
    public Type type() {
        return renderType;
    }

    @Since(MINECRAFT_1_20_3)
    public @Nullable ComponentUtils.NumberFormat numberFormat() {
        return numberFormat;
    }

    public enum Mode {
        CREATE_SCOREBOARD, REMOVE_SCOREBOARD, UPDATE_SCOREBOARD
    }

    public enum Type {
        INTEGER, HEARTS
    }

}