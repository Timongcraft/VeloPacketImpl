package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.PacketCodec;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import de.timongcraft.velopacketimpl.utils.ComponentUtils;
import de.timongcraft.velopacketimpl.utils.Either;
import de.timongcraft.velopacketimpl.utils.NamedTextColorUtils;
import de.timongcraft.velopacketimpl.utils.network.protocol.ExProtocolUtils;
import de.timongcraft.velopacketimpl.utils.packet.PacketRegistriesUtils;
import de.timongcraft.velopacketimpl.utils.packet.PacketRangeFactory;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.ApiStatus;

import static com.velocitypowered.api.network.ProtocolVersion.*;
import static de.timongcraft.velopacketimpl.utils.packet.PacketRegistriesUtils.MultiVersionPacketProtocolState.PLAY;

/**
 * (latest) Resource ID: 'minecraft:set_player_team'
 */
@SuppressWarnings("unused")
public class UpdateTeamsPacket implements MinecraftPacket {

    public static UpdateTeamsPacket ofCreate(
            String teamName, Component displayName, ImmutableSet<FriendlyFlag> friendlyFlags,
            NameTagVisibility nameTagVisibility, CollisionRule collisionRule,
            NamedTextColor teamColor, Component prefix, Component suffix,
            ImmutableList<String> entities) {
        return ofCreate(
                teamName,
                Either.secondary(displayName),
                friendlyFlags,
                nameTagVisibility,
                collisionRule,
                teamColor,
                Either.secondary(prefix),
                Either.secondary(suffix),
                entities
        );
    }

    @ApiStatus.Internal
    public static UpdateTeamsPacket ofCreate(
            String teamName, ComponentHolder displayName, ImmutableSet<FriendlyFlag> friendlyFlags,
            NameTagVisibility nameTagVisibility, CollisionRule collisionRule,
            NamedTextColor teamColor, ComponentHolder prefix, ComponentHolder suffix,
            ImmutableList<String> entities) {
        return ofCreate(
                teamName,
                Either.primary(displayName),
                friendlyFlags,
                nameTagVisibility,
                collisionRule,
                teamColor,
                Either.primary(prefix),
                Either.primary(suffix),
                entities
        );
    }

    private static UpdateTeamsPacket ofCreate(
            String teamName, Either<ComponentHolder, Component> displayName, ImmutableSet<FriendlyFlag> friendlyFlags,
            NameTagVisibility nameTagVisibility, CollisionRule collisionRule,
            NamedTextColor teamColor, Either<ComponentHolder, Component> prefix, Either<ComponentHolder, Component> suffix,
            ImmutableList<String> entities) {
        return new UpdateTeamsPacket(
                teamName,
                Mode.CREATE_TEAM,
                displayName,
                friendlyFlags,
                nameTagVisibility,
                collisionRule,
                teamColor,
                prefix,
                suffix,
                entities
        );
    }

    public static UpdateTeamsPacket ofUpdateInfo(String teamName, Component displayName, ImmutableSet<FriendlyFlag> friendlyFlags,
                                                 NameTagVisibility nameTagVisibility, CollisionRule collisionRule,
                                                 NamedTextColor teamColor, Component prefix, Component suffix) {
        return ofUpdateInfo(
                teamName,
                Either.secondary(displayName),
                friendlyFlags,
                nameTagVisibility,
                collisionRule,
                teamColor,
                Either.secondary(prefix),
                Either.secondary(suffix)
        );
    }

    @ApiStatus.Internal
    public static UpdateTeamsPacket ofUpdateInfo(String teamName, ComponentHolder displayName, ImmutableSet<FriendlyFlag> friendlyFlags,
                                                 NameTagVisibility nameTagVisibility, CollisionRule collisionRule,
                                                 NamedTextColor teamColor, ComponentHolder prefix, ComponentHolder suffix) {
        return ofUpdateInfo(
                teamName,
                Either.primary(displayName),
                friendlyFlags,
                nameTagVisibility,
                collisionRule,
                teamColor,
                Either.primary(prefix),
                Either.primary(suffix)
        );
    }

    private static UpdateTeamsPacket ofUpdateInfo(String teamName, Either<ComponentHolder, Component> displayName, ImmutableSet<FriendlyFlag> friendlyFlags,
                                                  NameTagVisibility nameTagVisibility, CollisionRule collisionRule,
                                                  NamedTextColor teamColor, Either<ComponentHolder, Component> prefix, Either<ComponentHolder, Component> suffix) {
        return new UpdateTeamsPacket(
                teamName,
                Mode.UPDATE_TEAM_INFO,
                displayName,
                friendlyFlags,
                nameTagVisibility,
                collisionRule,
                teamColor,
                prefix,
                suffix,
                ImmutableList.of()
        );
    }

