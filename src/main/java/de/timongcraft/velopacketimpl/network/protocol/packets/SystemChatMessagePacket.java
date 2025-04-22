package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import de.timongcraft.velopacketimpl.utils.ComponentUtils;
import de.timongcraft.velopacketimpl.utils.Either;
import de.timongcraft.velopacketimpl.utils.annotations.Since;
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;

/**
 * (latest) Resource Id: 'minecraft:system_chat'
 */
@SuppressWarnings("unused")
@Since(ProtocolVersion.MINECRAFT_1_19)
public class SystemChatMessagePacket extends VeloPacket {

    public static void register(boolean encodeOnly) {
        PacketRegistration.of(SystemChatMessagePacket.class)
                .direction(ProtocolUtils.Direction.CLIENTBOUND)
                .packetSupplier(SystemChatMessagePacket::new)
                .stateRegistry(StateRegistry.PLAY)
                .mapping(0x5F, ProtocolVersion.MINECRAFT_1_19, encodeOnly)
                .mapping(0x62, ProtocolVersion.MINECRAFT_1_19_1, encodeOnly)
                .mapping(0x60, ProtocolVersion.MINECRAFT_1_19_3, encodeOnly)
                .mapping(0x64, ProtocolVersion.MINECRAFT_1_19_4, encodeOnly)
                .mapping(0x67, ProtocolVersion.MINECRAFT_1_20_2, encodeOnly)
                .mapping(0x69, ProtocolVersion.MINECRAFT_1_20_3, encodeOnly)
                .mapping(0x6C, ProtocolVersion.MINECRAFT_1_20_5, encodeOnly)
                .mapping(0x73, ProtocolVersion.MINECRAFT_1_21_2, encodeOnly)
                .mapping(0x72, ProtocolVersion.MINECRAFT_1_21_5, encodeOnly)
                .register();
    }

    private Either<ComponentHolder, Component> content;
    private boolean overlay;

    public SystemChatMessagePacket() {}

    public SystemChatMessagePacket(ComponentHolder content, boolean overlay) {
        this.content = Either.primary(content);
        this.overlay = overlay;
    }

    public SystemChatMessagePacket(Component content, boolean overlay) {
        this.content = Either.secondary(content);
        this.overlay = overlay;
    }

    @Override
    public void decode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        decoded = true;

        content = Either.primary(ComponentHolder.read(buffer, protocolVersion));
        overlay = buffer.readBoolean();
    }

    @Override
    public void encode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        if (content.isPrimary()) {
            if (ComponentUtils.getVersion(content.getPrimary()).equals(protocolVersion)) {
                new ComponentHolder(protocolVersion, content.getPrimary().getComponent()).write(buffer);
            } else {
                content.getPrimary().write(buffer);
            }
        } else {
            new ComponentHolder(protocolVersion, content.getSecondary()).write(buffer);
        }
        buffer.writeBoolean(overlay);
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return false;
    }

    public Component content() {
        if (content.isPrimary()) {
            return content.getPrimary().getComponent();
        } else {
            return content.getSecondary();
        }
    }

    public SystemChatMessagePacket content(Component content) {
        this.content = Either.secondary(content);
        return this;
    }

    public SystemChatMessagePacket content(ComponentHolder content) {
        this.content = Either.primary(content);
        return this;
    }

    public boolean overlay() {
        return overlay;
    }

    public SystemChatMessagePacket overlay(boolean overlay) {
        this.overlay = overlay;
        return this;
    }

}