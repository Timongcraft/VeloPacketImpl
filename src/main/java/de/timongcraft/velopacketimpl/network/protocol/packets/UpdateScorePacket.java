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
import de.timongcraft.velopacketimpl.utils.annotations.Until;
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
 * (latest) Resource ID: 'minecraft:set_score'
 */
@SuppressWarnings("unused")
public class UpdateScorePacket implements MinecraftPacket {

    /**
     * For Minecraft &gt;=1.20.3:
     * @see ResetScorePacket
     */
    @Until(MINECRAFT_1_20_2)
    public static UpdateScorePacket ofLegacyRemove(String entityName, String objectiveName) {
        return new UpdateScorePacket(
                entityName,
                Action.REMOVE_SCORE,
                objectiveName,
                -1, //see #value()
                null,
                null
        );
    }

    public static UpdateScorePacket of(String entityName, String objectiveName, int value) {
        return of(entityName, objectiveName, value, (Component) null, null);
    }

    @Since(MINECRAFT_1_20_3)
    public static UpdateScorePacket of(String entityName,
                                       String objectiveName,
                                       int value,
                                       @Nullable Component displayName,
                                       @Nullable ComponentUtils.NumberFormat numberFormat) {
        return new UpdateScorePacket(
                entityName,
                Action.CREATE_OR_UPDATE_SCORE, //see #action()
                objectiveName,
                value,
                displayName != null ? Either.secondary(displayName) : null,
                numberFormat
        );
    }

    @ApiStatus.Internal
    @Since(MINECRAFT_1_20_3)
    public static UpdateScorePacket of(String entityName,
                                       String objectiveName,
                                       int value,
                                       @Nullable ComponentHolder displayName,
                                       @Nullable ComponentUtils.NumberFormat numberFormat) {
        return new UpdateScorePacket(
                entityName,
                Action.CREATE_OR_UPDATE_SCORE, //see #action()
                objectiveName,
                value,
                displayName != null ? Either.primary(displayName) : null,
                numberFormat
        );
    }

    public static void register(boolean encodeOnly) {
        PacketRegistriesUtils.register(PLAY, ProtocolUtils.Direction.CLIENTBOUND,
                UpdateScorePacket.class, UpdateScorePacket.Codec.INSTANCE,
                PacketRangeFactory.entry(0x56, MINECRAFT_1_18_2, encodeOnly),
                PacketRangeFactory.entry(0x59, MINECRAFT_1_19_1, encodeOnly),
                PacketRangeFactory.entry(0x57, MINECRAFT_1_19_3, encodeOnly),
                PacketRangeFactory.entry(0x5B, MINECRAFT_1_19_4, encodeOnly),
                PacketRangeFactory.entry(0x5D, MINECRAFT_1_20_2, encodeOnly),
                PacketRangeFactory.entry(0x5F, MINECRAFT_1_20_3, encodeOnly),
                PacketRangeFactory.entry(0x61, MINECRAFT_1_20_5, encodeOnly),
                PacketRangeFactory.entry(0x68, MINECRAFT_1_21_2, encodeOnly),
                PacketRangeFactory.entry(0x67, MINECRAFT_1_21_5, encodeOnly),
                PacketRangeFactory.entry(0x6C, MINECRAFT_1_21_9, encodeOnly));
    }

    private final String entityName;
    @Until(MINECRAFT_1_20_2)
    private final Action action;
    private final String objectiveName;
    private final int value;
    @Since(MINECRAFT_1_20_3)
    private final @Nullable Either<ComponentHolder, Component> displayName;
    @Since(MINECRAFT_1_20_3)
    private final @Nullable ComponentUtils.NumberFormat numberFormat;

    private UpdateScorePacket(String entityName, Action action, String objectiveName, int value,
                              @Nullable Either<ComponentHolder, Component> displayName,
                              @Nullable ComponentUtils.NumberFormat numberFormat) {
        this.entityName = entityName;
        this.action = action;
        this.objectiveName = objectiveName;
        this.value = value;
        this.displayName = displayName;
        this.numberFormat = numberFormat;
    }

    public static class Codec implements PacketCodec<UpdateScorePacket> {

        public static final UpdateScorePacket.Codec INSTANCE = new UpdateScorePacket.Codec();

