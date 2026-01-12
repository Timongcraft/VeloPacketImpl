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
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;

import static com.velocitypowered.api.network.ProtocolVersion.*;
import static de.timongcraft.velopacketimpl.utils.packet.PacketRegistriesUtils.MultiVersionPacketProtocolState.PLAY;

/**
 * (latest) Resource ID: 'minecraft:system_chat'
 */
@SuppressWarnings("unused")
@Since(MINECRAFT_1_19)
public class SystemChatMessagePacket implements MinecraftPacket {

    public static SystemChatMessagePacket of(Component content, boolean overlay) {
        return new SystemChatMessagePacket(Either.secondary(content), overlay);
    }

    @ApiStatus.Internal
    public static SystemChatMessagePacket of(ComponentHolder content, boolean overlay) {
        return new SystemChatMessagePacket(Either.primary(content), overlay);
    }

    public static void register(boolean encodeOnly) {
        PacketRegistriesUtils.register(PLAY, ProtocolUtils.Direction.CLIENTBOUND,
                SystemChatMessagePacket.class, SystemChatMessagePacket.Codec.INSTANCE,
                PacketRangeFactory.entry(0x5F, MINECRAFT_1_19, encodeOnly),
                PacketRangeFactory.entry(0x62, MINECRAFT_1_19_1, encodeOnly),
                PacketRangeFactory.entry(0x60, MINECRAFT_1_19_3, encodeOnly),
                PacketRangeFactory.entry(0x64, MINECRAFT_1_19_4, encodeOnly),
                PacketRangeFactory.entry(0x67, MINECRAFT_1_20_2, encodeOnly),
                PacketRangeFactory.entry(0x69, MINECRAFT_1_20_3, encodeOnly),
                PacketRangeFactory.entry(0x6C, MINECRAFT_1_20_5, encodeOnly),
                PacketRangeFactory.entry(0x73, MINECRAFT_1_21_2, encodeOnly),
                PacketRangeFactory.entry(0x72, MINECRAFT_1_21_5, encodeOnly),
                PacketRangeFactory.entry(0x77, MINECRAFT_1_21_9, encodeOnly));
    }

    private final Either<ComponentHolder, Component> content;
    private final boolean overlay;

    private SystemChatMessagePacket(Either<ComponentHolder, Component> content, boolean overlay) {
        this.content = content;
        this.overlay = overlay;
    }

    public static class Codec implements PacketCodec<SystemChatMessagePacket> {

        public static final SystemChatMessagePacket.Codec INSTANCE = new SystemChatMessagePacket.Codec();

        @Override
        public SystemChatMessagePacket decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
            Either<ComponentHolder, Component> content = Either.primary(ExProtocolUtils.readComponentHolder(buf, version));
            boolean overlay = buf.readBoolean();

            return new SystemChatMessagePacket(content, overlay);
        }

        @Override
        public void encode(SystemChatMessagePacket packet, ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
            ExProtocolUtils.writeInternalComponent(buf, version, packet.content);
            buf.writeBoolean(packet.overlay);
        }

    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return false;
    }

    public Component content() {
        return ComponentUtils.getComponent(content);
    }

    public boolean overlay() {
        return overlay;
    }

}