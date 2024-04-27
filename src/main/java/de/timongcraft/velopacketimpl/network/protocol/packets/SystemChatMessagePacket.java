package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import de.timongcraft.velopacketimpl.utils.annotations.Since;
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration;
import io.netty.buffer.ByteBuf;

@SuppressWarnings("unused")
@Since(ProtocolVersion.MINECRAFT_1_19)
public class SystemChatMessagePacket implements MinecraftPacket {

    public static void register(boolean encodeOnly) {
        PacketRegistration.of(SystemChatMessagePacket.class)
                .direction(ProtocolUtils.Direction.CLIENTBOUND)
                .packetSupplier(SystemChatMessagePacket::new)
                .stateRegistry(StateRegistry.PLAY)
                .mapping(0x5F, ProtocolVersion.MINECRAFT_1_19, encodeOnly)
                .mapping(0x62, ProtocolVersion.MINECRAFT_1_19_1, encodeOnly)
                .mapping(0x60, ProtocolVersion.MINECRAFT_1_19_3, encodeOnly)
                .mapping(0x64, ProtocolVersion.MINECRAFT_1_19_4, encodeOnly)
                .mapping(0x64, ProtocolVersion.MINECRAFT_1_20, encodeOnly)
                .mapping(0x67, ProtocolVersion.MINECRAFT_1_20_2, encodeOnly)
                .mapping(0x69, ProtocolVersion.MINECRAFT_1_20_3, encodeOnly)
                .mapping(0x6C, ProtocolVersion.MINECRAFT_1_20_5, encodeOnly)
                .register();
    }

    private ComponentHolder content;
    private boolean overlay;

    @Override
    public void decode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        content = ComponentHolder.read(buffer, protocolVersion);
        overlay = buffer.readBoolean();
    }

    @Override
    public void encode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        content.write(buffer);
        buffer.writeBoolean(overlay);
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return false;
    }

    public ComponentHolder getContent() {
        return content;
    }

    public void setContent(ComponentHolder content) {
        this.content = content;
    }

    public boolean isOverlay() {
        return overlay;
    }

    public void setOverlay(boolean overlay) {
        this.overlay = overlay;
    }

}