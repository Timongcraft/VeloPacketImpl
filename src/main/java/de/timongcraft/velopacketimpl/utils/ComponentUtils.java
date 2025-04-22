package de.timongcraft.velopacketimpl.utils;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import de.timongcraft.velopacketimpl.utils.annotations.Since;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_20_3;

@SuppressWarnings("unused")
@Since(MINECRAFT_1_20_3)
public class ComponentUtils {

    private static final VarHandle ComponentHolder$version;

    static {
        try {
            ComponentHolder$version = MethodHandles.privateLookupIn(ComponentHolder.class, MethodHandles.lookup())
                    .findVarHandle(ComponentHolder.class, "version", ProtocolVersion.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to find 'version' field in " + ComponentHolder.class.getName());
        }
    }

    public static ProtocolVersion getVersion(ComponentHolder holder) {
        return (ProtocolVersion) ComponentHolder$version.get(holder);
    }

    public enum NumberFormatType {

        BLANK,
        //uses snbt since 1.21.5
        STYLED,
        FIXED

    }

    public abstract static class NumberFormat {

        private final NumberFormatType type;

        protected NumberFormat(NumberFormatType type) {
            this.type = type;
        }

        public NumberFormatType getType() {
            return type;
        }

        public abstract void write(ByteBuf buf, ProtocolVersion version);

        public abstract NumberFormat compiled(ProtocolVersion version);

    }

    public static class NumberFormatBlank extends NumberFormat {

        private static final NumberFormatBlank INSTANCE = new NumberFormatBlank();

        public static NumberFormatBlank getInstance() {
            return INSTANCE;
        }

        private NumberFormatBlank() {
            super(NumberFormatType.BLANK);
        }

        @Override
        public void write(ByteBuf buf, ProtocolVersion version) {}

        @Override
        public NumberFormatBlank compiled(ProtocolVersion version) {
            return this;
        }

    }

    public static class NumberFormatFixed extends NumberFormat {

        private final Either<ComponentHolder, Component> content;

        public NumberFormatFixed(ComponentHolder content) {
            super(NumberFormatType.FIXED);
            this.content = Either.primary(content);
        }

        public NumberFormatFixed(Component content) {
            super(NumberFormatType.FIXED);
            this.content = Either.secondary(content);
        }

        @Override
        public void write(ByteBuf buf, ProtocolVersion version) {
            if (content.isPrimary()) {
                if (ComponentUtils.getVersion(content.getPrimary()).equals(version)) {
                    new ComponentHolder(version, content.getPrimary().getComponent()).write(buf);
                } else {
                    content.getPrimary().write(buf);
                }
                content.getPrimary().write(buf);
            } else {
                new ComponentHolder(version, content.getSecondary()).write(buf);
            }
        }

        @Override
        public NumberFormatFixed compiled(ProtocolVersion version) {
            if (content.isPrimary()) {
                if (ComponentUtils.getVersion(content.getPrimary()).equals(version)) {
                    return new NumberFormatFixed(new ComponentHolder(version, content.getPrimary().getComponent()));
                } else {
                    return this;
                }
            } else {
                return new NumberFormatFixed(new ComponentHolder(version, content.getSecondary()));
            }
        }

        public Component getContent() {
            if (content.isSecondary()) {
                return content.getSecondary();
            } else {
                return content.getPrimary().getComponent();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof NumberFormatFixed fixedFormat)) return false;
            return (content.isPrimary() && fixedFormat.content.isPrimary()
                    && content.getPrimary().getComponent().equals(content.getPrimary().getComponent()))
                    || (content.isSecondary() && fixedFormat.content.isSecondary()
                    && content.getSecondary().equals(content.getSecondary()));
        }

    }

}