    public static UpdateTeamsPacket ofAddEntities(String teamName, ImmutableList<String> entities) {
        return new UpdateTeamsPacket(
                teamName,
                Mode.ADD_ENTITIES,
                Either.secondary(Component.empty()),
                ImmutableSet.of(),
                NameTagVisibility.ALWAYS,
                CollisionRule.ALWAYS,
                NamedTextColor.WHITE,
                Either.secondary(Component.empty()),
                Either.secondary(Component.empty()),
                entities
        );
    }

    public static UpdateTeamsPacket ofRemoveEntities(String teamName, ImmutableList<String> entities) {
        return new UpdateTeamsPacket(
                teamName,
                Mode.REMOVE_ENTITIES,
                Either.secondary(Component.empty()),
                ImmutableSet.of(),
                NameTagVisibility.ALWAYS,
                CollisionRule.ALWAYS,
                NamedTextColor.WHITE,
                Either.secondary(Component.empty()),
                Either.secondary(Component.empty()),
                entities
        );
    }

    public static UpdateTeamsPacket ofRemove(String teamName) {
        return new UpdateTeamsPacket(
                teamName,
                Mode.REMOVE_TEAM,
                Either.secondary(Component.empty()),
                ImmutableSet.of(),
                NameTagVisibility.ALWAYS,
                CollisionRule.ALWAYS,
                NamedTextColor.WHITE,
                Either.secondary(Component.empty()),
                Either.secondary(Component.empty()),
                ImmutableList.of()
        );
    }

    public static void register(boolean encodeOnly) {
        PacketRegistriesUtils.register(PLAY, ProtocolUtils.Direction.CLIENTBOUND,
                UpdateTeamsPacket.class, UpdateTeamsPacket.Codec.INSTANCE,
                PacketRangeFactory.entry(0x55, MINECRAFT_1_18_2, encodeOnly),
                PacketRangeFactory.entry(0x58, MINECRAFT_1_19_1, encodeOnly),
                PacketRangeFactory.entry(0x56, MINECRAFT_1_19_3, encodeOnly),
                PacketRangeFactory.entry(0x5A, MINECRAFT_1_19_4, encodeOnly),
                PacketRangeFactory.entry(0x5C, MINECRAFT_1_20_2, encodeOnly),
                PacketRangeFactory.entry(0x5E, MINECRAFT_1_20_3, encodeOnly),
                PacketRangeFactory.entry(0x60, MINECRAFT_1_20_5, encodeOnly),
                PacketRangeFactory.entry(0x67, MINECRAFT_1_21_2, encodeOnly),
                PacketRangeFactory.entry(0x66, MINECRAFT_1_21_5, encodeOnly),
                PacketRangeFactory.entry(0x6B, MINECRAFT_1_21_9, encodeOnly));
    }

    private final String teamName;
    private final Mode mode;
    private final Either<ComponentHolder, Component> teamDisplayName;
    private final ImmutableSet<FriendlyFlag> friendlyFlags;
    private final NameTagVisibility nameTagVisibility;
    private final CollisionRule collisionRule;
    private final NamedTextColor teamColor;
    private final Either<ComponentHolder, Component> teamPrefix;
    private final Either<ComponentHolder, Component> teamSuffix;
    private final ImmutableList<String> entities;

    private UpdateTeamsPacket(String teamName, Mode mode,
                              Either<ComponentHolder, Component> teamDisplayName,
                              ImmutableSet<FriendlyFlag> friendlyFlags,
                              NameTagVisibility nameTagVisibility,
                              CollisionRule collisionRule,
                              NamedTextColor teamColor,
                              Either<ComponentHolder, Component> teamPrefix,
                              Either<ComponentHolder, Component> teamSuffix,
                              ImmutableList<String> entities) {
        this.teamName = teamName;
        this.mode = mode;
        this.teamDisplayName = teamDisplayName;
        this.friendlyFlags = friendlyFlags;
        this.nameTagVisibility = nameTagVisibility;
        this.collisionRule = collisionRule;
        this.teamColor = teamColor;
        this.teamPrefix = teamPrefix;
        this.teamSuffix = teamSuffix;
        this.entities = entities;
    }

    public static class Codec implements PacketCodec<UpdateTeamsPacket> {

