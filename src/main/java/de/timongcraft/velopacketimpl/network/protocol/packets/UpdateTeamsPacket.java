package de.timongcraft.velopacketimpl.network.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import de.timongcraft.velopacketimpl.utils.NamedTextColorUtils;
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class UpdateTeamsPacket extends VeloPacket {

    public static void register(boolean encodeOnly) {
        PacketRegistration.of(UpdateTeamsPacket.class)
                .direction(ProtocolUtils.Direction.CLIENTBOUND)
                .packetSupplier(UpdateTeamsPacket::new)
                .stateRegistry(StateRegistry.PLAY)
                .mapping(0x55, ProtocolVersion.MINECRAFT_1_18_2, encodeOnly)
                .mapping(0x55, ProtocolVersion.MINECRAFT_1_19, encodeOnly)
                .mapping(0x58, ProtocolVersion.MINECRAFT_1_19_1, encodeOnly)
                .mapping(0x56, ProtocolVersion.MINECRAFT_1_19_3, encodeOnly)
                .mapping(0x5A, ProtocolVersion.MINECRAFT_1_19_4, encodeOnly)
                .mapping(0x5A, ProtocolVersion.MINECRAFT_1_20, encodeOnly)
                .mapping(0x5C, ProtocolVersion.MINECRAFT_1_20_2, encodeOnly)
                .mapping(0x5E, ProtocolVersion.MINECRAFT_1_20_3, encodeOnly)
                .mapping(0x60, ProtocolVersion.MINECRAFT_1_20_5, encodeOnly)
                .register();
    }

    private String teamName;
    private Mode mode;
    private Component teamDisplayName;
    private List<FriendlyFlag> friendlyFlags;
    private NameTagVisibility nameTagVisibility;
    private CollisionRule collisionRule;
    private NamedTextColor teamColor;
    private Component teamPrefix;
    private Component teamSuffix;
    private List<String> entities;

    public UpdateTeamsPacket() {}

    public UpdateTeamsPacket(String teamName, Mode mode, Component teamDisplayName, NameTagVisibility nameTagVisibility, CollisionRule collisionRule, List<String> entities) {
        this(teamName, mode, teamDisplayName, new ArrayList<>(), nameTagVisibility, collisionRule, null, null, null, entities);
    }

    public UpdateTeamsPacket(String teamName, Mode mode, Component teamDisplayName, List<FriendlyFlag> friendlyFlags, NameTagVisibility nameTagVisibility, CollisionRule collisionRule, NamedTextColor teamColor, Component teamPrefix, Component teamSuffix, List<String> entities) {
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

    @Override
    public void decode(ByteBuf buffer, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        decoded = true;

        teamName = ProtocolUtils.readString(buffer);
        mode = Mode.values()[buffer.readByte()];

        if (mode == Mode.CREATE_TEAM || mode == Mode.UPDATE_TEAM_INFO) {
            teamDisplayName = ComponentHolder.read(buffer, protocolVersion).getComponent();
            int flagsBitmask = buffer.readUnsignedByte();
            friendlyFlags = new ArrayList<>();
            for (FriendlyFlag flag : FriendlyFlag.values()) {
                if ((flag.getBitmask() & flagsBitmask) == flag.getBitmask())
                    friendlyFlags.add(flag);
            }
            nameTagVisibility = NameTagVisibility.get(ProtocolUtils.readString(buffer));
            collisionRule = CollisionRule.get(ProtocolUtils.readString(buffer));
            teamColor = NamedTextColorUtils.getNamedTextColorById(ProtocolUtils.readVarInt(buffer));
            teamPrefix = ComponentHolder.read(buffer, protocolVersion).getComponent();
            teamSuffix = ComponentHolder.read(buffer, protocolVersion).getComponent();
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
            new ComponentHolder(protocolVersion, teamDisplayName).write(buffer);
            int flagsBitmask = 0;
            for (FriendlyFlag flag : friendlyFlags)
                flagsBitmask |= flag.getBitmask();
            buffer.writeByte(flagsBitmask);
            String nameTagVisibilityKey = nameTagVisibility.getKey();
            if (protocolVersion.equals(ProtocolVersion.MINECRAFT_1_19_4) && nameTagVisibilityKey.length() > 32)
                throw new IllegalStateException("name tag visibility can only be 32 chars long");
            ProtocolUtils.writeString(buffer, nameTagVisibilityKey);
            String collisionRuleKey = collisionRule.getKey();
            if (protocolVersion.equals(ProtocolVersion.MINECRAFT_1_19_4) && collisionRuleKey.length() > 32)
                throw new IllegalStateException("collision rule can only be 32 chars long");
            ProtocolUtils.writeString(buffer, collisionRuleKey);
            ProtocolUtils.writeVarInt(buffer, NamedTextColorUtils.getIdByNamedTextColor(teamColor));
            new ComponentHolder(protocolVersion, teamPrefix).write(buffer);
            new ComponentHolder(protocolVersion, teamSuffix).write(buffer);
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

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Component getTeamDisplayName() {
        return teamDisplayName;
    }

    public void setTeamDisplayName(Component teamDisplayName) {
        this.teamDisplayName = teamDisplayName;
    }

    public List<FriendlyFlag> getFriendlyFlags() {
        return friendlyFlags;
    }

    public void setFriendlyFlags(List<FriendlyFlag> friendlyFlags) {
        this.friendlyFlags = friendlyFlags;
    }

    public NameTagVisibility getNameTagVisibility() {
        return nameTagVisibility;
    }

    public void setNameTagVisibility(NameTagVisibility nameTagVisibility) {
        this.nameTagVisibility = nameTagVisibility;
    }

    public CollisionRule getCollisionRule() {
        return collisionRule;
    }

    public void setCollisionRule(CollisionRule collisionRule) {
        this.collisionRule = collisionRule;
    }

    public NamedTextColor getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(NamedTextColor teamColor) {
        this.teamColor = teamColor;
    }

    public Component getTeamPrefix() {
        return teamPrefix;
    }

    public void setTeamPrefix(Component teamPrefix) {
        this.teamPrefix = teamPrefix;
    }

    public Component getTeamSuffix() {
        return teamSuffix;
    }

    public void setTeamSuffix(Component teamSuffix) {
        this.teamSuffix = teamSuffix;
    }

    public List<String> getEntities() {
        return entities;
    }

    public void setEntities(List<String> entities) {
        this.entities = entities;
    }

    public enum Mode {
        CREATE_TEAM, REMOVE_TEAM, UPDATE_TEAM_INFO, ADD_ENTITIES, REMOVE_ENTITIES
    }

    public enum FriendlyFlag {
        ALLOW_FRIENDLY_FIRE(0x01), SEE_TEAM_INVISIBLE_PLAYERS(0x02);

        private final int bitmask;

        FriendlyFlag(int bitmask) {
            this.bitmask = bitmask;
        }

        public int getBitmask() {
            return bitmask;
        }
    }

    public enum NameTagVisibility {
        ALWAYS("always"), HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"), HIDE_FOR_OWN_TEAM("hideForOwnTeam"), NEVER("never");

        private static final Map<String, NameTagVisibility> valuesMap = new HashMap<>();

        public static NameTagVisibility get(String name) {
            return valuesMap.get(name);
        }

        static {
            for (NameTagVisibility value : values())
                valuesMap.put(value.getKey(), value);
        }

        private final String name;

        NameTagVisibility(String name) {
            this.name = name;
        }

        public String getKey() {
            return name;
        }
    }

    public enum CollisionRule {
        ALWAYS("always"), PUSH_OTHER_TEAMS("pushOtherTeams"), PUSH_OWN_TEAM("pushOwnTeam"), NEVER("never");

        private static final Map<String, CollisionRule> valuesMap = new HashMap<>();

        public static CollisionRule get(String name) {
            return valuesMap.get(name);
        }

        static {
            for (CollisionRule value : values())
                valuesMap.put(value.getKey(), value);
        }

        private final String name;

        CollisionRule(String name) {
            this.name = name;
        }

        public String getKey() {
            return name;
        }
    }

}
