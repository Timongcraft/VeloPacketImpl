package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import de.timongcraft.velopacketimpl.network.protocol.packets.core.AbstractPacket;
import de.timongcraft.velopacketimpl.utils.ComponentUtils;
import de.timongcraft.velopacketimpl.utils.Either;
import de.timongcraft.velopacketimpl.utils.annotations.Since;
import de.timongcraft.velopacketimpl.utils.network.protocol.ExProtocolUtils;
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;

import static com.velocitypowered.api.network.ProtocolVersion.*;

/**
 * (latest) Resource Id: 'minecraft:system_chat'
 */
@SuppressWarnings("unused")
@Since(MINECRAFT_1_19)
public class SystemChatMessagePacket extends AbstractPacket {

    public static void register(boolean encodeOnly) {
        PacketRegistration.of(SystemChatMessagePacket.class)
                .direction(ProtocolUtils.Direction.CLIENTBOUND)
                .packetSupplier(SystemChatMessagePacket::new)
                .stateRegistry(StateRegistry.PLAY)
                .mapping(0x5F, MINECRAFT_1_19, encodeOnly)
                .mapping(0x62, MINECRAFT_1_19_1, encodeOnly)
                .mapping(0x60, MINECRAFT_1_19_3, encodeOnly)
                .mapping(0x64, MINECRAFT_1_19_4, encodeOnly)
                .mapping(0x67, MINECRAFT_1_20_2, encodeOnly)
                .mapping(0x69, MINECRAFT_1_20_3, encodeOnly)
                .mapping(0x6C, MINECRAFT_1_20_5, encodeOnly)
                .mapping(0x73, MINECRAFT_1_21_2, encodeOnly)
                .mapping(0x72, MINECRAFT_1_21_5, encodeOnly)
                .register();
    }

    private Either<ComponentHolder, Component> content;
    private boolean overlay;

    public SystemChatMessagePacket() {}

    public SystemChatMessagePacket(Component content, boolean overlay) {
        this.content = Either.secondary(content);
        this.overlay = overlay;
    }

    @ApiStatus.Internal
    public SystemChatMessagePacket(ComponentHolder content, boolean overlay) {
        this.content = Either.primary(content);
        this.overlay = overlay;
    }

    @Override
    public void decode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        super.decode(buffer, direction, protocolVersion);

        content = Either.primary(ExProtocolUtils.readComponentHolder(buffer, protocolVersion));
        overlay = buffer.readBoolean();
    }

    @Override
    public void encode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        ExProtocolUtils.writeInternalComponent(buffer, protocolVersion, content);
        buffer.writeBoolean(overlay);
    }

    public Component content() {
        return ComponentUtils.getComponent(content);
    }

    public SystemChatMessagePacket content(Component content) {
        this.content = Either.secondary(content);
        return this;
    }

    @ApiStatus.Internal
    public SystemChatMessagePacket content(ComponentHolder contentHolder) {
        this.content = Either.primary(contentHolder);
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