package de.timongcraft.velopacketimpl.utils;

public class Either<P, S> {

    public static <P, S> Either<P, S> primary(P value) {
        return new Either<>(value, null);
    }

    public static <P, S> Either<P, S> secondary(S value) {
        return new Either<>(null, value);
    }

    private final P primary;
    private final S secondary;

    private Either(P primary, S secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    public boolean isPrimary() {
        return primary != null;
    }

    public boolean isSecondary() {
        return secondary != null;
    }

    public P getPrimary() {
        if (isPrimary()) {
            return primary;
        }

        throw new IllegalStateException("No primary value");
    }

    public S getSecondary() {
        if (isSecondary()) {
            return secondary;
        }

        throw new IllegalStateException("No secondary value");
    }

}