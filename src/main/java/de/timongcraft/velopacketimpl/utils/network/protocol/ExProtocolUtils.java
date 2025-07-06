package de.timongcraft.velopacketimpl.utils.network.protocol;

import com.google.common.collect.Lists;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import de.timongcraft.velopacketimpl.utils.ComponentUtils;
import de.timongcraft.velopacketimpl.utils.Either;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class ExProtocolUtils {

    public static @Nullable <T> T readOpt(ByteBuf buf, Supplier<T> reader) {
        if (buf.readBoolean()) {
            return reader.get();
        } else {
            return null;
        }
    }

    //intellij is too dumb to detect that we ensured not null, with runnable
    public static <T> void writeOpt(ByteBuf buf, @Nullable T value, Consumer<T> writer) {
        buf.writeBoolean(value != null);
        if (value != null) {
            writer.accept(value);
        }
    }

    public static @Nullable String readOptString(ByteBuf buf) {
        if (buf.readBoolean()) {
            return ProtocolUtils.readString(buf);
        } else {
            return null;
        }
    }

    public static void writeOptString(ByteBuf buf, @Nullable String s) {
        buf.writeBoolean(s != null);
        if (s != null) {
            ProtocolUtils.writeString(buf, s);
        }
    }

    public static ComponentHolder readComponentHolder(ByteBuf buf, ProtocolVersion version) {
        return ComponentHolder.read(buf, version);
    }

    public static void writeInternalComponent(ByteBuf buf, ProtocolVersion version, Either<ComponentHolder, Component> internalComponent) {
        if (internalComponent.isPrimary()) {
            if (ComponentUtils.getVersion(internalComponent.getPrimary()).equals(version)) {
                //version mismatch -> log?
                new ComponentHolder(version, internalComponent.getPrimary().getComponent()).write(buf);
            } else {
                internalComponent.getPrimary().write(buf);
            }
        } else {
            new ComponentHolder(version, internalComponent.getSecondary()).write(buf);
        }
    }

    public static <T> T readEnumByOrdinal(ByteBuf buf, Class<T> clazz) {
        return clazz.getEnumConstants()[ProtocolUtils.readVarInt(buf)];
    }

    public static <T extends Enum<T>> void writeEnumOrdinal(ByteBuf buf, T t) {
        ProtocolUtils.writeVarInt(buf, t.ordinal());
    }

    public static <T, C extends Collection<T>> C readCollection(ByteBuf buf, IntFunction<C> collectionFactory, Supplier<T> entryReader) {
        int size = ProtocolUtils.readVarInt(buf);
        C collection = collectionFactory.apply(size);

        for (int i = 0; i < size; i++) {
            collection.add(entryReader.get());
        }

        return collection;
    }

    public static <T> List<T> readList(ByteBuf buf, Supplier<T> entryReader) {
        return readCollection(buf, Lists::newArrayListWithCapacity, entryReader);
    }

    public static <T> void writeCollection(ByteBuf buf, Collection<T> collection, Consumer<T> entryWriter) {
        ProtocolUtils.writeVarInt(buf, collection.size());

        for (T object : collection) {
            entryWriter.accept(object);
        }
    }

    public static <K, V, M extends Map<K, V>> M readMap(ByteBuf buf, IntFunction<M> mapFactory, Supplier<K> keyReader, Supplier<V> valueReader) {
        int size = ProtocolUtils.readVarInt(buf);
        M map = mapFactory.apply(size);

        for (int i = 0; i < size; i++) {
            map.put(keyReader.get(), valueReader.get());
        }
        return map;
    }

    public static <T> Map<String, T> readStringKeyMap(ByteBuf buf, Supplier<T> valueReader) {
        int size = ProtocolUtils.readVarInt(buf);
        Map<String, T> map = new HashMap<>(size);

        for (int i = 0; i < size; i++) {
            map.put(ProtocolUtils.readString(buf), valueReader.get());
        }

        return map;
    }

    public static <K, V> void writeMap(ByteBuf buf, Map<K, V> map, Consumer<K> keyWriter, Consumer<V> valueWriter) {
        ProtocolUtils.writeVarInt(buf, map.size());

        for (Map.Entry<K, V> entry : map.entrySet()) {
            keyWriter.accept(entry.getKey());
            valueWriter.accept(entry.getValue());
        }
    }

    public static <T> void writeStringKeyMap(ByteBuf buf, Map<String, T> map, Consumer<T> valueWriter) {
        ProtocolUtils.writeVarInt(buf, map.size());

        for (Map.Entry<String, T> entry : map.entrySet()) {
            ProtocolUtils.writeString(buf, entry.getKey());
            valueWriter.accept(entry.getValue());
        }
    }

}