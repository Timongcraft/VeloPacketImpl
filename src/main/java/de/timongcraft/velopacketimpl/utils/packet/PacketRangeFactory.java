package de.timongcraft.velopacketimpl.utils.packet;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.registry.MultiVersionPacketRegistry;
import com.velocitypowered.proxy.protocol.registry.MultiVersionPacketRegistry.VersionRange;
import java.util.List;

public class PacketRangeFactory {

    private static final List<ProtocolVersion> SUPPORTED_VERSIONS = ImmutableList.copyOf(ProtocolVersion.SUPPORTED_VERSIONS);

    public static VersionRange range(int packetId, ProtocolVersion start, ProtocolVersion end, boolean encodeOnly) {
        if (encodeOnly) {
            return VersionRange.encodeOnly(start, end, packetId);
        } else {
            return VersionRange.of(start, end, packetId);
        }
    }

    public static VersionRange range(int packetId, ProtocolVersion start, boolean encodeOnly) {
        if (encodeOnly) {
            return VersionRange.encodeOnly(start, packetId);
        } else {
            return VersionRange.of(start, packetId);
        }
    }

    public static RangeBuilderEntry entry(int packetId, ProtocolVersion start, boolean encodeOnly) {
        return new RangeBuilderEntry(packetId, start, encodeOnly);
    }

    static MultiVersionPacketRegistry.VersionRange[] buildRanges(RangeBuilderEntry... rangeBuilderEntries) {
        MultiVersionPacketRegistry.VersionRange[] result = new MultiVersionPacketRegistry.VersionRange[rangeBuilderEntries.length];

        ProtocolVersion newerStartVersion = null;
        for (int i = rangeBuilderEntries.length - 1; i >= 0; i--) {
            RangeBuilderEntry entry = rangeBuilderEntries[i];

            ProtocolVersion entryEnd = null;
            if (newerStartVersion != null) {
                int earlierIndex = SUPPORTED_VERSIONS.indexOf(newerStartVersion);
                entryEnd = SUPPORTED_VERSIONS.get(earlierIndex - 1);
            }

            MultiVersionPacketRegistry.VersionRange range;
            if (entryEnd == null) {
                range = PacketRangeFactory.range(entry.packetId, entry.start, entry.encodeOnly);
            } else {
                range = PacketRangeFactory.range(entry.packetId, entry.start, entryEnd, entry.encodeOnly);
            }
            result[i] = range;

            newerStartVersion = entry.start;
        }

        return result;
    }

    public static final class RangeBuilderEntry {

        private final int packetId;
        private final ProtocolVersion start;
        private final boolean encodeOnly;

        private RangeBuilderEntry(int packetId, ProtocolVersion start, boolean encodeOnly) {
            this.packetId = packetId;
            this.start = start;
            this.encodeOnly = encodeOnly;
        }

    }

}