        public static final UpdateTeamsPacket.Codec INSTANCE = new UpdateTeamsPacket.Codec();

        @Override
        public UpdateTeamsPacket decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
            String teamName = ProtocolUtils.readString(buf);
            Mode mode = Mode.values()[buf.readByte()]; // handled as byte in vanilla

            Either<ComponentHolder, Component> teamDisplayName;
            ImmutableSet<FriendlyFlag> friendlyFlags;
            NameTagVisibility nameTagVisibility;
            CollisionRule collisionRule;
            NamedTextColor teamColor;
            Either<ComponentHolder, Component> teamPrefix;
            Either<ComponentHolder, Component> teamSuffix;
            ImmutableList<String> entities;

            if (mode == Mode.CREATE_TEAM || mode == Mode.UPDATE_TEAM_INFO) {
                teamDisplayName = Either.primary(ExProtocolUtils.readComponentHolder(buf, version));
                friendlyFlags = FriendlyFlag.getFlags(buf.readUnsignedByte());
                nameTagVisibility = NameTagVisibility.read(buf, version);
                collisionRule = CollisionRule.read(buf, version);
                teamColor = NamedTextColorUtils.getNamedTextColorById(ProtocolUtils.readVarInt(buf));
                teamPrefix = Either.primary(ExProtocolUtils.readComponentHolder(buf, version));
                teamSuffix = Either.primary(ExProtocolUtils.readComponentHolder(buf, version));
            } else {
                teamDisplayName = Either.secondary(Component.empty());
                friendlyFlags = ImmutableSet.of();
                nameTagVisibility = NameTagVisibility.ALWAYS; //see #nameTagVisibility()
                collisionRule = CollisionRule.ALWAYS; //see #collisionRule();
                teamColor = NamedTextColor.WHITE;
                teamPrefix = Either.secondary(Component.empty());
                teamSuffix = Either.secondary(Component.empty());
            }

            if (mode == Mode.CREATE_TEAM || mode == Mode.ADD_ENTITIES || mode == Mode.REMOVE_ENTITIES) {
                entities = ExProtocolUtils.readList(buf, () -> ProtocolUtils.readString(buf));
            } else {
                entities = ImmutableList.of();
            }

            return new UpdateTeamsPacket(teamName, mode,
                    teamDisplayName, friendlyFlags, nameTagVisibility, collisionRule,
                    teamColor, teamPrefix, teamSuffix, entities);
        }

