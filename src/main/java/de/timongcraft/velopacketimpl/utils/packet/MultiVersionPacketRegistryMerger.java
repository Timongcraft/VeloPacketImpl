package de.timongcraft.velopacketimpl.utils.packet;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.registry.MultiVersionPacketRegistry;
import io.netty.util.collection.IntObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Map;

/**
 * Merge utilities for MultiVersionPacketRegistry.
 * Requires reflective access to MultiVersionPacketRegistry.versionMappings and
 * to the private VersionMapping fields.
 */
public class MultiVersionPacketRegistryMerger {

    private static final VarHandle VERSION_MAPPINGS;
    private static final VarHandle PACKET_ID_TO_CODEC;
    private static final VarHandle PACKET_CLASS_TO_CODEC;
    private static final VarHandle PACKET_CLASS_TO_ID;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandles.Lookup priv = MethodHandles.privateLookupIn(MultiVersionPacketRegistry.class, lookup);
            VERSION_MAPPINGS = priv.findVarHandle(MultiVersionPacketRegistry.class, "versionMappings", Map.class);

            Class<?> versionMappingClass = Class.forName(MultiVersionPacketRegistry.class.getName() + "$VersionMapping");

            MethodHandles.Lookup privMapping = MethodHandles.privateLookupIn(versionMappingClass, lookup);

            PACKET_ID_TO_CODEC = privMapping.findVarHandle(versionMappingClass, "packetIdToCodec", IntObjectMap.class);
            PACKET_CLASS_TO_CODEC = privMapping.findVarHandle(versionMappingClass, "packetClassToCodec", Map.class);
            PACKET_CLASS_TO_ID = privMapping.findVarHandle(versionMappingClass, "packetClassToId", Object2IntMap.class);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to obtain VarHandle for MultiVersionPacketRegistry.versionMappings. "
                            + "Run with --add-opens java.base/java.util=ALL-UNNAMED (or open appropriately).", e);
        }
    }

    /**
     * Merge all mappings from source into target.
     * If a version is absent in target, the mapping object is inserted.
     * If both have a mapping for the same version, mapping contents are merged (no replacement).
     *
     * <p>On conflict (same packet class but different codec) an IllegalStateException is thrown.
     */
    public static void mergeRegistry(MultiVersionPacketRegistry target, MultiVersionPacketRegistry source) {
        //noinspection unchecked
        Map<ProtocolVersion, Object> sourceMap = (Map<ProtocolVersion, Object>) VERSION_MAPPINGS.get(source);
        //noinspection unchecked
        Map<ProtocolVersion, Object> targetMap = (Map<ProtocolVersion, Object>) VERSION_MAPPINGS.get(target);

        for (Map.Entry<ProtocolVersion, Object> entry : sourceMap.entrySet()) {
            ProtocolVersion version = entry.getKey();
            Object sourceMapping = entry.getValue();

            Object targetMapping = targetMap.get(version);
            if (targetMapping == null) {
                Collections$UnmodifiableMapAccess.put(targetMap, version, sourceMapping);
            } else {
                mergeVersionMappingInto(targetMapping, sourceMapping);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void mergeVersionMappingInto(Object targetMapping, Object sourceMapping) {
        try {
            Map<Class<?>, Object> sourceClassToCodec = (Map<Class<?>, Object>) PACKET_CLASS_TO_CODEC.get(sourceMapping);
            Map<Class<?>, Object> targetClassToCodec = (Map<Class<?>, Object>) PACKET_CLASS_TO_CODEC.get(targetMapping);

            Object2IntMap<Object> sourceClassToId = (Object2IntMap<Object>) PACKET_CLASS_TO_ID.get(sourceMapping);
            Object2IntMap<Object> targetClassToId = (Object2IntMap<Object>) PACKET_CLASS_TO_ID.get(targetMapping);

            IntObjectMap<Object> targetPacketIdToCodec = (IntObjectMap<Object>) PACKET_ID_TO_CODEC.get(targetMapping);

            // For each packet class in source.packetClassToCodec
            for (Map.Entry<Class<?>, Object> e : sourceClassToCodec.entrySet()) {
                Class<?> packetClass = e.getKey();
                Object sourceCodec = e.getValue();

                // check target for existing codec
                if (targetClassToCodec.containsKey(packetClass)) {
                    Object tgtCodec = targetClassToCodec.get(packetClass);
                    if (tgtCodec != sourceCodec && !tgtCodec.equals(sourceCodec)) {
                        throw new IllegalStateException("Conflict for packet class " + packetClass.getName()
                                + ": target codec=" + tgtCodec + ", source codec=" + sourceCodec);
                    }
                    // already present and equal -> skip
                    continue;
                }

                int id = sourceClassToId.getInt(packetClass);

                targetClassToCodec.put(packetClass, sourceCodec);

                targetClassToId.put(packetClass, id);

                targetPacketIdToCodec.put(id, sourceCodec);
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Throwable t) {
            throw new IllegalStateException("Failed to merge VersionMapping instances: " + t, t);
        }
    }

    private MultiVersionPacketRegistryMerger() {}

}