package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.PacketCodec;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import de.timongcraft.velopacketimpl.utils.annotations.Since;
import de.timongcraft.velopacketimpl.utils.network.protocol.ExProtocolUtils;
import de.timongcraft.velopacketimpl.utils.packet.PacketRegistriesUtils;
import de.timongcraft.velopacketimpl.utils.packet.PacketRangeFactory;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;

import static com.velocitypowered.api.network.ProtocolVersion.*;
import static de.timongcraft.velopacketimpl.utils.packet.PacketRegistriesUtils.MultiVersionPacketProtocolState.PLAY;

/**
 * (latest) Resource ID: 'minecraft:reset_score'
 */
@SuppressWarnings("unused")
@Since(MINECRAFT_1_20_3)
public class ResetScorePacket implements MinecraftPacket {

    /**
     * For Minecraft &lt;1.20.3 and newer:
     * @see UpdateScorePacket#ofLegacyRemove(String, String)
     */
    public static ResetScorePacket of(String entityName) {
        return of(entityName, null);
    }

    /**
     * For Minecraft &lt;1.20.3 and newer:
     * @see UpdateScorePacket#ofLegacyRemove(String, String)
     */
    public static ResetScorePacket of(String entityName, @Nullable String objectiveName) {
        return new ResetScorePacket(entityName, objectiveName);
    }

    public static void register(boolean encodeOnly) {
        PacketRegistriesUtils.register(PLAY, ProtocolUtils.Direction.CLIENTBOUND,
                ResetScorePacket.class, Codec.INSTANCE,
                PacketRangeFactory.entry(0x42, MINECRAFT_1_20_3, encodeOnly),
                PacketRangeFactory.entry(0x44, MINECRAFT_1_20_5, encodeOnly),
                PacketRangeFactory.entry(0x49, MINECRAFT_1_21_2, encodeOnly),
                PacketRangeFactory.entry(0x48, MINECRAFT_1_21_5, encodeOnly),
                PacketRangeFactory.entry(0x4D, MINECRAFT_1_21_9, encodeOnly));
    }

    private final String entityName;
    private final @Nullable String objectiveName;

    private ResetScorePacket(String entityName, @Nullable String objectiveName) {
        this.entityName = entityName;
        this.objectiveName = objectiveName;
    }

    public static class Codec implements PacketCodec<ResetScorePacket> {

        public static final Codec INSTANCE = new Codec();

        @Override
        public ResetScorePacket decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
            String entityName = ProtocolUtils.readString(buf);
            @Nullable String objectiveName = ExProtocolUtils.readOptString(buf);
            return new ResetScorePacket(entityName, objectiveName);
        }

        @Override
        public void encode(ResetScorePacket packet, ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
            ProtocolUtils.writeString(buf, packet.entityName);
            ExProtocolUtils.writeOptString(buf, packet.objectiveName);
        }

    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return false;
    }

    public String entityName() {
        return entityName;
    }

    @Nullable
    public String objectiveName() {
        return objectiveName;
    }

}