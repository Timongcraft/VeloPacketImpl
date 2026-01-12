package de.timongcraft.velopacketimpl.utils.packet;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class Collections$UnmodifiableMapAccess {

    private static final Class<?> UNMODIFIABLE_MAP_CLASS;
    private static final VarHandle INNER_MAP;
    private static final VarHandle KEYSET_CACHE;
    private static final VarHandle ENTRYSET_CACHE;
    private static final VarHandle VALUES_CACHE;

    static {
        try {
            //noinspection RedundantUnmodifiable
            Map<?, ?> wrapper = Collections.unmodifiableMap(Collections.emptyMap());
            UNMODIFIABLE_MAP_CLASS = wrapper.getClass(); // expected: java.util.Collections$UnmodifiableMap

            MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(UNMODIFIABLE_MAP_CLASS, MethodHandles.lookup());

            INNER_MAP = privateLookup.findVarHandle(UNMODIFIABLE_MAP_CLASS, "m", Map.class);
            KEYSET_CACHE = privateLookup.findVarHandle(UNMODIFIABLE_MAP_CLASS, "keySet", Set.class);
            ENTRYSET_CACHE = privateLookup.findVarHandle(UNMODIFIABLE_MAP_CLASS, "entrySet", Set.class);
            VALUES_CACHE = privateLookup.findVarHandle(UNMODIFIABLE_MAP_CLASS, "values", Collection.class);
        } catch (Throwable t) {
            throw new IllegalStateException(
                    "Failed to init Collections$UnmodifiableMapAccess. Add --add-opens java.base/java.util=ALL-UNNAMED.", t);
        }
    }

    public static <K, V> V put(Map<K, V> unmodifiableMap, K key, V value) {
        //noinspection unchecked
        Map<K, V> inner = (Map<K, V>) INNER_MAP.get(unmodifiableMap);
        V previousValue = inner.put(key, value);

        // clear cached transient view fields
        KEYSET_CACHE.set(unmodifiableMap, null);
        ENTRYSET_CACHE.set(unmodifiableMap, null);
        VALUES_CACHE.set(unmodifiableMap, null);

        return previousValue;
    }

    private Collections$UnmodifiableMapAccess() {}

}