package de.timongcraft.velopacketimpl.utils;

public class Either<L, R> {

    public static <L, R> Either<L, R> primary(L value) {
        return new Either<>(value, null);
    }

    public static <L, R> Either<L, R> secondary(R value) {
        return new Either<>(null, value);
    }

    private final L primary;
    private final R secondary;

    private Either(L primary, R secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    public boolean isPrimary() {
        return primary != null;
    }

    public boolean isSecondary() {
        return secondary != null;
    }

    public L getPrimary() {
        if (isPrimary()) {
            return primary;
        }

        throw new IllegalStateException("No primary value");
    }

    public R getSecondary() {
        if (!isSecondary()) {
            return secondary;
        }

        throw new IllegalStateException("No secondary value");
    }

}