        @Override
        public void encode(UpdateTeamsPacket packet, ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
            if (version.lessThan(MINECRAFT_1_20) && packet.teamName.length() > 16) {
                throw new IllegalStateException("team name can only be 16 chars long");
            }
            ProtocolUtils.writeString(buf, packet.teamName);
            buf.writeByte(packet.mode.ordinal()); // handled as byte in vanilla

            if (packet.mode == Mode.CREATE_TEAM || packet.mode == Mode.UPDATE_TEAM_INFO) {
                ExProtocolUtils.writeInternalComponent(buf, version, packet.teamDisplayName);
                buf.writeByte(FriendlyFlag.getBit(packet.friendlyFlags));
                packet.nameTagVisibility.write(buf, version);
                packet.collisionRule.write(buf, version);
                ProtocolUtils.writeVarInt(buf, NamedTextColorUtils.getIdByNamedTextColor(packet.teamColor));
                ExProtocolUtils.writeInternalComponent(buf, version, packet.teamPrefix);
                ExProtocolUtils.writeInternalComponent(buf, version, packet.teamSuffix);
            }

            if (packet.mode == Mode.CREATE_TEAM || packet.mode == Mode.ADD_ENTITIES || packet.mode == Mode.REMOVE_ENTITIES) {
                if (version.lessThan(MINECRAFT_1_20) && packet.entities.size() > 40) {
                    throw new IllegalStateException("entities array can only have 40 entries");
                }
                ExProtocolUtils.writeCollection(buf, packet.entities, entity -> ProtocolUtils.writeString(buf, entity));
            }
        }

    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return false;
    }

    public String teamName() {
        return teamName;
    }

    public Mode mode() {
        return mode;
    }

    public Component teamDisplayName() {
        return ComponentUtils.getComponent(teamDisplayName);
    }

    public ImmutableSet<FriendlyFlag> friendlyFlags() {
        return friendlyFlags;
    }

    /**
     * @implNote Returns {@link NameTagVisibility#ALWAYS} for {@link Mode#REMOVE_TEAM}, {@link Mode#ADD_ENTITIES}, {@link Mode#REMOVE_ENTITIES}
     */
    public NameTagVisibility nameTagVisibility() {
        return nameTagVisibility;
    }

    /**
     * @implNote Returns {@link CollisionRule#ALWAYS} for {@link Mode#REMOVE_TEAM}, {@link Mode#ADD_ENTITIES}, {@link Mode#REMOVE_ENTITIES}
     */
    public CollisionRule collisionRule() {
        return collisionRule;
    }

    public NamedTextColor teamColor() {
        return teamColor;
    }

    public Component teamPrefix() {
        return ComponentUtils.getComponent(teamPrefix);
    }

    public Component teamSuffix() {
        return ComponentUtils.getComponent(teamSuffix);
    }

    public ImmutableList<String> entities() {
        return entities;
    }

    public enum Mode {
        CREATE_TEAM, REMOVE_TEAM, UPDATE_TEAM_INFO, ADD_ENTITIES, REMOVE_ENTITIES
    }

    public enum FriendlyFlag {

        ALLOW_FRIENDLY_FIRE(0), SEE_TEAM_INVISIBLE_PLAYERS(1);

        private final int shift;

        FriendlyFlag(int shift) {
            this.shift = shift;
        }

        private int getMask() {
            return 1 << this.shift;
        }

        private boolean isSet(int mask) {
            return (mask & this.getMask()) == this.getMask();
        }

        public static ImmutableSet<FriendlyFlag> getFlags(int mask) {
            ImmutableSet.Builder<FriendlyFlag> builder = ImmutableSet.builder();
            for (FriendlyFlag flag : FriendlyFlag.values()) {
                if (flag.isSet(mask)) {
                    builder.add(flag);
                }
            }
            return builder.build();
        }

        public static int getBit(Set<FriendlyFlag> flags) {
            int mask = 0;
            for (FriendlyFlag flag : flags) {
                mask |= flag.getMask();
            }
            return mask;
        }

    }

    public enum NameTagVisibility {

        ALWAYS("always"), HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"), HIDE_FOR_OWN_TEAM("hideForOwnTeam"), NEVER("never");

        private static final Map<String, NameTagVisibility> VALUES = new HashMap<>();

        public static NameTagVisibility read(ByteBuf buf, ProtocolVersion version) {
            if (version.lessThan(MINECRAFT_1_21_5)) {
                return VALUES.get(ProtocolUtils.readString(buf));
            }

            return ExProtocolUtils.readEnumByOrdinal(buf, NameTagVisibility.class);
        }

        static {
            for (NameTagVisibility value : values()) {
                VALUES.put(value.getKey(), value);
            }
        }

        private final String key;

        NameTagVisibility(String key) {
            this.key = key;
        }

        public void write(ByteBuf buf, ProtocolVersion version) {
            if (version.lessThan(MINECRAFT_1_21_5)) {
                if (version.equals(MINECRAFT_1_19_4) && key.length() > 32) {
                    throw new IllegalStateException("name tag visibility can only be 32 chars long");
                }
                ProtocolUtils.writeString(buf, key);
                return;
            }

            ProtocolUtils.writeVarInt(buf, ordinal());
        }

        public String getKey() {
            return key;
        }

    }

    public enum CollisionRule {

        ALWAYS("always"),
        NEVER("never"),
        PUSH_OTHER_TEAMS("pushOtherTeams"),
        PUSH_OWN_TEAM("pushOwnTeam");

        private static final Map<String, CollisionRule> VALUES = new HashMap<>();

        public static CollisionRule read(ByteBuf buf, ProtocolVersion version) {
            if (version.lessThan(MINECRAFT_1_21_5)) {
                return VALUES.get(ProtocolUtils.readString(buf));
            }

            return ExProtocolUtils.readEnumByOrdinal(buf, CollisionRule.class);
        }

        static {
            for (CollisionRule value : values())
                VALUES.put(value.getKey(), value);
        }

        private final String key;

        CollisionRule(String key) {
            this.key = key;
        }

        public void write(ByteBuf buf, ProtocolVersion version) {
            if (version.lessThan(MINECRAFT_1_21_5)) {
                if (version.equals(MINECRAFT_1_19_4) && key.length() > 32) {
                    throw new IllegalStateException("name tag visibility can only be 32 chars long");
                }
                ProtocolUtils.writeString(buf, key);
                return;
            }

            ProtocolUtils.writeVarInt(buf, ordinal());
        }

        public String getKey() {
            return key;
        }

    }

}