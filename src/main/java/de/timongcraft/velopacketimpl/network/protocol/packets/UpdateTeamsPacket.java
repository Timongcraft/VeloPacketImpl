package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import de.timongcraft.velopacketimpl.utils.Either;
import de.timongcraft.velopacketimpl.utils.NamedTextColorUtils;
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * (latest) Resource Id: 'minecraft:set_player_team'
 */
@SuppressWarnings("unused")
public class UpdateTeamsPacket extends VeloPacket {

    public static void register(boolean encodeOnly) {
        PacketRegistration.of(UpdateTeamsPacket.class)
                .direction(ProtocolUtils.Direction.CLIENTBOUND)
                .packetSupplier(UpdateTeamsPacket::new)
                .stateRegistry(StateRegistry.PLAY)
                .mapping(0x55, ProtocolVersion.MINECRAFT_1_18_2, encodeOnly)
                .mapping(0x58, ProtocolVersion.MINECRAFT_1_19_1, encodeOnly)
                .mapping(0x56, ProtocolVersion.MINECRAFT_1_19_3, encodeOnly)
                .mapping(0x5A, ProtocolVersion.MINECRAFT_1_19_4, encodeOnly)
                .mapping(0x5C, ProtocolVersion.MINECRAFT_1_20_2, encodeOnly)
                .mapping(0x5E, ProtocolVersion.MINECRAFT_1_20_3, encodeOnly)
                .mapping(0x60, ProtocolVersion.MINECRAFT_1_20_5, encodeOnly)
                .mapping(0x67, ProtocolVersion.MINECRAFT_1_21_2, encodeOnly)
                .mapping(0x66, ProtocolVersion.MINECRAFT_1_21_5, encodeOnly)
                .register();
    }

    private String teamName;
    private Mode mode;
    private Either<ComponentHolder, Component> teamDisplayName;
    private Set<FriendlyFlag> friendlyFlags;
    private NameTagVisibility nameTagVisibility;
    private CollisionRule collisionRule;
    private NamedTextColor teamColor;
    private Either<ComponentHolder, Component> teamPrefix;
    private Either<ComponentHolder, Component> teamSuffix;
    private List<String> entities;

    public UpdateTeamsPacket() {}

    public UpdateTeamsPacket(String teamName, Mode mode, Component teamDisplayName, NameTagVisibility nameTagVisibility, CollisionRule collisionRule, List<String> entities) {
        this(teamName, mode, teamDisplayName, EnumSet.noneOf(FriendlyFlag.class), nameTagVisibility, collisionRule, null, null, null, entities);
    }

    public UpdateTeamsPacket(String teamName, Mode mode, Component teamDisplayName, Set<FriendlyFlag> friendlyFlags, NameTagVisibility nameTagVisibility, CollisionRule collisionRule, NamedTextColor teamColor, Component teamPrefix, Component teamSuffix, List<String> entities) {
        this.teamName = teamName;
        this.mode = mode;
        this.teamDisplayName = Either.secondary(teamDisplayName);
        this.friendlyFlags = friendlyFlags;
        this.nameTagVisibility = nameTagVisibility;
        this.collisionRule = collisionRule;
        this.teamColor = teamColor;
        this.teamPrefix = Either.secondary(teamPrefix);
        this.teamSuffix = Either.secondary(teamSuffix);
        this.entities = entities;
    }

    @Override
    public void decode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        decoded = true;

        teamName = ProtocolUtils.readString(buffer);
        mode = Mode.values()[buffer.readByte()];

        if (mode == Mode.CREATE_TEAM || mode == Mode.UPDATE_TEAM_INFO) {
            teamDisplayName = Either.primary(ComponentHolder.read(buffer, protocolVersion));
            friendlyFlags = FriendlyFlag.getFlags(buffer.readUnsignedByte());
            nameTagVisibility = NameTagVisibility.read(buffer, protocolVersion);
            collisionRule = CollisionRule.read(buffer, protocolVersion);
            teamColor = NamedTextColorUtils.getNamedTextColorById(ProtocolUtils.readVarInt(buffer));
            teamPrefix = Either.primary(ComponentHolder.read(buffer, protocolVersion));
            teamSuffix = Either.primary(ComponentHolder.read(buffer, protocolVersion));
        }