        @Override
        public UpdateScorePacket decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
            String entityName = ProtocolUtils.readString(buf);
            Action action;
            if (version.lessThan(MINECRAFT_1_20_3)) {
                action = ExProtocolUtils.readEnumByOrdinal(buf, Action.class);
            } else {
                action = Action.CREATE_OR_UPDATE_SCORE; //see #action()
            }
            String objectiveName = ProtocolUtils.readString(buf);
            int value;
            if (version.noLessThan(MINECRAFT_1_20_3)
                    || (version.lessThan(MINECRAFT_1_20_3) && action == Action.CREATE_OR_UPDATE_SCORE)) {
                value = ProtocolUtils.readVarInt(buf);
            } else {
                value = -1; //see #value()
            }
            @Nullable Either<ComponentHolder, Component> displayName;
            @Nullable ComponentUtils.NumberFormat numberFormat;
            if (version.noLessThan(MINECRAFT_1_20_3)) {
                displayName = ExProtocolUtils.readOpt(buf, () -> Either.primary(ExProtocolUtils.readComponentHolder(buf, version)));
                numberFormat = ExProtocolUtils.readOpt(buf, () ->
                        switch (ExProtocolUtils.readEnumByOrdinal(buf, ComponentUtils.NumberFormatType.class)) {
                            case BLANK -> ComponentUtils.NumberFormatBlank.getInstance();
                            case STYLED -> throw new IllegalStateException("Styled number format not implemented");
                            case FIXED -> new ComponentUtils.NumberFormatFixed(ExProtocolUtils.readComponentHolder(buf, version));
                        }
                );
            } else {
                displayName = null;
                numberFormat = null;
            }

            return new UpdateScorePacket(entityName, action, objectiveName, value, displayName, numberFormat);
        }

        @Override
        public void encode(UpdateScorePacket packet, ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
            if (version.lessThan(MINECRAFT_1_20) && packet.entityName.length() > 40) {
                throw new IllegalStateException("entity name can only be 40 chars long");
            }
            ProtocolUtils.writeString(buf, packet.entityName);
            if (version.lessThan(MINECRAFT_1_20_3)) {
                ProtocolUtils.writeVarInt(buf, packet.action.ordinal());
            }
            if (version.lessThan(MINECRAFT_1_20) && packet.objectiveName.length() > 16) {
                throw new IllegalStateException("objective name can only be 16 chars long");
            }
            ProtocolUtils.writeString(buf, packet.objectiveName);
            if ((version.lessThan(MINECRAFT_1_20_3)
                    && packet.action == Action.CREATE_OR_UPDATE_SCORE)
                    || version.noLessThan(MINECRAFT_1_20_3)) {
                ProtocolUtils.writeVarInt(buf, packet.value);
            }
            if (version.noLessThan(MINECRAFT_1_20_3)) {
                ExProtocolUtils.writeOpt(buf, packet.displayName, internalDisplayName ->
                        ExProtocolUtils.writeInternalComponent(buf, version, internalDisplayName));
                ExProtocolUtils.writeOpt(buf, packet.numberFormat, format -> {
                    ExProtocolUtils.writeEnumOrdinal(buf, format.getType());
                    if (format instanceof ComponentUtils.NumberFormatFixed numberFormatFixed) {
                        numberFormatFixed.write(buf, version);
                    }
                });
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

    /**
     * @implNote Always returns {@link Action#CREATE_OR_UPDATE_SCORE} for versions >1.20.2
     */
    @Until(MINECRAFT_1_20_2)
    public Action action() {
        return action;
    }

    public String objectiveName() {
        return objectiveName;
    }

    /**
     * @implNote Returns -1 for legacy {@link Action#REMOVE_SCORE} action
     */
    public int value() {
        return value;
    }

    @Since(MINECRAFT_1_20_3)
    public @Nullable Component displayName() {
        if (displayName == null) return null;
        return ComponentUtils.getComponent(displayName);
    }

    @Since(MINECRAFT_1_20_3)
    public @Nullable ComponentUtils.NumberFormat numberFormat() {
        return numberFormat;
    }

    @Until(MINECRAFT_1_20_2)
    public enum Action {
        CREATE_OR_UPDATE_SCORE, REMOVE_SCORE
    }

}