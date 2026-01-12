package de.timongcraft.velopacketimpl.utils.packet;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.PacketCodec;
import com.velocitypowered.proxy.protocol.ProtocolStates;
import com.velocitypowered.proxy.protocol.ProtocolUtils.Direction;
import com.velocitypowered.proxy.protocol.registry.MultiVersionPacketRegistry;
import de.timongcraft.velopacketimpl.utils.annotations.Since;

public class PacketRegistriesUtils {

    public static <T extends MinecraftPacket> void register(MultiVersionPacketProtocolState state,
                                                            Direction direction,
                                                            Class<T> packetClass,
                                                            PacketCodec<T> codec,
                                                            PacketRangeFactory.RangeBuilderEntry... rangeBuilderEntries) {
        register(state, direction, packetClass, codec, PacketRangeFactory.buildRanges(rangeBuilderEntries));
    }

    public static <T extends MinecraftPacket> void register(MultiVersionPacketProtocolState state,
                                                            Direction direction,
                                                            Class<T> packetClass,
                                                            PacketCodec<T> codec,
                                                            MultiVersionPacketRegistry.VersionRange... ranges) {
        MultiVersionPacketRegistryMerger.mergeRegistry(getRegistry(state, direction),
                MultiVersionPacketRegistry.builder(direction).register(packetClass, codec, ranges).build());
    }

    private static MultiVersionPacketRegistry getRegistry(MultiVersionPacketProtocolState state, Direction direction) {
        return (MultiVersionPacketRegistry) switch (state) {
            case LOGIN -> direction == Direction.SERVERBOUND ? ProtocolStates.LOGIN_SERVERBOUND : ProtocolStates.LOGIN_CLIENTBOUND;
            case CONFIGURATION ->
                    direction == Direction.SERVERBOUND ? ProtocolStates.CONFIG_SERVERBOUND : ProtocolStates.CONFIG_CLIENTBOUND;
            case PLAY -> direction == Direction.SERVERBOUND ? ProtocolStates.PLAY_SERVERBOUND : ProtocolStates.PLAY_CLIENTBOUND;
        };
    }

    public enum MultiVersionPacketProtocolState {
        LOGIN,
        @Since(ProtocolVersion.MINECRAFT_1_20_2)
        CONFIGURATION,
        PLAY
    }

}