        if (mode == Mode.CREATE_TEAM || mode == Mode.ADD_ENTITIES || mode == Mode.REMOVE_ENTITIES) {
            int entityCount = ProtocolUtils.readVarInt(buffer);
            entities = new ArrayList<>(entityCount);
            for (int i = 0; i < entityCount; i++)
                entities.add(ProtocolUtils.readString(buffer));
        }
    }

    @Override
    public void encode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_20) && teamName.length() > 16)
            throw new IllegalStateException("team name can only be 16 chars long");
        ProtocolUtils.writeString(buffer, teamName);
        buffer.writeByte(mode.ordinal());

        if (mode == Mode.CREATE_TEAM || mode == Mode.UPDATE_TEAM_INFO) {
            if (teamDisplayName.isPrimary()) {
                // breaks if packet is read in vX and written to vY
                teamDisplayName.getPrimary().write(buffer);
            } else {
                new ComponentHolder(protocolVersion, teamDisplayName.getSecondary()).write(buffer);
            }
            buffer.writeByte(FriendlyFlag.getBit(friendlyFlags));
            nameTagVisibility.write(buffer, protocolVersion);
            collisionRule.write(buffer, protocolVersion);
            ProtocolUtils.writeVarInt(buffer, NamedTextColorUtils.getIdByNamedTextColor(teamColor));
            if (teamPrefix.isPrimary()) {
                // breaks if packet is read in vX and written to vY
                teamPrefix.getPrimary().write(buffer);
            } else {
                new ComponentHolder(protocolVersion, teamPrefix.getSecondary()).write(buffer);
            }
            if (teamSuffix.isPrimary()) {
                // breaks if packet is read in vX and written to vY
                teamSuffix.getPrimary().write(buffer);
            } else {
                new ComponentHolder(protocolVersion, teamSuffix.getSecondary()).write(buffer);
            }
        }

        if (mode == Mode.CREATE_TEAM || mode == Mode.ADD_ENTITIES || mode == Mode.REMOVE_ENTITIES) {
            int entitiesSize = entities.size();
            if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_20) && entitiesSize > 40)
                throw new IllegalStateException("entities array can only have 40 entries");
            ProtocolUtils.writeVarInt(buffer, entities.size());
            for (String entity : entities)
                ProtocolUtils.writeString(buffer, entity);
        }
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return false;
    }

    public String teamName() {
        return teamName;
    }

    public UpdateTeamsPacket teamName(String teamName) {
        this.teamName = teamName;
        return this;
    }

    public Mode mode() {
        return mode;
    }

    public UpdateTeamsPacket mode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public Component teamDisplayName() {
        if (teamDisplayName.isPrimary()) {
            return teamDisplayName.getPrimary().getComponent();
        } else {
            return teamDisplayName.getSecondary();
        }
    }

    public UpdateTeamsPacket teamDisplayName(Component teamDisplayName) {
        this.teamDisplayName = Either.secondary(teamDisplayName);
        return this;
    }

    public Set<FriendlyFlag> friendlyFlags() {
        return friendlyFlags;
    }

    public UpdateTeamsPacket friendlyFlags(Set<FriendlyFlag> friendlyFlags) {
        this.friendlyFlags = friendlyFlags;
        return this;
    }

    public NameTagVisibility nameTagVisibility() {
        return nameTagVisibility;
    }

    public UpdateTeamsPacket nameTagVisibility(NameTagVisibility nameTagVisibility) {
        this.nameTagVisibility = nameTagVisibility;
        return this;
    }

    public CollisionRule collisionRule() {
        return collisionRule;
    }

    public UpdateTeamsPacket collisionRule(CollisionRule collisionRule) {
        this.collisionRule = collisionRule;
        return this;
    }

    public NamedTextColor teamColor() {
        return teamColor;
    }

    public UpdateTeamsPacket teamColor(NamedTextColor teamColor) {
        this.teamColor = teamColor;
        return this;
    }

    public Component teamPrefix() {
        if (teamPrefix.isPrimary()) {
            return teamPrefix.getPrimary().getComponent();
        } else {
            return teamPrefix.getSecondary();
        }
    }

    public UpdateTeamsPacket teamPrefix(Component teamPrefix) {
        this.teamPrefix = Either.secondary(teamPrefix);
        return this;
    }

    public Component teamSuffix() {
        if (teamSuffix.isPrimary()) {
            return teamSuffix.getPrimary().getComponent();
        } else {
            return teamSuffix.getSecondary();
        }
    }

    public UpdateTeamsPacket teamSuffix(Component teamSuffix) {
        this.teamSuffix = Either.secondary(teamSuffix);
        return this;
    }

    public List<String> entities() {
        return entities;
    }

    public UpdateTeamsPacket entities(List<String> entities) {
        this.entities = entities;
        return this;
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

        public static Set<FriendlyFlag> getFlags(int mask) {
            Set<FriendlyFlag> flags = EnumSet.noneOf(FriendlyFlag.class);
            for (FriendlyFlag flag : FriendlyFlag.values())
                if (flag.isSet(mask))
                    flags.add(flag);
            return flags;
        }

        public static int getBit(Set<FriendlyFlag> flags) {
            int mask = 0;
            for (FriendlyFlag flag : flags)
                mask |= flag.getMask();
            return mask;
        }

    }

    public enum NameTagVisibility {

        ALWAYS("always"), HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"), HIDE_FOR_OWN_TEAM("hideForOwnTeam"), NEVER("never");

        private static final Map<String, NameTagVisibility> VALUES = new HashMap<>();

        public static NameTagVisibility read(ByteBuf buf, ProtocolVersion version) {
            if (version.lessThan(ProtocolVersion.MINECRAFT_1_21_5)) {
                return VALUES.get(ProtocolUtils.readString(buf));
            }

            return values()[ProtocolUtils.readVarInt(buf)];
        }

        static {
            for (NameTagVisibility value : values())
                VALUES.put(value.getKey(), value);
        }

        private final String key;

        NameTagVisibility(String key) {
            this.key = key;
        }

        public void write(ByteBuf buf, ProtocolVersion version) {
            if (version.lessThan(ProtocolVersion.MINECRAFT_1_21_5)) {
                if (version.equals(ProtocolVersion.MINECRAFT_1_19_4) && key.length() > 32)
                    throw new IllegalStateException("name tag visibility can only be 32 chars long");
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
            if (version.lessThan(ProtocolVersion.MINECRAFT_1_21_5)) {
                return VALUES.get(ProtocolUtils.readString(buf));
            }

            return values()[ProtocolUtils.readVarInt(buf)];
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
            if (version.lessThan(ProtocolVersion.MINECRAFT_1_21_5)) {
                if (version.equals(ProtocolVersion.MINECRAFT_1_19_4) && key.length() > 32)
                    throw new IllegalStateException("name tag visibility can only be 32 chars long");
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